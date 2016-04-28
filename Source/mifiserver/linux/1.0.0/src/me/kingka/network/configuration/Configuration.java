package me.kingka.network.configuration;

import java.util.List;

/**
 * 服务端配置信息
 *
 * @author swift.apple
 */
public class Configuration {

    private int port = 8888;
    private String storage = "";
    private String password;

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

    private List<String> files;

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }

}
