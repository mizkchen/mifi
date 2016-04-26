package me.kingka.ui;

import io.netty.channel.Channel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import me.kingka.locale.I18n;
import me.kingka.message.Message;
import me.kingka.network.Socket;
import me.kingka.network.SocketDelegate;
import me.kingka.network.configuration.Configuration;
import me.kingka.network.configuration.ConfigurationFactory;
import me.kingka.service.Common;
import me.kingka.service.Download;
import me.kingka.service.FileList;
import me.kingka.service.Upload;
import me.kingka.service.core.Service;
import me.kingka.service.core.SocketService;
import me.kingka.ui.model.DisplayValue;
import me.kingka.ui.model.DisplayValueModel;
import me.kingka.ui.progress.BusyIndicator;
import me.kingka.utils.Toolkit;
import static me.kingka.utils.Toolkit.*;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 视图控制器
 *
 * @author swift.apple
 */
public class ViewController extends SocketDelegate implements EventHandler<ActionEvent> {

    @FXML
    private Button btnDownload;
    @FXML
    private Button btnConnect;
    @FXML
    private TextField host;
    @FXML
    private PasswordField password;
    @FXML
    private TextField port;
    @FXML
    private TableView<DisplayValue> sharedFiles;
    @FXML
    private TableView<DisplayValue> downloadFiles;
    @FXML
    private Button btnRefreshSharedFiles;
    @FXML
    private Button btnAddToList;
    @FXML
    private Button btnChooseDownloadFolder;
    @FXML
    private Button btnAddToListAll;
    @FXML
    private Button btnCancelDownload;
    @FXML
    private TextField downloadFolder;
    @FXML
    private TabPane tabCard;

    public ViewController() {
        service = SocketService.getInstance();
        locale = I18n.getInstance();

    }

    /**
     * 连接服务器
     *
     * @param event
     */
    @FXML
    private void onConnect(ActionEvent event) {
        if (service.isActive()) {
            service.disconnect();
        } else {
            String hostString = this.host.getText();
            String portString = this.port.getText();
            if (!isIP(hostString)) {
                information(locale.get("ui.information.title"), locale.get("ui.controller.005"), stage, null);
                return;
            }
            if (!isPort(portString)) {
                information(locale.get("ui.information.title"), locale.get("ui.controller.006"), stage, null);
                return;
            }
            disableButton(true);
            configuration.setHost(hostString);
            String passwordString = password.getText();
            configuration.setPassword(passwordString);
            configuration.setPort(Integer.parseInt(portString));
            service.delegate(this);
            indicator.show();
            service.connect(hostString, Integer.parseInt(portString));
        }
    }

    /**
     * 刷新文件列表
     *
     * @param event
     */
    @FXML
    private void onRefreshFiles(ActionEvent event) {
        if (!service.isActive()) {
            information(locale.get("ui.information.title"), locale.get("ui.controller.009"), stage, null);
            return;
        }
        String passwordString = password.getText();
        configuration.setPassword(passwordString);
        service.send(build(Common.LIST_FILES, DigestUtils.md5Hex(passwordString)));
    }

    /**
     * 添加选中文件到下载列表
     *
     * @param event
     */
    @FXML
    private void onAddToList(ActionEvent event) {
        if (!service.isActive()) {
            return;
        }
        if (sharedFilesModel.isEmpty()) {
            return;
        }
        TableSelectionModel<DisplayValue> model = sharedFiles.getSelectionModel();
        if (model != null) {
            model.getSelectedItems().stream().forEach(value -> {
                downloadFilesModel.append(value.cloneMe());
            });
            information(locale.get("ui.information.title"), locale.get("ui.controller.010"), stage, null);
            tabCard.getSelectionModel().select(1);
        }
    }

    /**
     * 选择下载文件保存目录
     *
     * @param event
     */
    @FXML
    private void onChooseDownloadFolder(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.setTitle(locale.get("ui.controller.005"));
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
        downloadFolder.setText(folder);
        configuration.setStorage(folder);
    }

