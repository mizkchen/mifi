package me.kingka.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 缓冲区
 * Created by zatams on 4/19/16.
 */
public class Buffer {
    private final Logger logger = Logger.getLogger(Buffer.class);
    private final Lock lock = new ReentrantLock();
    private ByteBuf buffer = null;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    private String file;

    /**
     * 创建缓冲区
     *
     * @param initialCapacity 缓冲区大小
     * @return
     */
    public static Buffer create(int initialCapacity) {
        try {
            Buffer buffer = new Buffer(initialCapacity);
            return buffer;
        } catch (Exception ex) {

        }
        return null;
    }

    private Buffer(int initialCapacity) {
        buffer = Unpooled.buffer(initialCapacity);
    }

    /**
     * 写入数据
     *
     * @param bytes 数据字节
     * @return
     */
    public int write(byte[] bytes) {
        int len = 0;
        try {
            lock.lock();
            if (buffer.writableBytes() <= 0 || bytes.length > buffer.writableBytes()) {//发现没有可写入的字节先刷新缓冲区
                flush();
            }
            if (bytes.length > 0 && bytes.length <= buffer.writableBytes()) {
                buffer.writeBytes(bytes);
                len = bytes.length;
            }
        } catch (Exception ex) {
            logger.debug(ex);
        } finally {
            lock.unlock();
        }
        return len;
    }

    /**
     * 刷新缓冲区
     *
     * @param bytes 缓冲区字节
     */
    private void flushBytes(byte[] bytes) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
        } catch (FileNotFoundException e) {
            logger.debug(e);
        } catch (IOException e) {
            logger.debug(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.debug(e);
                }
            }
        }
    }

    /**
     * 清空缓冲区
     */
    public void clear() {
        try {
            lock.lock();
            buffer.clear();
        } catch (Exception ex) {

        } finally {
            lock.unlock();
        }
    }

    /**
     * 清刷新缓冲区
     */
    public void flush() {
        try {
            lock.lock();
            if (buffer.readableBytes() > 0) {
                ByteBuf bfBytes = buffer.readBytes(buffer.readableBytes());
                byte[] bytes = bfBytes.array();
                buffer.clear();
                flushBytes(bytes);
            }
        } catch (Exception ex) {

        } finally {
            lock.unlock();
        }

    }

    /**
     * 销毁缓冲区
     */
    public void dispose() {
        if (buffer != null) {
            ReferenceCountUtil.safeRelease(buffer);
        }
    }
}
