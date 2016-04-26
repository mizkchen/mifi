package me.kingka.service.core;

import me.kingka.message.Message;
import me.kingka.network.Delegate;
import me.kingka.network.configuration.Configuration;

/**
 * 服务
 *
 * @author swift.apple
 */
public interface Service extends Delegate, Disposable {

    /**
     * 配置
     *
     * @return
     */
    public Configuration configuration();

    /**
     * 服务是否已激活
     *
     * @return
     */
    boolean isActive();

    /**
     * 获取服务委托对象
     *
     * @return
     */
    Delegate delegate();

    /**
     * 设置服务委托对象
     *
     * @param delegate
     */
    void delegate(Delegate delegate);

    /**
     * 连接
     *
     * @param host
     * @param port
     */
    void connect(String host, int port);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 发送请求
     *
     * @param request
     */
    void send(Message.Request request);
}
