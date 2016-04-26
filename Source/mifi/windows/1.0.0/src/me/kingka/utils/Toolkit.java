package me.kingka.utils;


import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.text.NumberFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.service.Block;
import me.kingka.ui.FileIcon;
import me.kingka.ui.model.DisplayValue;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 辅助工具类
 *
 * @author swift.apple
 */
public class Toolkit {
    private static final I18n locale;
    private static final List<String> videos;
    private static final List<String> musics;
    private static final List<String> applications;
    private static final List<String> pics;

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

    /**
     * 窗口对象(全局)
     */
    public static Stage stage;

    /**
     * 文件大小单位转换
     *
     * @param length 文件大小(字节)
     * @return
     */
    public static String fileUnit(long length) {
        int level = 0;
        double len = length;
        while (len >= 1024) {
            if (level > 4) {
                break;
            }
            level++;
            len /= 1024;
        }
        String unit = "B";
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
        NumberFormat numberFormatter = NumberFormat.getNumberInstance();
        numberFormatter.setMaximumFractionDigits(2);
        return String.format("%s%s", numberFormatter.format(len), unit);
    }

    /**
     * 依据文件名获得类别
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
     * 文件图标显示名称
     *
     * @param icon
     * @return
     */
    public static String guessIconDisplayName(FileIcon icon) {
        return locale.get(icon.description());
    }

    /**
     * 弹出确认框
     *
     * @param title    标题
     * @param message  消息
     * @param stage    窗口
     * @param callback 回调接口
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
     * @param title    标题
     * @param message  消息
     * @param stage    窗口
     * @param callback 回调接口
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
     * @param id
     * @param name
     * @param length
     * @return
     */
    public static DisplayValue makeDisplayValue(String id, String name, long length) {
        DisplayValue value = new DisplayValue();
        value.setId(id);
        value.setName(name);
        value.setPath("");
        value.setLength(length);
        FileIcon icon = guessIcon(name);
        value.setCatalog(guessIconDisplayName(icon));
        value.setSize(fileUnit(length));
        value.setIcon(icon.description());
        value.setBeginTime(Clock.systemUTC().millis());
        updateProgress(value, 0, locale.get("ui.controller.percent.0"));
        return value;
    }

    /**
     * 创建显示数据对象
     *
     * @param file
     * @return
     */
    public static DisplayValue makeDisplayValue(java.io.File file) {
        DisplayValue value = new DisplayValue();
        String path = file.getAbsolutePath();
        value.setName(file.getName());
        value.setPath(path);
        value.setLength(file.length());
        FileIcon fileIcon = guessIcon(file.getName());
        value.setCatalog(guessIconDisplayName(fileIcon));
        value.setSize(fileUnit(file.length()));
        value.setId(DigestUtils.md5Hex(path));
        value.setIcon(fileIcon.description());
        value.setBeginTime(Clock.systemUTC().millis());
        updateProgress(value, 0, locale.get("ui.controller.percent.0"));
        return value;
    }


    /**
     * 更新显示数据对象
     *
     * @param value
     * @param loaded 已加载字节数
     * @param status 百分比字段显示值
     */
    public static void updateProgress(DisplayValue value, long loaded, String status) {
        value.setCurrent(loaded);
        long length = value.getLength();
        double p = length == 0 ? 0 : (double) loaded / length;
        value.setProgress(p);
        if (status == null) {
            double p100 = 100 * p > 100.0 ? 100.0 : 100 * p;
            p100 = p100 < 0.0 ? 0.0 : p100;
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            numberFormatter.setMaximumFractionDigits(2);
            String percent = String.format("%s%s", numberFormatter.format(p100), "%");
            value.setPercent(percent);
        } else {
            value.setPercent(status);
        }
        long duration = Clock.systemUTC().millis() - value.getBeginTime();
        if (duration < 0) {
            duration = 0;
        }
        value.setElapsed(duration / 1000 + "秒");
        long speed = 0;
        if (duration > 0) {
            speed = loaded * 1000 / duration;
        }
        String speedString = fileUnit(speed);
        value.setSpeed(String.format("%s/s", speedString));
    }

    /**
     * 是否是IP地址格式
     *
     * @param host
     * @return
     */
    public static boolean isIP(String host) {
        String regex = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
        if (host != null) {
            return host.matches(regex);
        }
        return false;
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
     * 发送数据到服务端
     *
     * @param channel
     * @param kind
     * @param message
     */
    public static void send(Channel channel, String kind, String message) {
        Message.Request.Builder builder = Message.Request.newBuilder();
        if (message == null) {
            builder.setData(ByteString.EMPTY);
        } else {
            ByteString msg = ByteString.copyFromUtf8(message);
            builder.setData(msg);
        }
        builder.setKind(kind);
        channel.write(builder.build());
        channel.flush();

    }

    /**
     * 创建请求对象
     *
     * @param kind
     * @param message
     * @return
     */
    public static Message.Request build(String kind, String message) {
        Message.Request.Builder builder = Message.Request.newBuilder();
        builder.setKind(kind);
        if (message == null) {
            builder.setData(ByteString.EMPTY);
        } else {
            builder.setData(ByteString.copyFromUtf8(message));
        }
        return builder.build();
    }

    /**
     * 创建请求对象
     *
     * @param kind
     * @param message
     * @return
     */
    public static Message.Request build(String kind, ByteString message) {
        Message.Request.Builder builder = Message.Request.newBuilder();
        builder.setKind(kind);
        if (message == null) {
            builder.setData(ByteString.EMPTY);
        } else {
            builder.setData(message);
        }
        return builder.build();
    }

    /**
     * 将文件拆分成数据块
     *
     * @param value
     */
    public static void buildBlock(Queue<Block> queue, DisplayValue value, long blockSIze) {

        try {
            long total = value.getLength();
            long offset;
            long left = total % blockSIze;
            long count = (total - left) / blockSIze;
            for (long i = 0; i < count; i++) {
                offset = i * blockSIze;
                Block data = new Block();
                data.length = blockSIze;
                data.offset = offset;
                if (i == 0) {
                    data.first = true;
                } else {
                    data.first = false;
                }
                queue.offer(data);
            }
            if (left > 0) {
                offset = count * blockSIze;
                Block data = new Block();
                data.length = left;
                data.offset = offset;
                if (count > 0) {
                    data.first = false;
                } else {
                    data.first = true;
                }
                queue.offer(data);
            }

        } catch (Exception ex) {
        }
    }

}
