package me.kingka.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.cell.TextFieldTableCell;
import me.kingka.locale.I18n;

/**
 * 文本单元格
 *
 * @author swift.apple
 * @param <S>
 * @param <T>
 */
public class DefaultTableCell<S, T> extends TextFieldTableCell<S, T> {

    private I18n locale;
    private ContextMenuFactory factory;

    public DefaultTableCell(EventHandler<ActionEvent> handler) {
        super();
        createMenuItem(handler);

    }

    /**
     * 创建菜单项
     *
     * @param state
     * @param handler
     */
    private void createMenuItem(EventHandler<ActionEvent> handler) {
        if (locale == null) {
            locale = I18n.getInstance();
        }
        if (factory == null) {
            factory = ContextMenuFactory.getInstance();
        }
        ContextMenuFactory.MenuItem item1 = ContextMenuFactory.MenuItem.build("menu.1", locale.get("menu.1"), handler);
        ContextMenuFactory.MenuItem item2 = ContextMenuFactory.MenuItem.build("menu.2", locale.get("menu.2"), handler);
        ContextMenuFactory.MenuItem item3 = ContextMenuFactory.MenuItem.build("menu.3", locale.get("menu.3"), handler);
        setContextMenu(factory.build(item2, item1, item3));
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setAlignment(Pos.CENTER);
    }
}
