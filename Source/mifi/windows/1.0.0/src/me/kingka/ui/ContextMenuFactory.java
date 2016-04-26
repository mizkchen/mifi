package me.kingka.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;

/**
 * 菜单构建工厂
 *
 * @author swift.apple
 */
public class ContextMenuFactory {

    private static ContextMenuFactory instance;

    private ContextMenuFactory() {

    }

    /**
     * 获取唯一实例
     *
     * @return
     */
    public static synchronized ContextMenuFactory getInstance() {
        if (instance == null) {
            instance = new ContextMenuFactory();
        }
        return instance;
    }

    /**
     * 创建菜单
     *
     * @param items 菜单项
     * @return
     */
    public ContextMenu build(MenuItem... items) {
        ContextMenu menu = new ContextMenu();
        for (MenuItem item : items) {
            javafx.scene.control.MenuItem menuItem = new javafx.scene.control.MenuItem();
            menuItem.setOnAction(item.getHandler());
            menuItem.setText(item.getLabel());
            menuItem.setId(item.getId());
            menu.getItems().add(menuItem);
        }
        return menu;
    }

    /**
     * 菜单项
     *
     * @author swift.apple
     */
    public static class MenuItem {

        private String id;

        /**
         * id
         *
         * @return
         */
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        /**
         * 文本
         *
         * @return
         */
        public String getLabel() {
            return label;
        }

        /**
         * 事件处理器
         *
         * @return
         */
        public EventHandler<ActionEvent> getHandler() {
            return handler;
        }

        private String label;
        private EventHandler<ActionEvent> handler;

        /**
         * 菜单项
         *
         * @param id
         * @param label
         * @param handler
         */
        public MenuItem(String id, String label, EventHandler<ActionEvent> handler) {
            this.label = label;
            this.handler = handler;
            this.id = id;
        }

        /**
         * 创建菜单项
         *
         * @param id
         * @param label
         * @param handler
         * @return
         */
        public static MenuItem build(String id, String label, EventHandler<ActionEvent> handler) {
            MenuItem item = new MenuItem(id, label, handler);
            return item;
        }
    }
}
