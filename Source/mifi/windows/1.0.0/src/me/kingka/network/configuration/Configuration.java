package me.kingka.network.configuration;

/**
 * 服务端配置信息
 *
 * @author swift.apple
 */
public class Configuration {

    private int port = 8888;
    private String storage = "";
    private String password = "";
    private String host = "";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public Configuration() {

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
