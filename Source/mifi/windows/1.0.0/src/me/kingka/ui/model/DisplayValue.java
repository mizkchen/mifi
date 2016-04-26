package me.kingka.ui.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 显示数据对象
 *
 * @author swift.apple
 */
public class DisplayValue {

    private long length;

    /**
     * 文件字节数
     *
     * @param length
     */
    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    /**
     * 文件ID
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }


    private String id;

    public String getId() {
        return id;
    }

    private String path;

    /**
     * 文件地址
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }


    private long current;

    /**
     * 当前已下载或上传的字节数
     */
    public long getCurrent() {
        return current;
    }

    public void setCurrent(long value) {
        this.current = value;
    }

    private final StringProperty elapsed = new SimpleStringProperty();


    public void setElapsed(String value) {
        elapsed.set(value);
    }

    /**
     * 已消耗的时间
     */
    public String getElapsed() {
        return elapsed.get();
    }

    public StringProperty elapsedProperty() {
        return elapsed;
    }

    /**
     * 开始下载或上传的时间
     *
     * @return
     */
    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    private long beginTime;

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

    private final DoubleProperty progress = new SimpleDoubleProperty();

    /**
     * 进度1
     *
     * @return
     */
    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double value) {
        progress.set(value);
    }

    public DoubleProperty progressProperty() {
        return progress;
    }


    private final StringProperty percent = new SimpleStringProperty();

    /**
     * 进度2
     *
     * @return
     */
    public String getPercent() {
        return percent.get();
    }

    public void setPercent(String value) {
        percent.set(value);
    }

    public StringProperty percentProperty() {
        return percent;
    }

    private final StringProperty size = new SimpleStringProperty();


    public void setSize(String size) {
        this.size.set(size);
    }

    /**
     * 大小描述文字
     */
    public String getSize() {
        return size.get();
    }

    public StringProperty sizeProperty() {
        return size;
    }

    private final StringProperty catalog = new SimpleStringProperty();

    public StringProperty catalogProperty() {
        return catalog;
    }

    /**
     * 类别
     *
     * @return
     */
    public String getCatalog() {
        return catalog.get();
    }

    public void setCatalog(String catalog) {
        this.catalog.set(catalog);
    }

    private final StringProperty speed = new SimpleStringProperty();

    public void setSpeed(String speed) {
        this.speed.set(speed);
    }

    /**
     * 下载或上传的速度
     *
     * @return
     */
    public String getSpeed() {
        return speed.get();
    }


    public StringProperty speedProperty() {
        return speed;
    }

    /**
     * 克隆对象
     *
     * @return
     */
    public DisplayValue cloneMe() {
        DisplayValue value = new DisplayValue();
        value.setId(getId());
        value.setLength(getLength());
        value.setPath(getPath());
        value.setCurrent(getCurrent());
        value.setElapsed(getElapsed());
        value.setBeginTime(getBeginTime());
        value.setIcon(getIcon());
        value.setName(getName());
        value.setProgress(getProgress());
        value.setPercent(getPercent());
        value.setSize(getSize());
        value.setCatalog(getCatalog());
        value.setSpeed(getSpeed());
        return value;
    }

    public DisplayValue() {
    }


}
