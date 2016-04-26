package me.kingka.ui;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.network.Socket;
import me.kingka.network.SocketDelegate;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;
import me.kingka.service.Buffer;
import me.kingka.service.Cache;
import me.kingka.service.Common;
import me.kingka.service.Download;
import me.kingka.service.core.Service;
import me.kingka.service.core.SocketService;
import me.kingka.ui.model.DisplayValue;
import me.kingka.ui.model.DisplayValueModel;
import me.kingka.utils.Toolkit;
import static me.kingka.utils.Toolkit.*;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileSystemUtils;
import org.apache.log4j.Logger;

/**
 * 视图控制器
 *
 * @author swift.apple
 */
public class ViewController extends SocketDelegate implements EventHandler<ActionEvent> {

    private final Logger logger = Logger.getLogger(ViewController.class);
    private Service service = null;
    private I18n locale = null;
    private Cache cache = null;
    private Download download = null;

    public ViewController() {
        service = SocketService.getInstance();
        locale = I18n.getInstance();
        model = DisplayValueModel.getInstance();
        cache = Cache.create();
        download = Download.create();
    }

    @FXML
    private TableView<DisplayValue> tableView;
    @FXML
    private PasswordField password;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnSync;
    @FXML
    private Label lbInfo;
    @FXML
    private Button btnChooseStorage;
    @FXML
    private TextField port;
    @FXML
    private TextField storage;

    private DisplayValueModel model = null;

    @FXML
    private void onChooseStorage(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.setTitle(locale.get("ui.controller.config.001"));
        File file = fileChooser.showDialog(stage);
        if (file == null) {//没有选文件
            return;
        }
        String folder;
        if (file.isFile()) {
            folder = file.getParent();
        } else {
            folder = file.getAbsolutePath();
        }
        storage.setText(folder);
        configuration.setStorage(folder);
    }

