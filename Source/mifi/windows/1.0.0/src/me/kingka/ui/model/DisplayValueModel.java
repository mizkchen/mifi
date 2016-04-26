package me.kingka.ui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Optional;

/**
 * 显示数据对象集合
 *
 * @author swift.apple
 */
public class DisplayValueModel {
    private final ObservableList<DisplayValue> dataList = FXCollections.observableArrayList();

    public static DisplayValueModel createInstance() {
        return new DisplayValueModel();
    }

    private DisplayValueModel() {

    }

    /**
     * 获取显示数据对象
     *
     * @param index 位置
     * @return
     */
    public Optional<DisplayValue> get(int index) {
        Optional<DisplayValue> value = Optional.ofNullable(null);
        if (index >= 0 && index < dataList.size()) {
            value = Optional.of(dataList.get(index));
        }
        return value;
    }

    /**
     * 集合是否是空的
     *
     * @return
     */
    public boolean isEmpty() {
        return dataList.isEmpty();
    }

    /**
     * 集合内部数据
     *
     * @return
     */
    public ObservableList<DisplayValue> data() {
        return dataList;
    }

    /**
     * 是否包含显示数据对象
     *
     * @param data
     * @return
     */
    public boolean contains(DisplayValue data) {
        return dataList.contains(data);
    }

    /**
     * 添加显示数据对象
     *
     * @param data
     */
    public void append(DisplayValue data) {
        dataList.add(data);
    }

    /**
     * 批量添加显示数据对象
     *
     * @param list
     */
    public void append(Collection<DisplayValue> list) {
        dataList.addAll(list);
    }

    /**
     * 清空集合
     */
    public void clear() {
        dataList.clear();
    }

    /**
     * 获取指定ID的显示数据对象
     *
     * @param id 显示数据对象ID
     * @return
     */
    public Optional<DisplayValue> get(String id) {
        return dataList.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst();
    }

    /**
     * 删除指定ID的显示数据对象
     *
     * @param id 显示数据对象ID
     * @return
     */

    public Optional<DisplayValue> remove(String id) {
        Optional<DisplayValue> value = get(id);
        if (value.isPresent()) {
            dataList.remove(value.get());
            return value;
        }
        return Optional.ofNullable(null);
    }

    /**
     * 显示数据对象数量
     *
     * @return
     */
    public int count() {
        return dataList.size();
    }
}
