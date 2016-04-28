package me.kingka.network.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;

/**
 * 显示数据对象集合
 *
 * @author swift.apple
 */
public class ValueModel {

    private static ValueModel instance;
    private final List<Value> dataList = new LinkedList<>();
    private final Map<String, Value> dict = new ConcurrentHashMap<>();

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized ValueModel getInstance() {
        if (instance == null) {
            instance = new ValueModel();
        }
        return instance;
    }

    private ValueModel() {
    }


    /**
     * 内部数据
     *
     * @return
     */
    public List<Value> data() {
        return dataList;
    }

    /**
     * 添加显示数据对象
     *
     * @param data
     */
    public void append(Value data) {
        if (!dict.containsKey(data.getId())) {
            dict.putIfAbsent(data.getId(), data);
            dataList.add(data);
        }
    }


    /**
     * 获取指定ID的显示数据对象
     *
     * @param id 显示数据对象ID
     * @return
     */
    public Optional<Value> get(String id) {
        Optional<Value> value = Optional.ofNullable(null);
        if (dict.containsKey(id)) {
            value = Optional.of(dict.get(id));
        }
        return value;
    }


}
