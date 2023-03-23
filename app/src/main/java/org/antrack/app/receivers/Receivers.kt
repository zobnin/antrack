package org.antrack.app.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import org.antrack.app.App

class Receivers {
    private var registered = false

    private val phoneStateReceiver = PhoneStateReceiver()
    private val screenOnReceiver = ScreenOnReceiver()
    private val simChangeReceiver = SimChangeReceiver()

    @SuppressLint("InlinedApi")
    fun registerPersistentReceivers() {
        if (registered) return

        registerExtReceiver(phoneStateReceiver, IntentFilter().apply {
            addAction("android.intent.action.PHONE_STATE")
            addAction("android.intent.action.NEW_OUTGOING_CALL")
        })

        registerExtReceiver(screenOnReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
        })

        registerExtReceiver(simChangeReceiver, IntentFilter().apply {
            addAction("android.intent.action.SIM_STATE_CHANGED")
        })

        registered = true
    }

    private fun registerExtReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= 33) {
            App.context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            App.context.registerReceiver(receiver, filter)
        }
    }
}

