package com.doak.common.config;

import java.util.Map;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/23 21:05
 * @description：顶层配置类
 */
public class AbstractConfig {

    /**
     * 唯一名称
     */
    protected String id;

    /**
     * The customized parameters
     */
    protected Map<String, String> parameters;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractConfig{");
        sb.append("id='").append(id).append('\'');
        sb.append(", parameters=").append(parameters);
        sb.append('}');
        return sb.toString();
    }
}
