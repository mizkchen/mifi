package me.kingka.ui;

/**
 * 文件图标
 *
 * @author swift.apple
 */
public enum FileIcon {
    /**
     * 视频
     */
    VIDEO("fv"),
    /**
     * 音乐
     */
    MUSIC("fm"),
    /**
     * 应用程序
     */
    APPLICATIONS("fa"),
    /**
     * 图片
     */
    PICTURE("fp"),
    /**
     * 文档
     */
    FILE("ff");

    private String content;

    FileIcon(String content) {
        this.content = content;
    }

    public String description() {
        return content;
    }

}