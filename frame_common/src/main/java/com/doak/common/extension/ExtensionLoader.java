package com.doak.common.extension;


import com.doak.common.compiler.Compiler;
import com.doak.common.utils.ClassUtils;
import com.doak.common.utils.Holder;
import com.doak.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/13 12:16
 * @description：扩展类加载器
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private final ExtensionFactory extensionFactory;

    private final Class<T> type;

    private volatile String cachedDefaultName;

    private final ConcurrentHashMap<String, Holder<Object>> cacheInstances = new ConcurrentHashMap<>();

    private final Holder<Object> cacheAdaptiveInstance = new Holder<>();

    private volatile Class<?> cachedAdaptiveClass = null;

    private final Holder<Map<String, Class<?>>> cacheClasses = new Holder<>();

    private static final ConcurrentHashMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Object> EXTENSION_INSTANCE = new ConcurrentHashMap<>();

    public ExtensionLoader(Class<T> type) {
        this.type = type;
        this.extensionFactory = type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cacheAdaptiveInstance.get();
        if (instance == null) {
            instance = createAdaptiveExtension();
            cacheAdaptiveInstance.set(instance);
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can't create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * 使用ioc进行注入，只注入对象属性
     */
    private T injectExtension(T instance) {

        //1.遍历实例中的方法
        //2.找出set开头的方法，且入参只有一个
        //3.判断入参不是 Java原始类型，如果是，跳过
        //4.根据入参的对象类型，找出对象的实例
        //5.调用method.invoke进行属性注入

        for (Method method : instance.getClass().getMethods()) {

            if (!isSetter(method)) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (isPrimitive(parameterType)) {
                continue;
            }

            ExtensionLoader<?> extensionLoader = getExtensionLoader(parameterType);
            Object extension = extensionLoader.getAdaptiveExtension();

            try {
                if (null != extension) {
                    method.invoke(instance, extension);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return instance;

    }

    /**
     * return true if and only if:
     * <p>
     * 1, public
     * <p>
     * 2, name starts with "set"
     * <p>
     * 3, only has one parameter
     */
    private boolean isSetter(Method method) {
        return
                method.getName().startsWith("set")
                        && method.getParameterTypes().length == 1
                        && Modifier.isPublic(method.getModifiers());
    }

    private boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    public T getExtension(String name) {
        return getExtension(name, true);
    }

    public T getExtension(String name, boolean wrap) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        Holder<Object> holder = cacheInstances.get(name);
        if (holder == null) {
            cacheInstances.putIfAbsent(name, new Holder<>());
            holder = cacheInstances.get(name);
            T instance = createExtension(name, wrap);
            holder.set(instance);
        }
        return (T)holder.get();
    }

    private T createExtension(String name, boolean wrap) {
        getExtensionClasses();
        Class<?> clazz = cacheClasses.get().get(name);
        if (clazz == null) {
            throw new RuntimeException("找不到"+name +"对应的"+ type +"扩展类");
        }
        T instance = (T)EXTENSION_INSTANCE.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCE.putIfAbsent(clazz, clazz.newInstance());
                instance = (T)EXTENSION_INSTANCE.get(clazz);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        injectExtension(instance);

        if (wrap) {
            //暂时不做
        }
        return instance;
    }

    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    private Class<?> createAdaptiveExtensionClass() {
        String code = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
        ClassLoader classLoader = findClassLoader();
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }

    private void getExtensionClasses() {
        if (cacheClasses.get() == null) {
            synchronized (cacheClasses) {
                if (cacheClasses.get() == null) {
                    cacheClasses.set(loadExtensionClasses());
                }
            }
        }
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        cacheDefaultExtensionName();
        LoadingStrategy loadingStrategy1 = new DubboInternalLoadingStrategy();
        LoadingStrategy loadingStrategy2 = new DubboLoadingStrategy();
        List<LoadingStrategy> loadingStrategies = new ArrayList<>();
        loadingStrategies.add(loadingStrategy1);
        loadingStrategies.add(loadingStrategy2);

        Map<String, Class<?>> extensionClasses = new HashMap<>();

        for (LoadingStrategy strategy : loadingStrategies) {
            loadDirectory(extensionClasses, strategy.directory(), type.getName(), strategy.preferExtensionClassLoader(), strategy.overridden(), strategy.excludedPackages());
        }

        return extensionClasses;
    }

    /**
     * extract and cache default extension name if exists
     */
    private void cacheDefaultExtensionName() {
        SPI spi = type.getAnnotation(SPI.class);
        if (spi != null) {
            cachedDefaultName = spi.value();
        }
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir, String type,
                               boolean extensionLoaderClassLoaderFirst, boolean overridden, String... excludedPackages) {
        String fileName = dir + type;
        try {
            Enumeration<URL> urls = null;
            ClassLoader classLoader = findClassLoader();

            // try to load from ExtensionLoader's ClassLoader first
            if (extensionLoaderClassLoaderFirst) {
                ClassLoader extensionLoaderClassLoader = ExtensionLoader.class.getClassLoader();
                if (ClassLoader.getSystemClassLoader() != extensionLoaderClassLoader) {
                    urls = extensionLoaderClassLoader.getResources(fileName);
                }
            }

            if (urls == null || !urls.hasMoreElements()) {
                if (classLoader != null) {
                    urls = classLoader.getResources(fileName);
                } else {
                    urls = ClassLoader.getSystemResources(fileName);
                }
            }

            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL, overridden, excludedPackages);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private static ClassLoader findClassLoader() {
        return ClassUtils.getClassLoader(ExtensionLoader.class);
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader,
                              URL resourceURL, boolean overridden, String... excludedPackages) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0 && !isExcluded(line, excludedPackages)) {
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name, overridden);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class (interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    private boolean isExcluded(String className, String... excludedPackages) {
        if (excludedPackages != null) {
            for (String excludePackage : excludedPackages) {
                if (className.startsWith(excludePackage + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void loadClass(Map<String, Class<?>> extensionClasses, URL resourceURL, Class<?> clazz, String name,
                           boolean overridden) throws NoSuchMethodException {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error occurred when loading extension class (interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + " is not subtype of interface.");
        }
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            cacheAdaptiveClass(clazz, overridden);
        }
        if (!extensionClasses.containsKey(name)) {
            extensionClasses.put(name, clazz);
        }
    }

    /**
     * cache Adaptive class which is annotated with <code>Adaptive</code>
     */
    private void cacheAdaptiveClass(Class<?> clazz, boolean overridden) {
        if (cachedAdaptiveClass == null || overridden) {
            cachedAdaptiveClass = clazz;
        } else if (!cachedAdaptiveClass.equals(clazz)) {
            throw new IllegalStateException("More than 1 adaptive class found: "
                    + cachedAdaptiveClass.getName()
                    + ", " + clazz.getName());
        }
    }

    public Set<String> getSupportedExtensions() {
        return Optional.ofNullable(cacheClasses.get()).orElse(new HashMap<>()).keySet();
    }

    public T getDefaultExtension() {
        return getExtension(cachedDefaultName);
    }


}
