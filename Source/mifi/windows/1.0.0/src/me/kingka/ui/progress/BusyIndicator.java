package me.kingka.ui.progress;

import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 进度窗口
 *
 * @author swift.apple
 */
public class BusyIndicator extends Control {

    public BusyIndicator(String title,String message, Stage stage) {
        initialize(title,message, stage);
    }

    private Stage stage;
    private Label label;
    private ProgressBar progressBar;
    private Timeline animation;

    /**
     * 初始化
     *
     * @param message
     * @param primaryStage
     */
    private void initialize(String title,String message, Stage primaryStage) {
        stage = new Stage();
        AnchorPane pane = new AnchorPane();
        pane.setPrefWidth(200);
        pane.setPrefHeight(120);
        progressBar = new ProgressBar();
        progressBar.setPrefSize(172, 27);
        progressBar.setLayoutX(14);
        progressBar.setLayoutY(42);
        pane.getChildren().add(progressBar);
        label = new Label();
        label.setPrefSize(172, 15);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setLayoutX(14);
        label.setLayoutY(78);
        label.setTextFill(Paint.valueOf("#ff0000"));
        label.setText(message == null ? "" : message);
        pane.getChildren().add(label);
        Scene scene = new Scene(pane, 200, 120);
        URL styleUrl=getClass().getResource("app.css");
        String url=styleUrl.toExternalForm();
        scene.getStylesheets().add(url);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle(title);
        stage.setOnCloseRequest((event) -> event.consume());
        animation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0)
                ),
                new KeyFrame(
                        Duration.seconds(2.5),
                        new KeyValue(progressBar.progressProperty(), 1)
                )
        );
        animation.setCycleCount(Timeline.INDEFINITE);
    }

    public void show() {
        try {
            animation.playFromStart();
            this.stage.show();
        } catch (Exception e) {
        }
    }

    public void hide() {
        stage.hide();
        animation.stop();
    }


}
