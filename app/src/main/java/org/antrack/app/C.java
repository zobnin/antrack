package org.antrack.app;

public class C {
    public static final String DROPBOX_KEY = "otyoz3pz5z9rtmn";

    public static final String APP_NAME = "org.antrack.app";
    public static final String UPDATE_INTERVAL = "30";
    public static final String[] INTERVALS = {"15", "30", "60", "120", "180"};
    public static final int LOGS_MAX = 1000;
    public static final int DB_LONGPOLL_TIMEOUT = 300;

    public static final String DEVICES_DIR = "/devices/";
    public static final String MODULES_DIR = "/modules/";
    public static final String ODEX_DIR = "/odex/";

    public static final String NAME_FILE = "/name";
    public static final String MODULES_FILE = "/modules";
    public static final String SETTINGS_FILE = "/settings";
    public static final String FEATURES_FILE = "/features";
    public static final String CONTROL_FILE = "/ctl";
    public static final String RESULT_FILE = "/result";
    public static final String OSID_FILE = "/osid";
    public static final String PUBLIC_KEY_FILE = "/public_key";
    public static final String PRIVATE_KEY_FILE = "/.private_key";
    public static final String TOKEN_FILE = "/.token";
    public static final String WIZARD_COMPLETE_FILE = "/.wizard_complete";

    public static final String BOOTSTRAP_ASSET = "bootstrap";
    public static final String ALARM_ASSET = "alarm.ogg";

    public static final String CONTROL_Q_FILE = "/ctlq";
    public static final int CONTROL_Q_MAX_LENGTH = 10;

    public static final String MODULES_JSON_FILE = "/modules.json";

    public static final String MAIN_LOG_FILE = "/logs";

    public static final String NOWIPE_FILE = "/.nowipe";

    public static final String DEFAULT_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss";
    public static final String ACCURATE_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss.SSS";
    public static final String LAST_CMD_TIME_FORMAT = "yyyyMMddHHmmssSSS";

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String DONE = "done";
    public static final String ON = "on";
    public static final String OFF = "off";

    public static final String ACTION_ALARM = "alarm";
    public static final String ACTION_BOOT = "boot";
    public static final String ACTION_SCREENON = "screenOn";
    public static final String ACTION_OUTGOINGCALL = "outgoingCall";
    public static final String ACTION_INCOMINGCALL = "incomingCall";
    public static final String ACTION_COMMAND = "command";
    public static final String ACTION_PUSH = "push";

    public static final String S_USE_ADMIN = "useAdmin"; // false
    public static final String S_USE_ROOT = "useRoot"; // false
    public static final String S_UPDATE_INTERVAL = "updateInterval"; // 30
    public static final String S_PLUGIN = "plugin"; // null
    public static final String S_START_AT_BOOT = "startAtBoot"; // true
    public static final String S_ENABLE_SERVICE = "enableService"; // true
    public static final String S_HIDDEN = "hidden"; // false
    public static final String S_LOST = "lost"; // false
    public static final String S_SCREEN_ON_PHOTO = "screenOnPhoto"; // false
    public static final String S_BACKUP_PHONE = "backupPhone"; // null
    public static final String S_SMS_ON_SIM_CHANGE = "smsOnSimChange"; // false
    public static final String S_IMSI = "IMSI"; // null
    public static final String S_SYSTEM_APP = "systemApp"; // false
    public static final String S_SHOW_HELP = "showHelp"; // true
    public static final String S_LAST_CMD_TIME = "lastCmdId"; // null
    public static final String S_ENABLE_CTL = "enableCtl"; // false
}
