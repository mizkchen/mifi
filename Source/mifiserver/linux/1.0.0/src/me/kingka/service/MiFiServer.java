package me.kingka.service;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.network.Socket;
import me.kingka.network.SocketDelegate;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;
import me.kingka.network.data.Value;
import me.kingka.network.data.ValueModel;
import me.kingka.service.core.Disposable;
import me.kingka.service.core.Service;
import me.kingka.service.core.SocketService;
import me.kingka.utils.Toolkit;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileSystemUtils;
import org.apache.log4j.Logger;
import org.omg.SendingContext.RunTime;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

import static me.kingka.utils.Toolkit.isValidPassword;
import static me.kingka.utils.Toolkit.send;

/**
 * 服务器
 */
public class MiFiServer extends SocketDelegate {
    private final Logger logger = Logger.getLogger(MiFiServer.class);
    private I18n locale = null;
    private Cache cache = null;
    private Download download = null;
    private Service service = null;
    private Configuration configuration = null;
    private ConfigurationFactory factory = null;
    private ValueModel model = null;

    private static MiFiServer instance = null;

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static MiFiServer getInstance() {
        if (instance == null) {
            instance = new MiFiServer();
        }
        return instance;
    }

    private MiFiServer() {
        cache = Cache.create();
        download = Download.create();
        service = SocketService.getInstance();
        locale = I18n.getInstance();
        factory = ConfigurationFactory.getInstance();
        configuration = factory.configure();
        model = ValueModel.getInstance();
    }

    /**
     * 加载数据
     *
     * @return
     */
    public MiFiServer load() {
        List<String> files = configuration.getFiles();
        if (files != null) {
            files.stream().forEach(file -> {
                try {
                    File f = new File(file);
                    if (f.exists()) {
                        model.append(Toolkit.makeValue(f));
                    }
                } catch (Exception ex) {
                    logger.debug(ex);
                }

            });
        }
        return this;
    }

    /**
     * 运行
     *
     * @return
     */
    public MiFiServer run() {
        if (service != null) {
            service.delegate(this);
            service.startup();
        }
        return this;
    }


    /**
     * 获取IP
     *
     * @param channel
     * @return
     */
    private String getIp(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        return address.getHostString();
    }

    /**
     * 状态变更
     *
     * @param state
     */
    @Override
    public void stateChanged(int state) {
        final int port = configuration.getPort();
        if (state == Socket.SESSION_OFF) {
            logger.info(locale.get("text.002"));
        } else {
            logger.info(String.format(locale.get("text.004"), port));
        }
    }

