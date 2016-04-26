package me.kingka.network.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

/**
 * 配置信息工厂类
 *
 * @author swift.apple
 */
public class ConfigurationFactory {

    private Configuration configuration = null;
    private boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 配置
     *
     * @return
     */
    public Configuration configure() {
        load();
        return configuration;
    }

    private String configPath;

    public String getConfigPath() {
        return configPath;
    }

    /**
     * 设置配置文件地址
     *
     * @param configPath
     * @return
     */
    public ConfigurationFactory setConfigPath(String configPath) {
        this.configPath = configPath;
        return this;
    }

    private final Logger logger = Logger.getLogger(ConfigurationFactory.class);

    private ConfigurationFactory() {
        configuration = new Configuration();
    }

    private static ConfigurationFactory instance = null;

    /**
     * 获取配置工厂实例
     *
     * @return
     */
    public static synchronized ConfigurationFactory getInstance() {
        if (instance == null) {
            instance = new ConfigurationFactory();
        }
        return instance;
    }

    /**
     * 加载配置
     *
     * @return
     * @throws NullPointerException
     */
    public ConfigurationFactory load() throws NullPointerException {
        if (loaded) {
            return this;
        }
        if (configPath == null) {
            throw new NullPointerException("configPath is null");
        }
        final List<String> files = new LinkedList<>();
        String storage = null;
        int port = -1;
        String password = null;
        InputStream in = null;
        ByteArrayOutputStream bos;
        try {
            String folder = String.format("%s%s%s", System.getProperty("user.home"), File.separator, "mifi");
            String path = String.format("%s%s%s", folder, File.separator, configPath);
            File f = new File(path);
            if (!f.exists()) {
                return this;
            }
            in = new FileInputStream(path);
            bos = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = in.read(bytes)) > 0) {
                bos.write(bytes, 0, len);
            }
            byte[] buffer = bos.toByteArray();
            bos.close();
            String jsonString = StringUtils.newStringUtf8(buffer);
            JSONObject object = JSON.parseObject(jsonString);
            if (object.containsKey("port")) {
                port = object.getInteger("port");
            }
            if (object.containsKey("password")) {
                password = object.getString("password");
            }
            if (object.containsKey("storage")) {
                storage = object.getString("storage");
            }
            if (object.containsKey("files")) {
                JSONArray array = object.getJSONArray("files");
                array.stream().forEach(p -> files.add(String.valueOf(p)));
            }
            configuration.setFiles(files);
            configuration.setPort(port);
            configuration.setPassword(password);
            configuration.setStorage(storage);
            loaded = true;
        } catch (IOException e) {
            logger.debug(e);
        } finally {
            if (port < 0) {
                port = 8888;
            }
            if (port > 65535) {
                port = 65535;
            }
            if (password == null) {
                password = "";
            }
            if (storage == null) {
                storage = System.getProperty("user.home");
            }
            configuration.setFiles(files);
            apply(storage, password, port);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.debug(e);
                }
            }

        }
        return this;
    }

    /**
     * 端口
     *
     * @return
     */
    public int port() {
        if (configuration.getPort() == -1) {
            load();
        }
        return configuration.getPort();
    }

    /**
     * 密码
     *
     * @return
     */
    public String password() {
        if (configuration.getPassword() == null) {
            load();
        }
        return configuration.getPassword();
    }

    /**
     * 存储地址
     *
     * @return
     */
    public String storage() {
        if (configuration.getStorage() == null) {
            load();
        }
        return configuration.getStorage();
    }

    /**
     * 设置数据
     *
     * @param storage
     * @param password
     * @param port
     * @return
     */
    public ConfigurationFactory apply(String storage, String password, int port) {
        configuration.setPort(port);
        configuration.setPassword(password);
        configuration.setStorage(storage);
        return this;
    }

    /**
     * 保存配置
     *
     * @return
     */
    public ConfigurationFactory save() {
        String config = JSON.toJSONString(configuration);
        String folder = String.format("%s%s%s", System.getProperty("user.home"), File.separator, "mifi");
        File fd = new File(folder);
        if (!fd.exists()) {
            fd.mkdirs();
        }
        String path = String.format("%s%s%s", folder, File.separator, configPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            IOUtils.write(StringUtils.getBytesUtf8(config), fos);
        } catch (IOException ex) {
            logger.debug(ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    logger.debug(ex);
                }
            }
        }
        return this;
    }

}
