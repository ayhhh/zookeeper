package com.ayhhh.configurationcenter;


/**
 * 这个类是未来的配置类
 * 用于存储相应的配置信息
 */
public class Configuration {
    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "config='" + config + '\'' +
                '}';
    }
}
