package org.antrack.app.ui;

import android.view.MenuItem;

import org.antrack.app.Features;
import org.antrack.app.ui.fragments.BaseFragment;

import java.util.LinkedHashMap;

public class State {
    public static boolean firstRun = true;
    public static boolean initDone = false;
    // For CreateDeviceMenu
    public static boolean deviceMenuActive = false;

    public static Device device;
    public static Features features;
    public static LinkedHashMap<String, Module> modules;
    public static BaseFragment fragment;
    public static String menuItemTitle;
}
