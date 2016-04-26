package me.kingka.network;

import io.netty.channel.Channel;

/**
 * socket委托
 *
 * @author swift.apple
 */
public interface Delegate {
    /**
     * 连接状态变更
     *
     * @param state
     */
    void stateChanged(int state);

    /**
     * 建立连接
     *
     * @param channel
     */
    void sessionOpen(Channel channel);

    /**
     * 断开连接
     *
     * @param channel
     */
    void sessionEnd(Channel channel);

    /**
     * 获得消息
     *
     * @param channel
     * @param message
     */
    void messageReceived(Channel channel, Object message);

    /**
     * 产生异常
     *
     * @param exception
     */
    void exceptionCaught(String exception);
}