    /**
     * 取消下载
     *
     * @param event
     */
    @FXML
    private void onCancelDownload(ActionEvent event) {
        if (download.isActive()) {
            download.stop();
        }
    }

    /**
     * 下载文件
     *
     * @param event
     */
    @FXML
    private void onDownload(ActionEvent event) {

        boolean isFolder = false;
        String folder = null;
        try {
            folder = downloadFolder.getText();
            if (folder != null && !folder.trim().isEmpty()) {
                File file = new File(downloadFolder.getText());
                isFolder = file.exists() && file.isDirectory();
            }
        } catch (Exception ex) {
            isFolder = false;
        }
        if (!isFolder) {
            information(locale.get("ui.information.title"), locale.get("ui.controller.012"), stage, null);
            return;
        }
        if (!service.isActive()) {
            information(locale.get("ui.information.title"), locale.get("ui.controller.009"), stage, null);
            return;
        }
        if (downloadFilesModel.isEmpty()) {
            return;
        }
        if (!download.isActive()) {
            String passwordString = password.getText();
            configuration.setPassword(passwordString);
            Optional<DisplayValue> value = downloadFilesModel.get(0);
            if (value.isPresent()) {
                download.download(value.get());
            }

        }
    }

    /**
     * 添加所有文件到下载列表
     *
     * @param event
     */
    @FXML
    private void onAddToListAll(ActionEvent event) {
        if (sharedFilesModel.isEmpty()) {
            return;
        }
        sharedFilesModel.data().stream().forEach(value -> {
            downloadFilesModel.append(value.cloneMe());
        });
        information(locale.get("ui.information.title"), locale.get("ui.controller.010"), stage, null);
        tabCard.getSelectionModel().select(1);
    }

    @FXML
    private Button btnCancelUpload;
    @FXML
    private Button btnAddToUploadList;
    @FXML
    private TableView<DisplayValue> uploadFiles;
    @FXML
    private Button btnUpload;

    /**
     * 取消上传
     *
     * @param event
     */
    @FXML
    private void onCancelUpload(ActionEvent event) {
        if (upload.isActive()) {
            upload.stop();
        }
    }

    /**
     * 上传文件
     *
     * @param event
     */
    @FXML
    private void onUpload(ActionEvent event) {
        if (!service.isActive()) {
            information(locale.get("ui.information.title"), locale.get("ui.controller.009"), stage, null);
            return;
        }
        if (!upload.isActive()) {
            if (uploadFilesModel.isEmpty()) {
                return;
            }
            String passwordString = password.getText();
            configuration.setPassword(passwordString);
            Optional<DisplayValue> value = uploadFilesModel.get(0);
            if (value.isPresent()) {
                upload.upload(value.get());
            }
        }
    }

    /**
     * 添加文件到上传列表
     *
     * @param event
     */
    @FXML
    private void onAddToUploadList(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        fileChooser.setTitle(locale.get("ui.controller.011"));
        List<File> files = fileChooser.showOpenMultipleDialog(stage);
        if (files == null) {//没有选文件
            return;
        }
        files.stream().forEach(file -> {
            uploadFilesModel.append(makeDisplayValue(file));
        });
    }

    @FXML
    private TableView<DisplayValue> historyFiles;

    @FXML
    private Button btnOpenFolder;
    @FXML
    private Button btnClearHistoryFiles;

    @FXML
    private Button btnRemoveHistory;

    /**
     * 删除历史纪录
     *
     * @param event
     */
    @FXML
    private void onRemoveHistory(ActionEvent event) {
        TableSelectionModel<DisplayValue> model = historyFiles.getSelectionModel();
        if (model != null) {
            model.getSelectedItems().stream().forEach(value -> historyFilesModel.remove(value.getId()));
        }

    }