    /**
     * 添加文件
     *
     * @param event
     */
    @FXML
    private void onAddFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.setTitle(locale.get("ui.controller.005"));
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files == null) {//没有选文件
            return;
        }
        files.stream().forEach(file -> {
            model.append(makeDisplayValue(file));
        });
    }

    /**
     * 删除文件
     *
     * @param event
     */
    @FXML
    private void onDeleteFile(ActionEvent event) {
        TableView.TableViewSelectionModel<DisplayValue> modelSelection = tableView.selectionModelProperty().get();
        if (modelSelection != null) {
            final List<String> files = new LinkedList<>();
            modelSelection.getSelectedItems().stream().forEach(file -> {
                files.add(file.getId());
            });
            model.remove(files);
        }
    }

    /**
     * 禁用按钮
     *
     * @param disable
     */
    private void disableButton(boolean disable) {
        btnAdd.setDisable(disable);
        btnDelete.setDisable(disable);
        btnSync.setDisable(disable);
        btnChooseStorage.setDisable(disable);
    }

    /**
     * 表格菜单回调
     *
     * @param event
     */
    @Override
    public void handle(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String id = source.getId();
        id = id.replace("menu.", "");
        int mId = Integer.parseInt(id);
        switch (mId) {
            case MenuAction.ACTION1:
                onAddFile(null);
                break;
            case MenuAction.ACTION2:
                onDeleteFile(null);
                break;
            case MenuAction.ACTION3:
                onSync(null);
                break;
        }
    }

    private boolean check() {
        boolean isFolder = false;
        String folder = null;
        try {
            folder = storage.getText();
            if (folder != null && !folder.trim().isEmpty()) {
                File file = new File(folder);
                isFolder = file.exists() && file.isDirectory();
            }
        } catch (Exception ex) {
            isFolder = false;
        }
        if (!isFolder) {
            information(locale.get("ui.information.title"), locale.get("ui.controller.006"), stage, null);
            return false;
        }
        String portString = port.getText();
        if (!isPort(portString)) {
            information(locale.get("ui.information.title"), locale.get("ui.controller.007"), stage, null);
            return false;
        }
        String passwordString = password.getText();
        configuration.setPassword(passwordString);
        configuration.setPort(Integer.parseInt(portString));
        configuration.setStorage(folder);
        return true;
    }

    /**
     * 开启服务或关闭服务
     *
     * @param event
     */
    @FXML
    private void onSync(ActionEvent event) {
        if (service.isActive()) {
            disableButton(true);
            service.shutdown();
        } else {
            boolean success = check();
            if (!success) {
                return;
            }
            service.delegate(this);
            disableButton(true);
            service.startup();
        }
    }

    /**
     * 显示消息
     *
     * @param msg
     */
    private void displayMessage(String msg) {
        Platform.runLater(() -> {
            lbInfo.setText(msg);
        });

    }

    /**
     * 更新按钮状态
     *
     * @param state
     */
    private void updateButtonState(int state) {
        final int port = configuration.getPort();
        Platform.runLater(() -> {
            disableButton(false);
            if (state == Socket.SESSION_OFF) {
                btnSync.setText(locale.get("ui.controller.001"));
                displayMessage(locale.get("ui.controller.002"));
            } else {
                btnSync.setText(locale.get("ui.controller.003"));
                displayMessage(String.format(locale.get("ui.controller.004"), port));
            }
        });

    }
    private Configuration configuration = null;
    private ConfigurationFactory factory = null;

    //此方法会被FXMLLoader调用
    @FXML
    private void initialize() {
        createCells();
        factory = ConfigurationFactory.getInstance();
        configuration = factory.configure();
        List<String> files = configuration.getFiles();
        if (files != null) {
            files.stream().forEach(file -> {
                try {
                    File f = new File(file);
                    if (f.exists()) {
                        model.append(makeDisplayValue(f));
                    }
                } catch (Exception ex) {

                }
            });

        }
        password.setText(configuration.getPassword());
        port.setText(String.valueOf(configuration.getPort()));
        String st = configuration.getStorage();
        if (st.isEmpty()) {
            st = System.getProperty("user.home");
        } else {
            st = configuration.getStorage();
        }
        storage.setText(st);
        tableView.setItems(model.data());
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        password.setText(configuration.getPassword());
        stage.setOnCloseRequest(event -> {
            event.consume();
            confirm(locale.get("ui.alert.title"), locale.get("ui.alert.message"), stage,
                    response -> {
                        if (response == ButtonType.OK) {
                            String passwordString = password.getText();
                            configuration.setPassword(passwordString);
                            String portString = port.getText();
                            int iPort = configuration.getPort();
                            if (Toolkit.isPort(portString)) {
                                iPort = Integer.parseInt(portString);
                            }
                            configuration.setPort(iPort);
                            String storageString = storage.getText();
                            configuration.setStorage(storageString);
                            factory.save();
                            System.exit(0);
                        }
                    }
            );
        });

    }

    /**
     * 创建单元格
     */
    private void createCells() {
        ObservableList<TableColumn<DisplayValue, ?>> columns = tableView.getColumns();
        TableColumn<DisplayValue, String> column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setSortable(false);
        column.setPrefWidth(36);
        column.setText(locale.get("ui.controller.column0"));
        column.setCellValueFactory(new PropertyValueFactory<>("icon"));
        column.setCellFactory(column1 -> new ImageTableCell<>());
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(500);
        column.setText(locale.get("ui.controller.column1"));
        column.setCellValueFactory(new PropertyValueFactory<>("name"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(this));
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(112);
        column.setText(locale.get("ui.controller.column2"));
        column.setCellValueFactory(new PropertyValueFactory<>("catalog"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(this));
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(130);
        column.setText(locale.get("ui.controller.column3"));
        column.setCellValueFactory(new PropertyValueFactory<>("size"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(this));
        columns.add(column);

    }

    /**
     * 产生异常
     *
     * @param exception
     */
    @Override
    public void exceptionCaught(String exception) {
        Platform.runLater(() -> {
            disableButton(false);
        });
        displayMessage(exception);
    }

    /**
     * 接收消息
     *
     * @param channel
     * @param message
     */
    @Override
    public void messageReceived(Channel channel, Object message) {
        if (message instanceof Message.Request) {
            Message.Request request = (Message.Request) message;
            String kind = request.getKind();
            if (kind == null) {
                return;
            }
            switch (kind) {
                case Common.LIST_FILES:
                    handleList(channel, request);
                    break;
                case Common.DOWNLOAD_FILES:
                    handleRequestDownload(channel, request);
                    break;
                case Common.UPLOAD_FILES:
                    handleRequestUpload(channel, request);
                    break;
                case Common.FLUSH_UPLOAD_CACHE:
                    handleFlush(channel, request);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 解析下载
     *
     * @param channel
     * @param request
     */
    private void handleRequestDownload(Channel channel, Message.Request request) {
        String kind = request.getKind();
        ByteString byteString = request.getData();
        if (byteString == null || byteString.isEmpty()) {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
            return;
        }
        java.nio.ByteBuffer buffer = byteString.asReadOnlyByteBuffer();
        String password = null;
        String id = null;
        long offset = -1;
        long len = -1;
        try {
            if (buffer.remaining() >= 4) {
                int passwordLength = buffer.getInt();//密码字节数
                if (buffer.remaining() >= passwordLength) {
                    byte[] passwordBytes = new byte[passwordLength];
                    buffer.get(passwordBytes);
                    password = StringUtils.newStringUtf8(passwordBytes);//密码
                    if (buffer.remaining() >= 4) {
                        int idLength = buffer.getInt();//id长度
                        if (buffer.remaining() >= idLength) {
                            byte[] idBytes = new byte[idLength];//id
                            buffer.get(idBytes);
                            id = StringUtils.newStringUtf8(idBytes);
                            if (buffer.remaining() >= 8) {
                                offset = buffer.getLong();//偏移
                                if (buffer.remaining() >= 8) {
                                    len = buffer.getLong();//长度
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {

        }
        if (password == null || id == null || offset < 0 || len < 0) {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
            return;
        }
        String realPassword = DigestUtils.md5Hex(configuration.getPassword());
        if (!password.equals(realPassword)) {
            send(channel, kind, null, Common.PASSWORD_INVALID);
            return;
        }
        Optional<DisplayValue> optional = model.get(id);
        if (!optional.isPresent()) {
            send(channel, kind, null, Common.FILE_NOT_FOUND);
            return;
        }
        String file = optional.get().getPath();
        byte[] bytes = download.read(file, offset, len);
        if (bytes == null) {
            send(channel, kind, null, Common.FILE_NO_DATA);
            return;
        }
        ByteString c = ByteString.copyFrom(bytes);
        send(channel, kind, c, Common.SUCCESS);
    }

    /**
     * 刷新
     *
     * @param channel
     * @param request
     */
    private void handleFlush(Channel channel, Message.Request request) {
        String kind = request.getKind();
        String bufferId = DigestUtils.md5Hex(getIp(channel));
        cache.flush(bufferId);
        send(channel, kind, null, Common.SUCCESS);
    }

    /**
     * 解析上传
     *
     * @param channel
     * @param request
     */
    private void handleRequestUpload(Channel channel, Message.Request request) {
        String kind = request.getKind();
        ByteString byteString = request.getData();
        if (byteString != null && !byteString.isEmpty()) {
            java.nio.ByteBuffer block = byteString.asReadOnlyByteBuffer();
            String fileName = null;
            long blockTotalSize = -1;
            long blockSize = -1;
            byte[] blockData = null;
            byte blockPosition = 0;
            String password = null;
            try {
                //解析数据
                if (block.remaining() >= 4) {
                    int passwordLength = block.getInt();//密码字节数
                    if (block.remaining() >= passwordLength) {
                        byte[] passwordBytes = new byte[passwordLength];
                        block.get(passwordBytes);
                        password = StringUtils.newStringUtf8(passwordBytes);//密码
                        if (block.remaining() >= 4) {
                            int fileNameLength = block.getInt();//文件名字节数
                            if (block.remaining() >= fileNameLength) {
                                byte[] bytes = new byte[fileNameLength];
                                block.get(bytes);
                                fileName = StringUtils.newStringUtf8(bytes);//文件名
                                if (block.remaining() >= 8) {
                                    blockTotalSize = block.getLong();//数据块总大小
                                    if (block.remaining() >= 1) {
                                        blockPosition = block.get();//数据块块位置
                                        if (block.remaining() >= 8) {
                                            blockSize = block.getLong();//数据块长度
                                            if (block.remaining() >= blockSize) {
                                                blockData = new byte[(int) blockSize];//数据
                                                block.get(blockData);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {

            }
            if (password == null || fileName == null || blockTotalSize == -1 || blockData == null || blockSize == -1) {
                send(channel, kind, null, Common.PARAM_IS_MISSING);
                return;
            }
            String password2 = DigestUtils.md5Hex(configuration.getPassword());
            if (!password2.equals(password)) {
                send(channel, kind, null, Common.PASSWORD_INVALID);
                return;
            }
            long diskFreeSpace = 0;
            try {
                diskFreeSpace = FileSystemUtils.freeSpaceKb(configuration.getStorage());
                diskFreeSpace *= 1024;
            } catch (IOException e) {
            }
            if (blockSize >= diskFreeSpace || blockTotalSize >= diskFreeSpace) {
                send(channel, kind, null, Common.NO_FREE_SPACE);
                return;
            }
            String bufferId = DigestUtils.md5Hex(getIp(channel));
            Buffer buffer = cache.newBuffer(bufferId, fileName);
            if (buffer == null) {
                send(channel, kind, null, Common.NO_ENOUGH_BUFF);
                return;
            }
            if (blockPosition == 1) {//第一个数据包
                boolean success = true;
                try {
                    String path = buffer.getFile();
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception ex) {
                    success = false;
                } finally {
                    if (!success) {
                        send(channel, kind, null, Common.FILE_UPLOAD_FAIL);
                        return;
                    }
                }
                buffer.clear();//删除上一次的剩余数据
            }
            int bytes = cache.write(bufferId, blockData);
            if (bytes > 0) {
                send(channel, kind, null, Common.SUCCESS);
            } else {
                send(channel, kind, null, Common.FILE_UPLOAD_FAIL);
            }
        } else {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
        }
    }

    /**
     * 解析文件列表
     *
     * @param channel
     * @param request
     */
    private void handleList(Channel channel, Message.Request request) {
        ByteString pass = request.getData();
        String kind = request.getKind();
        if (pass != null && pass.isValidUtf8()) {
            String password = pass.toStringUtf8();
            if (!isValidPassword(configuration.getPassword(), password)) {
                send(channel, kind, null, Common.PASSWORD_INVALID);
            } else {
                String jsonString = JSON.toJSONString(model.list());
                ByteString msg = ByteString.copyFromUtf8(jsonString);
                send(channel, kind, msg, Common.SUCCESS);
            }
        } else {
            send(channel, kind, null, Common.PARAM_IS_MISSING);
        }

    }

    /**
     * 获取IP
     *
     * @param channel
     * @return
     */
    private String getIp(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        return address.getHostString();
    }

    /**
     * 状态变更
     *
     * @param state
     */
    @Override
    public void stateChanged(int state) {
        updateButtonState(state);
    }

    /**
     * 建立连接
     *
     * @param channel
     */
    @Override
    public void sessionOpen(Channel channel) {
        String host = getIp(channel);
        logger.info(String.format("%s connected to server", host));
    }

    /**
     * 断开连接
     *
     * @param channel
     */
    @Override
    public void sessionEnd(Channel channel) {
        String host = getIp(channel);
        logger.info(String.format("%s disconnected from server", host));
        String bufferId = DigestUtils.md5Hex(host);
        //断开连接后释放服务器缓存
        cache.removeBuffer(bufferId);
        logger.info(String.format("remove buffer while disconnected:%s", bufferId));
    }
}
