package com.doak.common.spring.schema;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/23 22:13
 * @description：
 */
public class ApplicationStarter {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/provider.xml");
        context.start();
        while (true) {

        }
    }
}
