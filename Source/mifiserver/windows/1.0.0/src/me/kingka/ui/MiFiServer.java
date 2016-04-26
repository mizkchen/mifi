package me.kingka.ui;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.kingka.locale.I18n;
import me.kingka.network.configuration.ConfigurationFactory;
import me.kingka.utils.Toolkit;
import org.apache.log4j.Logger;

/**
 * 启动程序
 *
 * @author swift.apple
 */
public class MiFiServer extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Toolkit.stage = stage;
            final I18n locale = I18n.getInstance().load("zh_CN");
            ConfigurationFactory.getInstance().setConfigPath("mifi.server.json").load();
            Parent root = FXMLLoader.load(getClass().getResource("ui.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(locale.get("ui.title"));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.show();
        } catch (NullPointerException | IOException e) {
            Logger.getLogger(MiFiServer.class).debug(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
