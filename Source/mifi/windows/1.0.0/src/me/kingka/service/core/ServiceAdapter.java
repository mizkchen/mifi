package me.kingka.service.core;

import io.netty.channel.Channel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.kingka.network.Delegate;
import me.kingka.network.Socket;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;

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
    protected String newPassword;
    protected Configuration configuration;
    protected ConfigurationFactory factory;

    public ServiceAdapter() {
        threads = Executors.newCachedThreadPool();
        factory = ConfigurationFactory.getInstance();
        configuration = factory.configure();
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public Delegate delegate() {
        return delegate;
    }

    @Override
    public void delegate(Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void stateChanged(int state) {
        active = (state == Socket.SESSION_ON);
        if (delegate != null) {
            delegate.stateChanged(state);
        }
    }

    @Override
    public void sessionOpen(Channel channel) {
        if (delegate != null) {
            delegate.sessionOpen(channel);
        }
    }

    @Override
    public void sessionEnd(Channel channel) {
        if (delegate != null) {
            delegate.sessionEnd(channel);
        }
    }

    @Override
    public void messageReceived(Channel channel, Object message) {
        if (delegate != null) {
            delegate.messageReceived(channel, message);
        }
    }

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
     * 自定义清理
     */
    protected void cleanup() {

    }

    /**
     * 检查对象是否已销毁
     */
    protected void checkDisposed() {
        if (isDisposed) {
            throw new IllegalStateException("Object instance has been destroyed");
        }
    }
}
