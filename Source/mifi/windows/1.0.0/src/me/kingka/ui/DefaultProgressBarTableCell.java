package me.kingka.ui;

import javafx.geometry.Insets;
import javafx.scene.control.cell.ProgressBarTableCell;

/**
 * 表格进度条单元格
 *
 * @author swift.apple
 */
public class DefaultProgressBarTableCell<S, T> extends ProgressBarTableCell<T> {
    public DefaultProgressBarTableCell() {
        super();
        setPadding(new Insets(8, 8, 8, 8));
    }
}
