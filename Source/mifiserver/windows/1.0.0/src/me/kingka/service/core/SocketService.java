package me.kingka.service.core;

import me.kingka.network.Socket;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;

/**
 * socket服务
 *
 * @author swift.apple
 */
public class SocketService extends ServiceAdapter {

    private static Service instance;
    private Socket socket;
    private Configuration configuration;

    /**
     * 配置信息
     *
     * @return
     */
    @Override
    public Configuration configuration() {
        checkDisposed();
        if (configuration == null) {
            ConfigurationFactory factory = ConfigurationFactory.getInstance();
            configuration = factory.configure();
        }
        return configuration;
    }

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized Service getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    private SocketService() {
        socket = Socket.getInstance();
    }

    /**
     * 关闭服务
     */
    @Override
    public void shutdown() {
        checkDisposed();
        if (socket != null) {
            socket.close();
        }
    }

    private boolean isFirst = true;

    /**
     * 开启服务
     */
    @Override
    public void startup() {
        checkDisposed();
        socket.delegate = this;
        threads.submit(() -> {
            if (isFirst) {
                isFirst = false;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    dispose();
                }));
            }
            configuration();
            socket.open(configuration.getPort());
        });

    }

    /**
     * 清理
     */
    @Override
    protected void cleanup() {
        if (socket != null) {
            socket.delegate = null;
            socket.close();
            socket = null;
        }
        isFirst = false;
        configuration = null;
    }

}
