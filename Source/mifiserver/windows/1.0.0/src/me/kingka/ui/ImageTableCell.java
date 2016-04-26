package me.kingka.ui;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 图片单元格
 *
 * @author swift.apple
 */
public class ImageTableCell<S, T> extends TableCell<S, T> {
    private ImageView image;

    public ImageTableCell() {
        super();
        image = new ImageView();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            image.setFitWidth(24.0);
            image.setFitHeight(24.0);
            String imageFile = String.format("images/%s.png", item);
            image.setImage(new Image(imageFile));
            this.setAlignment(Pos.CENTER);
            setGraphic(image);
        }
    }
}
