package org.antrack.app

import app.BuildConfig

const val TRUE = "true"
const val FALSE = "false"
const val DONE = "done"
const val ON = "on"
const val OFF = "off"

const val WIZARD_FIRST_LAUNCH_CODE = 1
const val WIZARD_LAUNCH_CODE = 2
const val DEVICE_ADMIN_CODE = 3

const val DROPBOX_KEY = BuildConfig.DROPBOX_KEY
const val LONG_POLL_TIMEOUT = 300

const val APP_NAME = "org.antrack.app"
const val DEFAULT_UPDATE_INTERVAL = 30L

const val MODULES_ASSET_DIR = "modules"
const val MODULES_DIR = "/modules/"
const val MODULES_FILE = "/modules"
const val MODULES_JSON_FILE = "/modules.json"
const val SETTINGS_FILE = "/settings"
const val FEATURES_FILE = "/features"
const val CONTROL_FILE = "/ctl"
const val CONTROL_Q_FILE = "/ctlq"
const val RESULT_FILE = "/result"

const val TESTING_FILE = "/testing"
const val TESTING_TEMP_FILE = "/testing_temp"

const val TOKEN_FILE = "/.token"
const val LOST_FILE = "/.lost"
const val WIZARD_COMPLETE_FILE = "/.wizard_complete"

const val BOOTSTRAP_ASSET = "bootstrap"
const val ALARM_ASSET = "alarm.ogg"

const val LOG_FILE = "/log"
const val LOG_TO_LOGCAT = true
const val LOG_TO_FILE = true
const val LOGS_MAX_LENGTH = 64 * 1024

const val LOG_TIME_FORMAT = "MM.dd HH:mm:ss"
const val ACCURATE_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss.SSS"
