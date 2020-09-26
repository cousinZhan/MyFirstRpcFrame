import com.doak.common.common.common.URL;
import com.doak.common.common.demo.service.HelloService;
import com.doak.common.common.extension.ExtensionLoader;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/14 22:24
 * @description：
 */
public class HelloServiceTest {

    @Test
    public void sayHello() {
        HelloService helloService  = ExtensionLoader.getExtensionLoader(HelloService.class).getAdaptiveExtension();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("hello", "english");
        URL url = new URL("dubo", "localhost", 8099, parameters);
        helloService.sayHello("doak", url);
    }
}
