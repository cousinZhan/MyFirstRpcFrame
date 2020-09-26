package com.doak.common.spring;

import com.doak.common.config.ServiceConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/26 8:02
 * @description：服务提供者bean，注入到spring中
 */
public class ServiceBean extends ServiceConfig implements InitializingBean, ApplicationContextAware,
        BeanNameAware {

    private ApplicationContext applicationContext;

    private String beanName;

    @Override
    public void afterPropertiesSet() throws Exception {
        //设置接口路径
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String s) {
        this.beanName = s;
    }
}
