package com.doak.common.config;

/**
 * @author ：zhanyiqun
 * @date ：Created in 2020/9/23 20:59
 * @description：提供者配置类
 */
public class ProviderConfig extends AbstractConfig{

    /**
     * Service ip addresses (used when there are multiple network cards available)
     */
    private String host;

    /**
     * Service port
     */
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProviderConfig{");
        sb.append("host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
