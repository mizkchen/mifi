package me.kingka.service;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import java.io.File;
import java.time.Clock;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;
import me.kingka.service.core.Disposable;
import me.kingka.service.core.Service;
import me.kingka.ui.FileIcon;
import me.kingka.ui.model.DisplayValue;
import me.kingka.ui.model.DisplayValueModel;
import static me.kingka.utils.Toolkit.build;
import static me.kingka.utils.Toolkit.buildBlock;
import static me.kingka.utils.Toolkit.guessIcon;
import static me.kingka.utils.Toolkit.information;
import static me.kingka.utils.Toolkit.stage;
import static me.kingka.utils.Toolkit.updateProgress;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 文件下载管理器
 *
 * @author swift.apple
 */
public class Download implements Disposable {

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
     * 是否正在下载
     */
    private boolean active;
    /**
     * 已下载的数据量
     */
    private long downloaded;
    /**
     * 正在下载的数据块
     */
    private Block currentBlock;
    /**
     * 正在下载的文件
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
     * 缓冲区
     */
    private Buffer buffer = null;

    /**
     * 是否已激活
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 下载
     *
     * @param value
     */
    public void download(final DisplayValue value) {
        if (value == null) {
            return;
        }
        if (model.isEmpty()) {
            return;
        }
        if (!active) {
            active = true;
            current = value;
            downloaded = 0;
            stopped = false;
            thread.submit(() -> {
                String file = String.format("%s%s%s", configuration.getStorage(), File.separator, current.getName());
                current.setPath(file);
                buffer.setFile(file);
                current.setBeginTime(Clock.systemUTC().millis());
                Platform.runLater(() -> {
                    updateProgress(current, 0, locale.get("ui.controller.percent.0"));
                    current.setIcon(FileIcon.DOWNLOADING.description());
                });
                if (!queue.isEmpty()) {
                    queue.clear();
                }
                buildBlock(queue, value, Common.BLOCK_SIZE);
                downloadBlock();
            });
        }
    }

    /**
     * 下载数据块
     */
    private void downloadBlock() {
        thread.submit(() -> {
            if (!queue.isEmpty() && active) {
                currentBlock = queue.poll();
                if (currentBlock != null && active) {
                    ByteBuf buf = Unpooled.buffer();
                    byte[] passwords = StringUtils.getBytesUtf8(DigestUtils.md5Hex(service.configuration().getPassword()));// 密码
                    buf.writeInt(passwords.length);
                    buf.writeBytes(passwords);
                    byte[] names = StringUtils.getBytesUtf8(current.getId());// id
                    buf.writeInt(names.length);
                    buf.writeBytes(names);
                    buf.writeLong(currentBlock.offset);// 数据块偏移量
                    buf.writeLong(currentBlock.length);// 数据块大小
                    byte[] data = buf.array();
                    ReferenceCountUtil.release(buf);
                    ByteString content = ByteString.copyFrom(data);
                    service.send(build(Common.DOWNLOAD_FILES, content));
                }
            }
        });

    }

    /**
     * 国际化
     */
    private I18n locale = null;
    /**
     *
     */
    private Configuration configuration = null;

    /**
     * 下载管理器
     *
     * @param model
     * @param history
     * @param service
     */
    public Download(DisplayValueModel model, DisplayValueModel history, Service service) {
        this.model = model;
        this.history = history;
        this.service = service;
        configuration = ConfigurationFactory.getInstance().configure();
        locale = I18n.getInstance();
        queue = new ConcurrentLinkedDeque<>();
        thread = Executors.newCachedThreadPool();
        buffer = Buffer.create(Common.BUFFER_SIZE);
    }

    /**
     * 是否应该取消下载
     */
    private boolean stopped = false;

    /**
     * 取消下载
     */
    public void stop() {
        if (active && current != null) {
            active = false;
            queue.clear();
            stopped = true;
            reset(current);
        }
    }

    /**
     * 执行下一个下载
     */
    private void next() {
        if (!model.isEmpty() && active) {// 还有剩余任务
            thread.submit(() -> {
                Optional<DisplayValue> value = model.get(0);
                if (value.isPresent()) {
                    active = false;
                    download(value.get());
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
            case Common.DOWNLOAD_FILES:
                ByteString byteString = response.getData();
                int status = response.getStatus();
                if (status == Common.SUCCESS) {
                    if (byteString == null || byteString.isEmpty()) {
                        return;
                    }
                    downloaded += currentBlock.length;
                    java.nio.ByteBuffer byteBuffer = byteString.asReadOnlyByteBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    onComplete(bytes, current, downloaded);
                } else {
                    onError(current, downloaded, status);
                }
                break;
        }

    }

    /**
     * 重置状态
     *
     * @param value
     */
    private void reset(DisplayValue value) {
        FileIcon fileIcon = guessIcon(value.getName());
        current.setIcon(fileIcon.description());
        current.setBeginTime(Clock.systemUTC().millis());
        current.setElapsed("0秒");
        updateProgress(value, 0, locale.get("ui.controller.percent.4"));
    }

    /**
     * 错误处理
     *
     * @param value
     * @param downloaded
     * @param status
     */
    private void onError(DisplayValue value, long downloaded, int status) {
        Platform.runLater(() -> {
            if (stopped) {
                return;
            }
            value.setIcon(FileIcon.ERROR.description());
            updateProgress(value, downloaded, locale.get("ui.controller.percent.2"));
            model.remove(value.getId());
            history.append(value);
            information(locale.get("ui.information.title"), locale.get(String.format("error.%s", status)), stage, null);
            next();
        });
    }

    /**
     * 一个数据块下载完成后的处理
     *
     * @param bytes 数据块字节
     * @param value 下载对应的文件
     * @param downloaded 已下载的总字节
     */
    private void onComplete(byte[] bytes, DisplayValue value, long downloaded) {
        Platform.runLater(() -> {
            if (stopped) {
                return;
            }
            updateProgress(value, downloaded, null);
            buffer.write(bytes);
            if (queue.isEmpty()) {
                current.setIcon(FileIcon.COMPLETE.description());
                model.remove(current.getId());
                history.append(current);
                buffer.flush();
            }
            if (!queue.isEmpty()) {
                downloadBlock();
            } else {
                next();
            }
        });
    }

    /**
     * 刷新当前缓冲区
     */
    public void flush() {
        buffer.flush();
    }

    @Override
    public void dispose() {
        if (thread != null) {
            thread.shutdownNow();
        }
        if (buffer != null) {
            buffer.dispose();
        }
    }

}
