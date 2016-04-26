package me.kingka.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.cell.TextFieldTableCell;
import me.kingka.locale.I18n;

/**
 * 表格文本单元格
 *
 * @author swift.apple
 * @param <S>
 * @param <T>
 */
public class DefaultTableCell<S, T> extends TextFieldTableCell<S, T> {
    private I18n locale;
    private ContextMenuFactory factory;

    public DefaultTableCell(int state, EventHandler<ActionEvent> handler) {
        super();
        createMenuItem(state, handler);

    }

    /**
     * 创建菜单项
     *
     * @param state
     * @param handler
     */
    private void createMenuItem(int state, EventHandler<ActionEvent> handler) {
        if (locale == null) {
            locale = I18n.getInstance();
        }
        if (factory == null) {
            factory = ContextMenuFactory.getInstance();
        }
        switch (state) {
            case MenuType.NORMAL:
                ContextMenuFactory.MenuItem item1 = ContextMenuFactory.MenuItem.build("menu.1", locale.get("menu.1"), handler);
                ContextMenuFactory.MenuItem item2 = ContextMenuFactory.MenuItem.build("menu.2", locale.get("menu.2"), handler);
                ContextMenuFactory.MenuItem item3 = ContextMenuFactory.MenuItem.build("menu.3", locale.get("menu.3"), handler);
                setContextMenu(factory.build(item2, item1, item3));
                break;
            case MenuType.DOWNLOAD:
                ContextMenuFactory.MenuItem item4 = ContextMenuFactory.MenuItem.build("menu.4", locale.get("menu.4"), handler);
                ContextMenuFactory.MenuItem item5 = ContextMenuFactory.MenuItem.build("menu.5", locale.get("menu.5"), handler);
                setContextMenu(factory.build(item4, item5));
                break;
            case MenuType.UPLOAD:
                ContextMenuFactory.MenuItem item6 = ContextMenuFactory.MenuItem.build("menu.6", locale.get("menu.6"), handler);
                ContextMenuFactory.MenuItem item7 = ContextMenuFactory.MenuItem.build("menu.7", locale.get("menu.7"), handler);
                ContextMenuFactory.MenuItem item8 = ContextMenuFactory.MenuItem.build("menu.8", locale.get("menu.8"), handler);
                setContextMenu(factory.build(item6, item7, item8));
                break;
            case MenuType.HISTORY:
                ContextMenuFactory.MenuItem item9 = ContextMenuFactory.MenuItem.build("menu.9", locale.get("menu.9"), handler);
                ContextMenuFactory.MenuItem item10 = ContextMenuFactory.MenuItem.build("menu.10", locale.get("menu.10"), handler);
                ContextMenuFactory.MenuItem item11 = ContextMenuFactory.MenuItem.build("menu.11", locale.get("menu.11"), handler);
                setContextMenu(factory.build(item9, item10, item11));
                break;
        }
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setAlignment(Pos.CENTER);
    }
}
