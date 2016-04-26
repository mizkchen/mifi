package me.kingka.service;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;
import org.apache.log4j.Logger;

/**
 * 缓存管理器(每个socket连接会对应于服务端的一个缓存)
 *
 * @author swift.apple
 */
public class Cache {

    private Configuration configuration = null;
    private final Map<String, Buffer> buffers = new ConcurrentHashMap<>();
    private final Lock bufferLock = new ReentrantLock();

    /**
     * 获取新的缓存
     *
     * @param token
     * @param fileName
     * @return
     */
    public Buffer newBuffer(String token, String fileName) {
        try {
            bufferLock.lock();
            String file = String.format("%s%s%s", configuration.getStorage(), File.separator, fileName);
            Buffer buffer = buffers.get(token);
            if (buffer != null) {
                if (!file.equals(buffer.getFile())) {
                    buffer.flush();
                }
                buffer.setFile(file);
                return buffer;
            } else {
                buffer = Buffer.create(10485760);
                buffer.setFile(file);
                buffers.put(token, buffer);
                Logger.getLogger(Cache.class).info(String.format("generate buffer:%s", token));
                return buffer;
            }
        } catch (Exception ex) {

        } finally {
            bufferLock.unlock();
        }

        return null;

    }

    /**
     * 删除缓存
     *
     * @param bufferId 缓冲区ID
     */
    public void removeBuffer(String bufferId) {
        try {
            bufferLock.lock();
            Buffer buffer = buffers.remove(bufferId);
            if (buffer != null) {
                buffer.flush();//删除缓存前刷新数据到本地磁盘
                buffer.dispose();
            }
        } catch (Exception ex) {

        } finally {
            bufferLock.unlock();
        }
    }

    /**
     * 写入数据
     *
     * @param bufferId 缓冲区ID
     * @param bytes 数据字节
     * @return
     */
    public int write(String bufferId, byte[] bytes) {
        Buffer buffer = buffers.get(bufferId);
        if (buffer != null) {
            return buffer.write(bytes);
        }
        return 0;
    }

    /**
     * 刷新缓存
     *
     * @param bufferId 缓冲区ID
     */
    public void flush(String bufferId) {
        Buffer buffer = buffers.get(bufferId);
        if (buffer != null) {
            buffer.flush();
        }
    }

    private Cache() {
        ConfigurationFactory factory = ConfigurationFactory.getInstance();
        configuration = factory.configure();
    }

    private static Cache instance = null;

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized Cache create() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

}