    /**
     * 打开文件所在目录
     *
     * @param event
     */
    @FXML
    private void onOpenFolder(ActionEvent event) {
        TableSelectionModel<DisplayValue> model = historyFiles.selectionModelProperty().get();
        if (model != null) {
            DisplayValue value = model.getSelectedItem();
            try {
                File file = new File(value.getPath());
                if (file.exists()) {
                    File folder = file.getParentFile();
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(folder);
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

    /**
     * 清空历史纪录
     *
     * @param event
     */
    @FXML
    private void onClearHistoryFiles(ActionEvent event) {
        historyFilesModel.clear();
    }

    private DisplayValueModel sharedFilesModel;
    private DisplayValueModel downloadFilesModel;
    private DisplayValueModel uploadFilesModel;
    private DisplayValueModel historyFilesModel;
    private I18n locale = null;
    private Service service = null;
    private BusyIndicator indicator;
    private Upload upload;
    private Download download;
    private FileList list;
    private Configuration configuration;
    private ConfigurationFactory factory;

    //此方法会被FXMLLoader调用
    @FXML
    private void initialize() {
        factory = ConfigurationFactory.getInstance();
        configuration = factory.configure();
        String folder = configuration.getStorage();
        if (folder.isEmpty()) {
            folder = System.getProperty("user.home");
        }
        downloadFolder.setText(folder);
        port.setText(String.valueOf(configuration.getPort()));
        password.setText(configuration.getPassword());
        host.setText(configuration.getHost());

        indicator = new BusyIndicator(locale.get("ui.controller.008"), locale.get("ui.controller.007"), Toolkit.stage);
        initTable();
        upload = new Upload(uploadFilesModel, historyFilesModel, service);
        download = new Download(downloadFilesModel, historyFilesModel, service);
        list = new FileList(sharedFilesModel);
        Toolkit.stage.setOnCloseRequest(event -> {
            event.consume();
            confirm(locale.get("ui.alert.title"), locale.get("ui.alert.message"), Toolkit.stage,
                    response -> {
                        if (response == ButtonType.OK) {
                            upload.dispose();
                            download.dispose();
                            String hostString = host.getText();
                            String portString = port.getText();
                            String storageString = downloadFolder.getText();
                            String passwordString = password.getText();
                            int iPort = configuration.getPort();
                            if (Toolkit.isPort(portString)) {
                                iPort = Integer.parseInt(portString);
                            }
                            factory.apply(hostString, storageString, passwordString, iPort);
                            factory.save();
                            System.exit(0);
                        }
                    }
            );
        });

    }

    /**
     * 初始化表格
     */
    private void initTable() {
        createFilesCells1(sharedFiles, MenuType.NORMAL);
        sharedFilesModel = DisplayValueModel.createInstance();
        sharedFiles.setItems(sharedFilesModel.data());
        sharedFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        createFilesCells2(downloadFiles, MenuType.DOWNLOAD);
        downloadFilesModel = DisplayValueModel.createInstance();
        downloadFiles.setItems(downloadFilesModel.data());

        createFilesCells2(uploadFiles, MenuType.UPLOAD);
        uploadFilesModel = DisplayValueModel.createInstance();
        uploadFiles.setItems(uploadFilesModel.data());

        createFilesCells2(historyFiles, MenuType.HISTORY);
        historyFilesModel = DisplayValueModel.createInstance();
        historyFiles.setItems(historyFilesModel.data());
        historyFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * 创建单元格类型1
     *
     * @param tableView
     */
    private void createFilesCells1(TableView<DisplayValue> tableView, int state) {
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
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(112);
        column.setText(locale.get("ui.controller.column20"));
        column.setCellValueFactory(new PropertyValueFactory<>("catalog"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(130);
        column.setText(locale.get("ui.controller.column30"));
        column.setCellValueFactory(new PropertyValueFactory<>("size"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);
    }

    /**
     * 创建单元格类型2
     *
     * @param tableView
     */
    private void createFilesCells2(TableView<DisplayValue> tableView, int state) {
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
        column.setPrefWidth(280);
        column.setText(locale.get("ui.controller.column1"));
        column.setCellValueFactory(new PropertyValueFactory<>("name"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(80);
        column.setText(locale.get("ui.controller.column5"));
        column.setCellValueFactory(new PropertyValueFactory<>("elapsed"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(100);
        column.setText(locale.get("ui.controller.column4"));
        column.setCellValueFactory(new PropertyValueFactory<>("speed"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);

        TableColumn<DisplayValue, Double> columnProgress = new TableColumn<>();
        columnProgress.setEditable(false);
        columnProgress.setResizable(false);
        column.setSortable(false);
        columnProgress.setPrefWidth(180);
        columnProgress.setText(locale.get("ui.controller.column21"));
        columnProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        columnProgress.setCellFactory(column1 -> new DefaultProgressBarTableCell<>());
        columns.add(columnProgress);

        column = new TableColumn<>();
        column.setEditable(false);
        column.setResizable(false);
        column.setPrefWidth(102);
        column.setText(locale.get("ui.controller.column31"));
        column.setCellValueFactory(new PropertyValueFactory<>("percent"));
        column.setCellFactory(column1 -> new DefaultTableCell<>(state, this));
        columns.add(column);
    }

    /**
     * 禁用按钮
     *
     * @param disable
     */
    private void disableButton(boolean disable) {
        btnAddToList.setDisable(disable);
        btnRefreshSharedFiles.setDisable(disable);
        btnChooseDownloadFolder.setDisable(disable);
        btnConnect.setDisable(disable);
        btnDownload.setDisable(disable);
        btnUpload.setDisable(disable);
        btnOpenFolder.setDisable(disable);
        btnClearHistoryFiles.setDisable(disable);
        btnAddToUploadList.setDisable(disable);
        btnAddToListAll.setDisable(disable);
        btnRemoveHistory.setDisable(disable);
        btnCancelDownload.setDisable(disable);
        btnCancelUpload.setDisable(false);
    }

    /**
     * 修改按钮状态（连接服务器按钮）
     *
     * @param state
     */
    private void updateButtonState(int state) {
        Platform.runLater(() -> {
            indicator.hide();
            disableButton(false);
            if (state == Socket.SESSION_OFF) {
                btnConnect.setText(locale.get("ui.controller.001"));
                information(locale.get("ui.information.title"), locale.get("ui.controller.004"), stage, null);
            } else {
                btnConnect.setText(locale.get("ui.controller.002"));
            }
        });
    }

    @Override
    public void exceptionCaught(final String exception) {

        Platform.runLater(() -> {
            indicator.hide();
            disableButton(false);
            information(locale.get("ui.information.title"), exception, stage, null);
        });
    }

    @Override
    public void sessionOpen(Channel channel) {
        send(channel, Common.LIST_FILES, DigestUtils.md5Hex(configuration.getPassword()));
    }

    @Override
    public void sessionEnd(Channel channel) {
        download.flush();
    }

    @Override
    public void stateChanged(int state) {
        updateButtonState(state);
    }

    @Override
    public void messageReceived(Channel channel, Object msg) {
        if (msg instanceof Message.Response) {
            Message.Response response = (Message.Response) msg;
            String kind = response.getKind();
            if (kind == null) {
                return;
            }
            switch (kind) {
                case Common.LIST_FILES:
                    list.messageReceived(channel, response);
                    break;
                case Common.FLUSH_UPLOAD_CACHE:
                case Common.UPLOAD_FILES:
                    upload.messageReceived(channel, response);
                    break;
                case Common.DOWNLOAD_FILES:
                    download.messageReceived(channel, response);
                    break;
            }
        }
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
                onAddToListAll(null);
                break;
            case MenuAction.ACTION2:
                onAddToList(null);
                break;
            case MenuAction.ACTION3:
                onRefreshFiles(null);
                break;
            case MenuAction.ACTION4:
                onDownload(null);
                break;
            case MenuAction.ACTION5:
                onCancelDownload(null);
                break;
            case MenuAction.ACTION6:
                onAddToUploadList(null);
                break;
            case MenuAction.ACTION7:
                onUpload(null);
                break;
            case MenuAction.ACTION8:
                onCancelUpload(null);
                break;
            case MenuAction.ACTION9:
                onOpenFolder(null);
                break;
            case MenuAction.ACTION10:
                onClearHistoryFiles(null);
                break;
            case MenuAction.ACTION11:
                onRemoveHistory(null);
                break;

        }
    }
}
