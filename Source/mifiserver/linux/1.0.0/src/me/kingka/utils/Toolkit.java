package me.kingka.utils;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.List;

import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.network.data.Value;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 辅助工具类
 *
 * @author swift.apple
 */
public class Toolkit {

    /**
     * 创建显示数据对象
     *
     * @param file
     * @return
     */
    public static Value makeValue(java.io.File file) {
        Value value = new Value();
        value.setName(file.getName());
        value.setLength(file.length());
        value.setPath(file.getAbsolutePath());
        value.setId(DigestUtils.md5Hex(StringUtils.getBytesUtf8(file.getAbsolutePath())));
        return value;
    }

    /**
     * 密码是否一致
     *
     * @param password
     * @param comparedPassword
     * @return
     */
    public static boolean isValidPassword(String password, String comparedPassword) {
        return comparedPassword.equalsIgnoreCase(DigestUtils.md5Hex(password));
    }

    /**
     * 发送响应
     *
     * @param channel
     * @param kind
     * @param message
     * @param status
     */
    public static void send(Channel channel, String kind, ByteString message, int status) {
        Message.Response.Builder builder = Message.Response.newBuilder();
        builder.setStatus(status);
        builder.setKind(kind);
        if (message == null) {
            builder.setData(ByteString.EMPTY);
        } else {
            builder.setData(message);
        }
        channel.write(builder.build());
        channel.flush();
    }

    /**
     * 是否是端口格式
     *
     * @param host
     * @return
     */
    public static boolean isPort(String host) {
        String regex = "^(0|[1-9]\\d{0,4})$";
        if (host != null) {
            if (host.matches(regex)) {
                int port = Integer.parseInt(host);
                if (port >= 0 && port <= 65535) {
                    return true;
                }
            }
        }
        return false;
    }

}
