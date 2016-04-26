package me.kingka.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
    public static final int SESSION_ON = 1;
    public static final int SESSION_OFF = 2;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private static Socket instance;

    /**
     * 获取socket唯一实例
     *
     * @return
     */
    public static synchronized Socket getInstance() {
        if (instance == null) {
            instance = new Socket();
        }
        return instance;
    }

    private int port = 0;

    /**
     * 端口
     *
     * @return
     */
    public int port() {
        return port;
    }

    /**
     * socket委托
     */
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
     * 是否已激活
     *
     * @return
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 开启socket
     *
     * @param port socket监听端口
     */
    public void open(int port) {
        if (!isActive()) {
            this.port = port;
            try {
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                ServerBootstrap server = new ServerBootstrap();
                server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        ch.pipeline().addLast("protobufDecoder", new ProtobufDecoder(Message.Request.getDefaultInstance()));
                        ch.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                        ch.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
                        ch.pipeline().addLast("handler", new SocketHandler(delegate));
                    }
                });
                ChannelFuture future = server.bind(port).sync();
                active = true;
                debug(String.format("server is running on %d", port));
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
                close();
            }
        } else {
            debug("server is already running");
        }
    }

    /**
     * 关闭socket
     */
    public void close() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        if (isActive()) {
            active = false;
            debug("server has been shutdown");
            if (delegate != null) {
                delegate.stateChanged(SESSION_OFF);
            }
        }
    }
}
