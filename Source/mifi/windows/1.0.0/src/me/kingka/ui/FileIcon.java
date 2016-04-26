package me.kingka.ui;

/**
 * 图标
 * @author swift.apple
 */
public enum FileIcon {
    VIDEO("fv"),
    MUSIC("fm"),
    APPLICATIONS("fa"),
    PICTURE("fp"),
    FILE("ff"),
    DOWNLOADING("fd"),
    UPLOADING("fu"),
    COMPLETE("fc"),
    ERROR("fe");
    private String content;

    FileIcon(String content) {
        this.content = content;
    }

    public String description() {
        return content;
    }

}