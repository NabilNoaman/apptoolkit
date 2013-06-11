package com.mcxiaoke.appmanager.fragment;

/**
 * Project: filemanager
 * Package: com.mcxiaoke.appmanager.fragment
 * User: mcxiaoke
 * Date: 13-6-11
 * Time: 上午10:53
 */
public interface Refreshable {

    public void refresh();

    public void showProgressIndicator();

    public void hideProgressIndicator();
}
