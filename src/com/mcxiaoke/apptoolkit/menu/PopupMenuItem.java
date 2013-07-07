package com.mcxiaoke.apptoolkit.menu;

/**
 * Project: filemanager
 * Package: com.mcxiaoke.appmanager.model
 * User: mcxiaoke
 * Date: 13-6-12
 * Time: 下午11:01
 */
public class PopupMenuItem {

    public int id;
    public int type;
    public String name;
    public boolean enabled;
    public boolean root;
    public boolean advanced;

    public PopupMenuItem(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
