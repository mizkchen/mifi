package me.kingka.utils;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.ui.FileIcon;
import me.kingka.ui.model.DisplayValue;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 辅助工具类
 *
 * @author swift.apple
 */
public class Toolkit {

    private static I18n locale = null;
    private static List<String> videos = null;
    private static List<String> musics = null;
    private static List<String> applications = null;
    private static List<String> pics = null;

    static {
        locale = I18n.getInstance();
        videos = Arrays.asList(
                "asf", "avi", "wm", "wmv", "ram", "rm", "rmvb", "rp", "rpm", "rt", "smil", "scm", "m1v", "m2v", "m2p", "m2ts",
                "m1v", "m2v", "m2p", "m2ts", "mp2v", "mpe", "mpeg", "mpeg1", "mpeg2", "mpg", "mpv2", "pss", "pva", "tp", "tpr",
                "ts", "m4b", "m4r", "m4p", "m4v", "mp4", "mpeg4", "3g2", "3gp", "3gp2", "3gpp", "mov", "qt", "flv", "f4v", "swf",
                "hlv", "amv", "csf", "divx", "evo", "mkv", "mod", "pmp", "vp6", "bik", "mts", "xvx", "xv", "xlmv", "ogm", "ogv",
                "ogx"
        );
        musics = Arrays.asList(
                "aac", "ac3", "acc", "aiff", "amr", "ape", "au", "cda", "dts", "flac", "m1a", "m2a", "m4a", "mka", "mp2", "mp3",
                "mpa", "mpc", "ra", "tta", "wav", "wma", "mwv", "mid", "midi", "ogg", "oga"
        );
        applications = Arrays.asList("com", "exe", "msi", "bat", "cmd", "pkg", "app", "deb", "rpm", "apk", "ipa");

        pics = Arrays.asList("png", "jpg", "gif", "bmp", "icns", "svg", "ico");
    }
    public static Stage stage;

    /**
     * 文件大小单位转换
     *
     * @param length 文件大小(字节)
     * @return
     */
    public static String unitOfFile(long length) {
        int level = 0;
        double len = length;
        while (len >= 1024) {
            if (level > 4) {
                break;
            }
            level++;
            len /= 1024;
        }
        String unit = "Bytes";
        switch (level) {
            case 1:
                unit = "KB";
                break;
            case 2:
                unit = "MB";
                break;
            case 3:
                unit = "GB";
                break;
            case 4:
                unit = "TB";
                break;
        }
        return String.format("%.2f%s", len, unit);
    }

    /**
     * 根据文件名获得文件图标
     *
     * @param name
     * @return
     */
    public static FileIcon guessIcon(String name) {
        String extension = "";
        if (name.contains(".")) {
            int index = name.lastIndexOf(".");
            extension = name.substring(index + 1);
        }
        if (videos.contains(extension)) {
            return FileIcon.VIDEO;
        } else if (musics.contains(extension)) {
            return FileIcon.MUSIC;
        } else if (applications.contains(extension)) {
            return FileIcon.APPLICATIONS;
        } else if (pics.contains(extension)) {
            return FileIcon.PICTURE;
        }
        return FileIcon.FILE;
    }

    /**
     * 获取文件图标的显示名称
     *
     * @param type 文件图标
     * @return
     */
    public static String guessIconDisplayName(FileIcon type) {
        return locale.get(type.description());
    }

    /**
     * 弹出消息确认框
     *
     * @param title 标题
     * @param message 消息
     * @param stage 窗口
     * @param callback 回调
     */
    public static void confirm(String title, String message, Stage stage, Consumer<ButtonType> callback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(message);
        if (callback != null) {
            alert.showAndWait().ifPresent(callback);
        } else {
            alert.showAndWait();
        }

    }

    /**
     * 弹出消息框
     *
     * @param title 标题
     * @param message 消息
     * @param stage 窗口
     * @param callback 回调
     */
    public static void information(String title, String message, Stage stage, Consumer<ButtonType> callback) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(message);
        if (callback != null) {
            alert.showAndWait().ifPresent(callback);
        } else {
            alert.showAndWait();
        }
    }

    /**
     * 创建显示数据对象
     *
     * @param file
     * @return
     */
    public static DisplayValue makeDisplayValue(java.io.File file) {
        DisplayValue value = new DisplayValue();
        value.setName(file.getName());
        value.setLength(file.length());
        value.setSize(unitOfFile(file.length()));
        value.setPath(file.getAbsolutePath());
        value.setId(DigestUtils.md5Hex(StringUtils.getBytesUtf8(file.getAbsolutePath())));
        FileIcon fileIcon = guessIcon(file.getName());
        value.setIcon(fileIcon.description());
        value.setCatalog(guessIconDisplayName(fileIcon));
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

    /**
     * 是否是有效的缓存
     *
     * @param numberic
     * @return
     */
    public static boolean isValidCache(String numberic) {
        String regex = "^(0|[1-9]\\d{0,4})$";
        if (numberic != null) {
            if (numberic.matches(regex)) {
                int port = Integer.parseInt(numberic);
                if (port >= 1 && port <= 10) {
                    return true;
                }
            }
        }
        return false;
    }
}
