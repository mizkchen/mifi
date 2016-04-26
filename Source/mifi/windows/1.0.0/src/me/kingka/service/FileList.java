package me.kingka.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import javafx.application.Platform;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.ui.model.DisplayValue;
import me.kingka.ui.model.DisplayValueModel;

import java.util.LinkedList;
import java.util.List;

import static me.kingka.utils.Toolkit.*;

/**
 * 文件列表
 *
 * @author swift.apple
 */
public class FileList {

    private DisplayValueModel model;
    private I18n locale;

    public FileList(DisplayValueModel model) {
        this.model = model;
        locale = I18n.getInstance();
    }

    /**
     * 处理文件列表
     *
     * @param channel
     * @param response
     */
    public void messageReceived(Channel channel, Message.Response response) {
        int status = response.getStatus();
        try {
            ByteString bytes = response.getData();
            if (bytes == null || !bytes.isValidUtf8()) {
                return;
            }
            if (status == Common.SUCCESS) {
                JSONArray array = JSON.parseArray(bytes.toStringUtf8());
                List<DisplayValue> valueList = new LinkedList<>();
                array.stream().forEach((item) -> {
                    String id = null;
                    String name = null;
                    long len = -1;
                    JSONObject object = (JSONObject) item;
                    if (object != null && object.containsKey("id")) {
                        id = object.getString("id");
                    }
                    if (object != null && object.containsKey("name")) {
                        name = object.getString("name");
                    }
                    if (object != null && object.containsKey("length")) {
                        len = object.getLong("length");
                    }
                    if (name != null && id != null && len > 0) {
                        valueList.add(makeDisplayValue(id, name, len));
                    }
                });
                Platform.runLater(() -> {
                    model.clear();
                    model.append(valueList);
                });
            } else {
                Platform.runLater(() -> information(locale.get("ui.information.title"), locale.get(String.format("error.%s", status)), stage, null));
            }

        } catch (Exception ex) {

        }

    }
}
