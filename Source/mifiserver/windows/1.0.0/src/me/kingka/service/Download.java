package me.kingka.service;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 下载管理器
 *
 * @author swift.apple
 */
public class Download {
    private final Logger logger = Logger.getLogger(Download.class);
    private final Lock lock = new ReentrantLock();
    private static Download instance = null;

    private Download() {

    }

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized Download create() {
        if (instance == null) {
            instance = new Download();
        }
        return instance;
    }

    /**
     * 读取指定区间的数据
     *
     * @param fileName 文件地址
     * @param offset   偏移量
     * @param len      长度
     * @return
     */
    public byte[] read(String fileName, long offset, long len) {
        lock.lock();
        RandomAccessFile file = null;
        byte[] bytes = null;
        FileChannel channel = null;
        try {
            file = new RandomAccessFile(fileName, "r");
            channel = file.getChannel();
            if (offset >= 0 && offset + len <= file.length()) {
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, len);
                bytes = new byte[(int) len];
                buffer.get(bytes);
            }

        } catch (IOException e) {
            logger.debug(e);
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.debug(e);
                }
            }
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    logger.debug(e);
                }
            }
            lock.unlock();
        }

        return bytes;
    }

}
