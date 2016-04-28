package me.kingka.service.core;

import io.netty.channel.Channel;
import me.kingka.network.Delegate;
import me.kingka.network.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务适配器
 *
 * @author swift.apple
 */
public abstract class ServiceAdapter implements Service {
    protected Delegate delegate;
    protected boolean active;
    protected ExecutorService threads;
    private boolean isDisposed;

    public ServiceAdapter() {
        threads = Executors.newCachedThreadPool();
    }

    /**
     * 获取委托对象
     *
     * @return
     */
    @Override
    public Delegate delegate() {
        return delegate;
    }

    /**
     * 设置委托对象
     *
     * @param delegate
     */
    @Override
    public void delegate(Delegate delegate) {
        this.delegate = delegate;
    }

    /**
     * 是否已激活
     *
     * @return
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * 状态变更
     *
     * @param state
     */
    @Override
    public void stateChanged(int state) {
        active = (state == Socket.SESSION_ON);
        if (delegate != null) {
            delegate.stateChanged(state);
        }
    }

    /**
     * 建立连接
     *
     * @param channel
     */
    @Override
    public void sessionOpen(Channel channel) {
        if (delegate != null) {
            delegate.sessionOpen(channel);
        }
    }

    /**
     * 断开连接
     *
     * @param channel
     */
    @Override
    public void sessionEnd(Channel channel) {
        if (delegate != null) {
            delegate.sessionEnd(channel);
        }
    }

    /**
     * 接收消息
     *
     * @param channel
     * @param message
     */
    @Override
    public void messageReceived(Channel channel, Object message) {
        if (delegate != null) {
            delegate.messageReceived(channel, message);
        }
    }

    /**
     * 产生异常
     *
     * @param exception
     */
    @Override
    public void exceptionCaught(String exception) {
        if (delegate != null) {
            delegate.exceptionCaught(exception);
        }
    }

    @Override
    public final void dispose() {
        checkDisposed();
        delegate = null;
        active = false;
        if (threads != null) {
            threads.shutdownNow();
            threads = null;
        }
        cleanup();
        isDisposed = true;
    }

    /**
     * 覆盖此方法来实现自定义清理逻辑.
     */
    protected void cleanup() {

    }

    /**
     * 检查对象是否已销毁
     */
    protected void checkDisposed() {
        if (isDisposed) {
            throw new IllegalStateException("Object instace has been disposed");
        }
    }
}
