package me.kingka.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import me.kingka.message.Message;
import org.apache.log4j.Logger;

/**
 * socket
 *
 * @author swift.apple
 */
public class Socket {
    /**
     * 连接建立
     */
    public static final int SESSION_ON = 1;
    /**
     * 连接断开
     */
    public static final int SESSION_OFF = 2;
    private EventLoopGroup workerGroup;
    private static Socket instance;

    public Delegate delegate;
    protected boolean active = false;
    private final Logger logger = Logger.getLogger(Socket.class);

    public Socket() {

    }

    /**
     * 输出调试信息
     *
     * @param msg
     */
    public final void debug(String msg) {
        logger.debug(msg);
    }

    /**
     * 输出调试信息
     *
     * @param throwable
     */
    public final void debug(Throwable throwable) {
        logger.debug(throwable);
    }

    /**
     * socket是否已激活
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 获取socket类唯一实例
     *
     * @return
     */
    public static synchronized Socket getInstance() {
        if (instance == null) {
            instance = new Socket();
        }
        return instance;
    }

    private Channel channel;

    /**
     * 连接
     *
     * @param host
     * @param port
     */

    public void connect(String host, int port) {
        if (!isActive()) {
            try {
                workerGroup = new NioEventLoopGroup();
                Bootstrap client = new Bootstrap();
                client.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<NioSocketChannel>() {

                            @Override
                            protected void initChannel(NioSocketChannel channel) throws Exception {
                                channel.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                                channel.pipeline().addLast("protobufDecoder", new ProtobufDecoder(Message.Response.getDefaultInstance()));
                                channel.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                                channel.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
                                channel.pipeline().addLast("handler", new SocketHandler(delegate));
                            }

                        });
                ChannelFuture future = client.connect(host, port).sync();
                active = true;
                debug(String.format("Connection has been esablished on %s:%d", host, port));
                channel = future.channel();
                if (delegate != null) {
                    delegate.stateChanged(SESSION_ON);
                }
                future.channel().closeFuture().sync();
            } catch (Exception ex) {
                debug(ex.getLocalizedMessage());
                if (delegate != null) {
                    delegate.exceptionCaught(String.format("[ERROR]:%s", ex.getLocalizedMessage()));
                }
            } finally {
                disconnect();
            }
        } else {
            debug("duplicated connection");
        }
    }

    /**
     * 发送请求
     *
     * @param request
     */
    public void send(Message.Request request) {
        if (channel != null && channel.isWritable()) {
            channel.write(request);
            channel.flush();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        if (isActive()) {
            active = false;
            debug("disconnect from server");
            if (delegate != null) {
                delegate.stateChanged(SESSION_OFF);
            }
        }
    }

}
