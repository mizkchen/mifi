package me.kingka.network;

import io.netty.channel.Channel;

/**
 * socket 委托接口
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
     * 连接已建立
     *
     * @param channel
     */
    void sessionOpen(Channel channel);

    /**
     * 连接已断开
     *
     * @param channel
     */
    void sessionEnd(Channel channel);

    /**
     * 接收消息
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
