package org.antrack.app.ui;

import org.antrack.app.Features;
import org.antrack.app.ui.fragments.BaseFragment;

import java.util.LinkedHashMap;

public class State {
    public static boolean firstRun = true;
    public static boolean initDone = false;
    public static boolean deviceMenuActive = false;

    public static Device device;
    public static Features features;
    public static LinkedHashMap<String, Module> modules;
    public static BaseFragment fragment;
    public static String menuItemTitle;
}
