package com.doak.common.spring.schema;

import com.doak.common.config.ConsumerConfig;
import com.doak.common.spring.ServiceBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/23 8:00
 * @description：命名空间处理器
 */
public class FrameNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("service",new FrameBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("consumer",new FrameBeanDefinitionParser(ConsumerConfig.class, true));

    }
}
