package com.doak.common.common.extension;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/13 12:17
 * @description：扩展工厂接口
 */
@SPI
public interface ExtensionFactory {

    /**
     * 获取扩展类
     * @param type 类型
     * @param name key
     * @param <T> 类型
     * @return 扩展类实例
     */
   <T> T getExtension(Class<T> type, String name);
}
