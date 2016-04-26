package me.kingka.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.kingka.network.data.Value;

/**
 * 显示数据对象
 *
 * @author swift.apple
 */
public class DisplayValue {

    private long length;

    /**
     * 文件长度
     *
     * @return
     */
    public long getLength() {
        return length;
    }

    private String id;

    /**
     * 文件ID
     *
     * @return
     */
    public String getId() {
        return id;
    }

    private String path;

    /**
     * 文件地址
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    private final StringProperty icon = new SimpleStringProperty();

    /**
     * 图标
     *
     * @return
     */
    public String getIcon() {
        return icon.get();
    }

    public void setIcon(String value) {
        icon.set(value);
    }

    public StringProperty iconProperty() {
        return icon;
    }

    private final StringProperty name = new SimpleStringProperty();

    /**
     * 文件名
     *
     * @return
     */
    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public StringProperty nameProperty() {
        return name;
    }

    private final StringProperty size = new SimpleStringProperty();

    /**
     * 大小描述
     *
     * @return
     */
    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public void setSize(String value) {
        this.size.set(value);
    }

    private final StringProperty catalogProperty = new SimpleStringProperty();


    public StringProperty catalogProperty() {
        return catalogProperty;
    }

    /**
     * 类别
     *
     * @return
     */
    public String getCatalog() {
        return catalogProperty.get();
    }

    public void setCatalog(String catalog) {
        catalogProperty.set(catalog);
    }

    /**
     * 转化位数据
     * @return
     */
    public Value toValue() {
        Value value = new Value();
        value.setName(name.get());
        value.setId(id);
        value.setLength(length);
        return value;
    }

    public DisplayValue() {

    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
