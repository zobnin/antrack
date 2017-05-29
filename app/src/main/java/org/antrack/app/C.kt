package org.antrack.app

object C {
    val DROPBOX_KEY = "otyoz3pz5z9rtmn"

    val APP_NAME = "org.antrack.app"
    val UPDATE_INTERVAL = "30"
    val UPDATE_INTERVALS = arrayOf("15", "30", "60", "120", "180")
    val LOGS_MAX = 1000
    val DB_LONGPOLL_TIMEOUT = 300

    val DEVICES_DIR = "/devices/"
    val MODULES_DIR = "/modules/"
    val ODEX_DIR = "/odex/"

    val NAME_FILE = "/name"
    val MODULES_FILE = "/modules"
    val SETTINGS_FILE = "/settings"
    val FEATURES_FILE = "/features"
    val CONTROL_FILE = "/ctl"
    val RESULT_FILE = "/result"
    val OSID_FILE = "/osid"
    val PUBLIC_KEY_FILE = "/public_key"
    val PRIVATE_KEY_FILE = "/.private_key"
    val TOKEN_FILE = "/.token"
    val WIZARD_COMPLETE_FILE = "/.wizard_complete"
    val TRUSTED_DEVICES_FILE = "/trusted"

    val BOOTSTRAP_ASSET = "bootstrap"
    val ALARM_ASSET = "alarm.ogg"

    val CONTROL_Q_FILE = "/ctlq"
    val CONTROL_Q_MAX_LENGTH = 10

    val MODULES_JSON_FILE = "/modules.json"

    val MAIN_LOG_FILE = "/logs"

    val NOWIPE_FILE = "/.nowipe"

    val DEFAULT_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss"
    val ACCURATE_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss.SSS"
    val LAST_CMD_TIME_FORMAT = "yyyyMMddHHmmssSSS"

    val TRUE = "true"
    val FALSE = "false"
    val DONE = "done"
    val ON = "on"
    val OFF = "off"

    val ACTION_ALARM = "alarm"
    val ACTION_BOOT = "boot"
    val ACTION_SCREENON = "screenOn"
    val ACTION_OUTGOINGCALL = "outgoingCall"
    val ACTION_INCOMINGCALL = "incomingCall"
    val ACTION_COMMAND = "command"
    val ACTION_PUSH = "push"
    val ACTION_CTL_ENABLED = "ctlEnabled"
    val ACTION_CTL_DISABLED = "ctlDisabled"
    val ACTION_AUTH_DEVICE = "authDevice"
    val ACTION_BAN_DEVICE = "banDevice"

    val S_USE_ADMIN = "useAdmin" // false
    val S_USE_ROOT = "useRoot" // false
    val S_UPDATE_INTERVAL = "updateInterval" // 30
    val S_PLUGIN = "plugin" // null
    val S_START_AT_BOOT = "startAtBoot" // true
    val S_ENABLE_SERVICE = "enableService" // true
    val S_HIDDEN = "hidden" // false
    val S_LOST = "lost" // false
    val S_SCREEN_ON_PHOTO = "screenOnPhoto" // false
    val S_BACKUP_PHONE = "backupPhone" // null
    val S_SMS_ON_SIM_CHANGE = "smsOnSimChange" // false
    val S_IMSI = "IMSI" // null
    val S_SYSTEM_APP = "systemApp" // false
    val S_SHOW_HELP = "showHelp" // true
    val S_LAST_CMD_TIME = "lastCmdId" // null
    val S_ENABLE_CTL = "enableCtl" // false
    val S_ENABLE_UPLOADER = "enableUploader" // true
}
