package com.doak.common.config;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/23 22:22
 * @description：服务提供者配置类
 */
public class ServiceConfig<T> extends AbstractConfig{

    /**
     * The interface class of the exported service
     */
    protected Class<?> interfaceClass;

    /**
     * The reference of the interface implementation
     */
    protected T ref;

}
