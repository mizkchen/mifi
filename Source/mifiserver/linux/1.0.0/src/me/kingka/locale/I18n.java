package me.kingka.locale;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * 国际化
 *
 * @author swift.apple
 */
public class I18n {

    private static I18n instance;

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized I18n getInstance() {
        if (instance == null) {
            instance = new I18n();
        }
        return instance;
    }

    private I18n() {

    }

    private final Map<String, String> dictionary = new HashMap<>();

    /**
     * 加载国际化文件
     *
     * @param locale
     * @return
     */
    public I18n load(String locale) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(String.format("%s.json", locale))) {
            String jsonString = IOUtils.toString(stream, "utf-8");
            JSONObject object = JSON.parseObject(jsonString);
            JSONObject dict = object.getJSONObject("i18n");
            dict.entrySet().stream().forEach((entry) -> {
                dictionary.putIfAbsent(entry.getKey(), String.valueOf(entry.getValue()));
            });
        } catch (Exception ex) {
        }
        return this;
    }

    /**
     * 获得国际化文字
     *
     * @param key
     * @return
     */
    public String get(String key) {
        String value = dictionary.get(key);
        if (value == null) {
            return "";
        }
        return value;
    }
}