    /**
     * 解析下载
     *
     * @param channel
     * @param request
     */
    private void handleRequestDownload(Channel channel, Message.Request request) {
        String kind = request.getKind();
        ByteString byteString = request.getData();
        if (byteString == null || byteString.isEmpty()) {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
            return;
        }
        java.nio.ByteBuffer buffer = byteString.asReadOnlyByteBuffer();
        String password = null;
        String id = null;
        long offset = -1;
        long len = -1;
        try {
            if (buffer.remaining() >= 4) {
                int passwordLength = buffer.getInt();//密码字节数
                if (buffer.remaining() >= passwordLength) {
                    byte[] passwordBytes = new byte[passwordLength];
                    buffer.get(passwordBytes);
                    password = StringUtils.newStringUtf8(passwordBytes);//密码
                    if (buffer.remaining() >= 4) {
                        int idLength = buffer.getInt();//id长度
                        if (buffer.remaining() >= idLength) {
                            byte[] idBytes = new byte[idLength];//id
                            buffer.get(idBytes);
                            id = StringUtils.newStringUtf8(idBytes);
                            if (buffer.remaining() >= 8) {
                                offset = buffer.getLong();//偏移
                                if (buffer.remaining() >= 8) {
                                    len = buffer.getLong();//长度
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {

        }
        if (password == null || id == null || offset < 0 || len < 0) {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
            return;
        }
        String realPassword = DigestUtils.md5Hex(configuration.getPassword());
        if (!password.equals(realPassword)) {
            send(channel, kind, null, Common.PASSWORD_INVALID);
            return;
        }
        Optional<Value> optional = model.get(id);
        if (!optional.isPresent()) {
            send(channel, kind, null, Common.FILE_NOT_FOUND);
            return;
        }
        String file = optional.get().getPath();
        byte[] bytes = download.read(file, offset, len);
        if (bytes == null) {
            send(channel, kind, null, Common.FILE_NO_DATA);
            return;
        }
        ByteString c = ByteString.copyFrom(bytes);
        send(channel, kind, c, Common.SUCCESS);
    }

    /**
     * 刷新
     *
     * @param channel
     * @param request
     */
    private void handleFlush(Channel channel, Message.Request request) {
        String kind = request.getKind();
        String bufferId = DigestUtils.md5Hex(getIp(channel));
        cache.flush(bufferId);
        send(channel, kind, null, Common.SUCCESS);
    }

    /**
     * 解析上传
     *
     * @param channel
     * @param request
     */
    private void handleRequestUpload(Channel channel, Message.Request request) {
        String kind = request.getKind();
        ByteString byteString = request.getData();
        if (byteString != null && !byteString.isEmpty()) {
            java.nio.ByteBuffer block = byteString.asReadOnlyByteBuffer();
            String fileName = null;
            long blockTotalSize = -1;
            long blockSize = -1;
            byte[] blockData = null;
            byte blockPosition = 0;
            String password = null;
            try {
                //解析数据
                if (block.remaining() >= 4) {
                    int passwordLength = block.getInt();//密码字节数
                    if (block.remaining() >= passwordLength) {
                        byte[] passwordBytes = new byte[passwordLength];
                        block.get(passwordBytes);
                        password = StringUtils.newStringUtf8(passwordBytes);//密码
                        if (block.remaining() >= 4) {
                            int fileNameLength = block.getInt();//文件名字节数
                            if (block.remaining() >= fileNameLength) {
                                byte[] bytes = new byte[fileNameLength];
                                block.get(bytes);
                                fileName = StringUtils.newStringUtf8(bytes);//文件名
                                if (block.remaining() >= 8) {
                                    blockTotalSize = block.getLong();//数据块总大小
                                    if (block.remaining() >= 1) {
                                        blockPosition = block.get();//数据块块位置
                                        if (block.remaining() >= 8) {
                                            blockSize = block.getLong();//数据块长度
                                            if (block.remaining() >= blockSize) {
                                                blockData = new byte[(int) blockSize];//数据
                                                block.get(blockData);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {

            }
            if (password == null || fileName == null || blockTotalSize == -1 || blockData == null || blockSize == -1) {
                send(channel, kind, null, Common.PARAM_IS_MISSING);
                return;
            }
            String password2 = DigestUtils.md5Hex(configuration.getPassword());
            if (!password2.equals(password)) {
                send(channel, kind, null, Common.PASSWORD_INVALID);
                return;
            }
            long diskFreeSpace = 0;
            try {
                diskFreeSpace = FileSystemUtils.freeSpaceKb(configuration.getStorage());
                diskFreeSpace *= 1024;
            } catch (IOException e) {
            }
            if (blockSize >= diskFreeSpace || blockTotalSize >= diskFreeSpace) {
                send(channel, kind, null, Common.NO_FREE_SPACE);
                return;
            }
            String bufferId = DigestUtils.md5Hex(getIp(channel));
            Buffer buffer = cache.newBuffer(bufferId, fileName);
            if (buffer == null) {
                send(channel, kind, null, Common.NO_ENOUGH_BUFF);
                return;
            }
            if (blockPosition == 1) {//第一个数据包
                boolean success = true;
                try {
                    String path = buffer.getFile();
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception ex) {
                    success = false;
                } finally {
                    if (!success) {
                        send(channel, kind, null, Common.FILE_UPLOAD_FAIL);
                        return;
                    }
                }
                buffer.clear();//删除上一次的剩余数据
            }
            int bytes = cache.write(bufferId, blockData);
            if (bytes > 0) {
                send(channel, kind, null, Common.SUCCESS);
            } else {
                send(channel, kind, null, Common.FILE_UPLOAD_FAIL);
            }
        } else {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
        }
    }

    /**
     * 解析文件列表
     *
     * @param channel
     * @param request
     */
    private void handleList(Channel channel, Message.Request request) {
        ByteString pass = request.getData();
        String kind = request.getKind();
        if (pass != null && pass.isValidUtf8()) {
            String password = pass.toStringUtf8();
            if (!isValidPassword(configuration.getPassword(), password)) {
                send(channel, kind, null, Common.PASSWORD_INVALID);
            } else {
                String jsonString = JSON.toJSONString(model.data());
                ByteString msg = ByteString.copyFromUtf8(jsonString);
                send(channel, kind, msg, Common.SUCCESS);
            }
        } else {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
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
        if (message instanceof Message.Request) {
            Message.Request request = (Message.Request) message;
            String kind = request.getKind();
            if (kind == null) {
                return;
            }
            switch (kind) {
                case Common.LIST_FILES:
                    handleList(channel, request);
                    break;
                case Common.DOWNLOAD_FILES:
                    handleRequestDownload(channel, request);
                    break;
                case Common.UPLOAD_FILES:
                    handleRequestUpload(channel, request);
                    break;
                case Common.FLUSH_UPLOAD_CACHE:
                    handleFlush(channel, request);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 产生异常
     *
     * @param exception
     */
    @Override
    public void exceptionCaught(String exception) {
        logger.debug(exception);
    }

    /**
     * 建立连接
     *
     * @param channel
     */
    @Override
    public void sessionOpen(Channel channel) {
        String host = getIp(channel);
        logger.info(String.format("%s connected to server", host));
    }

    /**
     * 断开连接
     *
     * @param channel
     */
    @Override
    public void sessionEnd(Channel channel) {
        String host = getIp(channel);
        logger.info(String.format("%s disconnected from server", host));
        String bufferId = DigestUtils.md5Hex(host);
        //断开连接后释放服务器缓存
        cache.removeBuffer(bufferId);
        logger.info(String.format("remove buffer while disconnected:%s", bufferId));
    }

}
