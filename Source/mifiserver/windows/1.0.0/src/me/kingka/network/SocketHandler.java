package me.kingka.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * socket处理器
 *
 * @author swift.apple
 */
public class SocketHandler extends ChannelInboundHandlerAdapter {

    private final Delegate delegate;

    public SocketHandler(Delegate delegate) {
        this.delegate = delegate;
    }

    /**
     * 读取消息
     *
     * @param ctx 上下文
     * @param msg 消息
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (delegate != null) {
                Channel channel = ctx.channel();
                delegate.messageReceived(channel, msg);
            }
        } catch (Exception ex) {

        } finally {
            ReferenceCountUtil.safeRelease(msg);
        }
    }

//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }

    /**
     * 建立连接
     *
     * @param ctx 上下文
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            if (delegate != null) {
                Channel channel = ctx.channel();
                delegate.sessionOpen(channel);
            }
        } catch (Exception ex) {

        }
        super.channelActive(ctx);
    }

    /**
     * 断开连接
     *
     * @param ctx 上下文
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            if (delegate != null) {
                Channel channel = ctx.channel();
                delegate.sessionEnd(channel);
            }
        } catch (Exception ex) {

        }
        super.channelInactive(ctx);
    }

    /**
     * 产生异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            if (delegate != null) {
                delegate.exceptionCaught(cause.getLocalizedMessage());
            }
        } catch (Exception ex) {

        }
        super.exceptionCaught(ctx, cause);
    }
}
