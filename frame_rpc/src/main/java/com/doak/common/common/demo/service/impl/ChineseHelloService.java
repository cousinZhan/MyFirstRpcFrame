package com.doak.common.common.demo.service.impl;

import com.doak.common.common.common.URL;
import com.doak.common.common.demo.service.HelloService;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/14 22:21
 * @description：中文问候服务
 */
public class ChineseHelloService implements HelloService {
    @Override
    public void sayHello(String name, URL url) {
        System.out.println("您好！" + name);
    }
}
