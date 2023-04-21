package org.antrack.app.service

import android.content.Intent

class IntentActionProcessor(private val cc: CommandRunner) {
    fun process(intent: Intent) {
        when (intent.action) {
            IntentActions.ACTION_BOOT -> cc.executeModules(IntentActions.ACTION_BOOT, "")
            IntentActions.ACTION_ALARM -> processAlarm()
            IntentActions.ACTION_SCREEN_ON -> cc.executeModules(IntentActions.ACTION_SCREEN_ON, "")
            IntentActions.ACTION_OUTGOING_CALL -> processOutgoingCall(intent)
            IntentActions.ACTION_INCOMING_CALL -> processIncomingCall(intent)
            IntentActions.ACTION_COMMAND -> processCommand(intent)
        }
    }

    private fun processCommand(intent: Intent) {
        val command = intent.getStringExtra(IntentActions.EXTRA_COMMAND)
        if (command != null) {
            cc.executeCtlCommand(command)
        }
    }

    private fun processIncomingCall(intent: Intent) {
        val number = intent.getStringExtra(IntentActions.EXTRA_PHONE_NUMBER)
        if (number != null)
            cc.executeModules(IntentActions.ACTION_INCOMING_CALL, number)
    }

    private fun processOutgoingCall(intent: Intent) {
        val outNumber = intent.getStringExtra(IntentActions.EXTRA_PHONE_NUMBER)
        if (outNumber != null)
            cc.executeModules(IntentActions.ACTION_OUTGOING_CALL, outNumber)
    }

    private fun processAlarm() {
        cc.executeModules(IntentActions.ACTION_ALARM, "")
    }
}