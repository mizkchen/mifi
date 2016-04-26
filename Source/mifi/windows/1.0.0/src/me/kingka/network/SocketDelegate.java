package me.kingka.network;

import io.netty.channel.Channel;

/**
 * 默认的socket委托
 *
 * @author swift.apple
 */
public class SocketDelegate implements Delegate {
    @Override
    public void stateChanged(int state) {

    }

    @Override
    public void sessionOpen(Channel channel) {

    }

    @Override
    public void sessionEnd(Channel channel) {

    }

    @Override
    public void messageReceived(Channel channel, Object message) {

    }

    @Override
    public void exceptionCaught(String exception) {

    }
}
