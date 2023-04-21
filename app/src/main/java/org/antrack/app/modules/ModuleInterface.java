package org.antrack.app.modules;

import android.content.Context;

public interface ModuleInterface {
    // Module version (major.minor)
    String version();

    // Module author
    String author();

    // Short module description
    String desc();

    // When start module?
    // boot, load, command, alarm, screenon, incomingcall, outgoingcall
    String[] startWhen();

    // Remote command to run module ("null" if it don't have command)
    String command();

    // Result file or dir ("null" if none)
    // File example: "/contacts"
    // Dir example: "/camera/"
    String result();

    // Type of result
    String resultType();

    // If module uses root rights this method must return true
    boolean usesRoot();

    // If module uses device administrator this method must return true
    boolean usesAdmin();

    // Called when phone booted
    void onBoot(Context context);

    // Called when module loaded
    void onLoad(Context context);

    // Called when AnTrack get remote command to run module
    String onCommand(Context context, String[] args);

    // Called when triggers alarm
    void onAlarm(Context context);

    // Called when screen is on
    void onScreenOn(Context context);

    // Called on incoming call
    void onIncomingCall(Context context, String phoneNumber);

    // Called on outgoing call
    void onOutgoingCall(Context context, String phoneNumber);
}
