package me.kingka.ui.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;
import me.kingka.network.data.Value;

/**
 * 显示数据对象集合
 *
 * @author swift.apple
 */
public class DisplayValueModel {

    private List<String> ids = null;
    private static DisplayValueModel instance;
    private final ObservableList<DisplayValue> dataList = FXCollections.observableArrayList();
    private final ObservableMap<String, DisplayValue> dict = FXCollections.observableHashMap();

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized DisplayValueModel getInstance() {
        if (instance == null) {
            instance = new DisplayValueModel();
        }
        return instance;
    }

    private DisplayValueModel() {
        ConfigurationFactory factory = ConfigurationFactory.getInstance();
        Configuration configuration = factory.configure();
        ids = configuration.getFiles();
    }

    /**
     * 数据集合
     *
     * @return
     */
    public List<Value> list() {
        List<Value> values = new LinkedList<>();
        dataList.stream().forEach(value -> values.add(value.toValue()));
        return values;
    }

    /**
     * 内部数据
     *
     * @return
     */
    public ObservableList<DisplayValue> data() {
        return dataList;
    }

    /**
     * 是否存在显示数据对象
     *
     * @param data
     * @return
     */
    public boolean contains(DisplayValue data) {
        String id = data.getId();
        return dict.containsKey(id);
    }

    /**
     * 添加显示数据对象
     *
     * @param data
     */
    public void append(DisplayValue data) {
        if (!dict.containsKey(data.getId())) {
            dict.putIfAbsent(data.getId(), data);
            dataList.add(data);
            if (ids != null) {
                if (!ids.contains(data.getPath())) {
                    ids.add(data.getPath());
                }
            }
        }
    }

    /**
     * 添加显示数据对象
     *
     * @param data
     */
    private void appendAnyway(DisplayValue data) {
        dict.put(data.getId(), data);
        dataList.add(data);
        if (ids != null) {
            if (!ids.contains(data.getPath())) {
                ids.add(data.getPath());
            }
        }
    }

    /**
     * 获取指定ID的显示数据对象
     *
     * @param id 显示数据对象ID
     * @return
     */
    public Optional<DisplayValue> get(String id) {
        Optional<DisplayValue> value = Optional.ofNullable(null);
        if (dict.containsKey(id)) {
            value = Optional.of(dict.get(id));
        }
        return value;
    }

    /**
     * 批量添加显示数据对象
     *
     * @param data
     */
    public void append(List<DisplayValue> data) {
        data.stream().filter(d -> !dict.containsKey(d.getId())).forEach(this::appendAnyway);
    }

    /**
     * 删除指定ID的刷新数据对象
     *
     * @param id 显示数据对象ID
     * @return
     */
    public Optional<DisplayValue> remove(String id) {
        if (dict.containsKey(id)) {
            DisplayValue data = dict.remove(id);
            dataList.remove(data);
            if (ids != null) {
                ids.remove(data.getPath());
            }
            return Optional.of(data);
        }
        return Optional.ofNullable(null);
    }

    public void remove(List<String> files) {
        files.stream().forEach(id -> {
            if (dict.containsKey(id)) {
                DisplayValue data = dict.remove(id);
                dataList.remove(data);
                if (ids != null) {
                    ids.remove(data.getPath());
                }
            }
        });
    }

    /**
     * 数量
     *
     * @return
     */
    public int count() {
        return dict.size();
    }
}
