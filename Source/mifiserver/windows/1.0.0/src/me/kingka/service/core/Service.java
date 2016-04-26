package me.kingka.service.core;

import me.kingka.network.Delegate;
import me.kingka.network.configuration.Configuration;

/**
 * 服务接口
 *
 * @author swift.apple
 */
public interface Service extends Delegate, Disposable {
    /**
     * 是否已激活
     *
     * @return
     */
    boolean isActive();

    /**
     * 获取委托对象
     *
     * @return
     */
    Delegate delegate();

    /**
     * 设置委托对象
     *
     * @param delegate
     */
    void delegate(Delegate delegate);

    /**
     * 启动服务
     */
    void startup();

    /**
     * 关闭服务
     */
    void shutdown();

    /**
     * 配置
     *
     * @return
     */
    Configuration configuration();

}
