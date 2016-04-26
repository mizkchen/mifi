package me.kingka.service.core;

import me.kingka.message.Message;
import me.kingka.network.Socket;

/**
 * socket 服务
 *
 * @author swift.apple
 */
public class SocketService extends ServiceAdapter {
    private Socket socket;
    private static Service instance;

    public static synchronized Service getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    private SocketService() {
        socket = Socket.getInstance();
    }

    private boolean isFirstConnect = true;

    /**
     * 连接
     *
     * @param host
     * @param port
     */
    @Override
    public void connect(String host, int port) {
        checkDisposed();
        socket.delegate = this;
        threads.submit(() -> {
            if (isFirstConnect) {
                isFirstConnect = false;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    dispose();
                }));
            }
            socket.connect(host, port);
        });

    }

    /**
     * 断开连接
     */
    @Override
    public void disconnect() {
        checkDisposed();
        if (socket != null) {
            socket.disconnect();
        }
    }


    /**
     * 发送请求
     *
     * @param request
     */
    @Override
    public final void send(final Message.Request request) {
        checkDisposed();
        if (socket != null) {
            threads.submit(() -> socket.send(request));
        }
    }

    /**
     * 清理
     */
    @Override
    protected void cleanup() {
        if (socket != null) {
            socket.delegate = null;
            socket.disconnect();
            socket = null;
        }
    }
}
