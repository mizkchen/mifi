package me.kingka.service;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Clock;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.service.core.Disposable;
import me.kingka.service.core.Service;
import me.kingka.ui.FileIcon;
import me.kingka.ui.model.DisplayValue;
import me.kingka.ui.model.DisplayValueModel;
import me.kingka.utils.Toolkit;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 * 文件上传管理器
 *
 * @author swift.apple
 */
public class Upload implements Disposable {

    private final Logger logger = Logger.getLogger(Upload.class);
    /**
     * 文件集合
     */
    private DisplayValueModel model = null;
    /**
     * 历史纪录集合
     */
    private DisplayValueModel history = null;

    /**
     * socket 服务
     */
    private Service service = null;
    /**
     * 是否正在上传
     */
    private boolean active;
    /**
     * 已上传的数据量
     */
    private long uploaded;
    /**
     * 正在上传的数据块
     */
    private Block currentBlock;
    /**
     * 正在上传的文件
     */
    private DisplayValue current;
    /**
     * 数据块队列
     */
    private Queue<Block> queue = null;
    /**
     * 线程池
     */
    private ExecutorService thread = null;

    /**
     * 是否已激活
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 读取数据块数据
     *
     * @param path 文件地址
     * @param offset 偏移
     * @param len 大小
     * @return
     */
    private byte[] readBlockData(String path, long offset, long len) {
        RandomAccessFile file = null;
        FileChannel channel = null;
        byte[] bytes = null;
        try {
            file = new RandomAccessFile(path, "r");
            channel = file.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, len);
            bytes = new byte[(int) len];
            buffer.get(bytes);
        } catch (Exception ex) {
            logger.debug(ex);
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
        }
        return bytes;
    }

    /**
     * 上传
     *
     * @param value 文件
     */
    public void upload(final DisplayValue value) {
        if (value == null) {
            return;
        }
        if (model.isEmpty()) {
            return;
        }
        if (!active) {
            active = true;
            current = value;
            uploaded = 0;
            stopped = false;
            thread.submit(() -> {
                current.setBeginTime(System.currentTimeMillis());
                Platform.runLater(() -> {
                    Toolkit.updateProgress(current, 0, locale.get("ui.controller.percent.0"));
                    current.setIcon(FileIcon.UPLOADING.description());
                });
                if (!queue.isEmpty()) {
                    queue.clear();
                }
                Toolkit.buildBlock(queue, value, Common.BLOCK_SIZE);
                uploadBlock();
            });
        }
    }

    /**
     * 上传数据块
     */
    private void uploadBlock() {
        thread.submit(() -> {
            if (!queue.isEmpty() && active) {
                currentBlock = queue.poll();
                if (currentBlock != null && active) {
                    byte[] bytes = readBlockData(current.getPath(), currentBlock.offset, currentBlock.length);//从文件中读取数据块
                    if (bytes == null) {
                        return;
                    }
                    ByteBuf buf = Unpooled.buffer();
                    byte[] passwords = StringUtils.getBytesUtf8(DigestUtils.md5Hex(service.configuration().getPassword()));//密码
                    buf.writeInt(passwords.length);
                    buf.writeBytes(passwords);
                    byte[] names = StringUtils.getBytesUtf8(current.getName());//文件名
                    buf.writeInt(names.length);
                    buf.writeBytes(names);
                    buf.writeLong(current.getLength());//总的文件大小
                    if (currentBlock.first) {
                        buf.writeByte(1);//第一个数据块
                    } else {
                        buf.writeByte(0);//默认值
                    }
                    buf.writeLong(currentBlock.length);//数据块大小
                    buf.writeBytes(bytes);//数据块数据
                    byte[] data = buf.array();
                    ReferenceCountUtil.release(buf);
                    ByteString content = ByteString.copyFrom(data);
                    service.send(Toolkit.build(Common.UPLOAD_FILES, content));
                }
            }
        });

    }

    /**
     * 国际化
     */
    private final I18n locale;

    /**
     * 上传管理器
     *
     * @param model
     * @param history
     * @param service
     */
    public Upload(DisplayValueModel model, DisplayValueModel history, Service service) {
        this.model = model;
        this.history = history;
        this.service = service;
        locale = I18n.getInstance();
        queue = new ConcurrentLinkedDeque<>();
        thread = Executors.newCachedThreadPool();
    }

    /**
     * 是否应该取消上传
     */
    private boolean stopped = false;

    /**
     * 取消上传
     */
    public void stop() {
        if (active && current != null) {
            active = false;
            stopped = true;
            queue.clear();
            reset(current);
        }

    }

    /**
     * 执行下一个上传
     */
    private void next() {
        if (!model.isEmpty() && active) {//还有剩余任务
            thread.submit(() -> {
                Optional<DisplayValue> value = model.get(0);
                if (value.isPresent()) {
                    active = false;
                    upload(value.get());
                }
            });
        } else {
            active = false;
        }
    }

    /**
     * socket回调
     *
     * @param channel
     * @param response
     */
    public void messageReceived(Channel channel, Message.Response response) {
        String kind = response.getKind();
        switch (kind) {
            case Common.UPLOAD_FILES:
                int status = response.getStatus();
                if (status == Common.SUCCESS) {
                    uploaded += currentBlock.length;
                    onComplete(current, uploaded);
                } else {
                    onError(current, uploaded, status);
                }
                break;
            case Common.FLUSH_UPLOAD_CACHE:
                next();
                break;
        }

    }

    /**
     * 错误处理
     *
     * @param value
     * @param uploaded
     * @param status
     */
    private void onError(DisplayValue value, long uploaded, int status) {
        Platform.runLater(() -> {
            if (stopped) {
                return;
            }
            value.setIcon(FileIcon.ERROR.description());
            Toolkit.updateProgress(value, uploaded, locale.get("ui.controller.percent.1"));
            model.remove(value.getId());
            history.append(value);
            Toolkit.information(locale.get("ui.information.title"), locale.get(String.format("error.%s", status)), Toolkit.stage, null);
            next();
        });
    }

    /**
     * 重置状态
     *
     * @param value
     */
    private void reset(DisplayValue value) {
        FileIcon fileIcon = Toolkit.guessIcon(value.getName());
        current.setIcon(fileIcon.description());
        current.setBeginTime(Clock.systemUTC().millis());
        current.setElapsed("0秒");
        Toolkit.updateProgress(value, 0, locale.get("ui.controller.percent.3"));
    }

    /**
     * 一个数据块上传完成后的处理
     *
     * @param value
     * @param uploaded
     */
    private void onComplete(DisplayValue value, long uploaded) {
        Platform.runLater(() -> {
            if (stopped) {
                return;
            }
            Toolkit.updateProgress(value, uploaded, null);
            if (queue.isEmpty()) {
                current.setIcon(FileIcon.COMPLETE.description());
                model.remove(current.getId());
                history.append(current);
            }
            if (!queue.isEmpty()) {
                uploadBlock();
            } else {
                service.send(Toolkit.build(Common.FLUSH_UPLOAD_CACHE, ByteString.EMPTY));//刷新服务器的上传缓存(将服务器的缓存数据写入磁盘)
            }
        });

    }

    @Override
    public void dispose() {
        if (thread != null) {
            thread.shutdownNow();
        }
    }

}
