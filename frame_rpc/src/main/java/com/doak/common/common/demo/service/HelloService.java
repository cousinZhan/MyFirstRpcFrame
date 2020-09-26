package com.doak.common.common.demo.service;

import com.doak.common.common.common.constants.CommonConstants;
import com.doak.common.common.common.URL;
import com.doak.common.common.extension.Adaptive;
import com.doak.common.common.extension.SPI;

@SPI("chinese")
public interface HelloService {

    /**
     * HELLO_KEY  指定读取URL中的某个属性，来动态调用对应的实现类
     * @param name
     * @param url
     */
    @Adaptive({CommonConstants.HELLO_KEY})
    void sayHello(String name, URL url);
}
