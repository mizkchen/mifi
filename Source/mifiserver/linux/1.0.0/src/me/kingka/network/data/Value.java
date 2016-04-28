package me.kingka.network.data;

/**
 * 值对象
 */
public class Value implements java.io.Serializable {
    private String name;
    private String id;
    private long length;
    private String path;

    /**
     * 文件长度
     *
     * @return
     */
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public Value() {

    }

    /**
     * 文件名
     *
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 文件ID
     *
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
