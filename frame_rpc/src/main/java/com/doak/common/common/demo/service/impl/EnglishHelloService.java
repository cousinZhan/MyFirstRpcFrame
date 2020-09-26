package com.doak.common.common.demo.service.impl;

import com.doak.common.common.common.URL;
import com.doak.common.common.demo.service.HelloService;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/14 22:22
 * @description：英文问候服务
 */
public class EnglishHelloService  implements HelloService {

    @Override
    public void sayHello(String name, URL url) {
        System.out.println("Hello！！" + name);
    }
}
