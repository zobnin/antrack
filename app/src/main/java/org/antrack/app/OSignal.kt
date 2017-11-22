package org.antrack.app

import com.onesignal.OneSignal

import org.antrack.app.libs.Files
import org.antrack.app.libs.L
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

object OSignal {
    fun writeId() {
        OneSignal.idsAvailable { userId, _ ->
            try {
                Files.writeTextFile(Init.MAIN_DIR + C.OSID_FILE, userId)
            } catch (e: IOException) {
                L.e("OSignal", "error: " + e)
            }
        }
    }

    fun push(userId: String, message: String) {
        L.d("OSignal", "push to " + userId)

        try {
            OneSignal.postNotification(JSONObject("{'contents': {'en':'$message'}, 'include_player_ids': ['$userId'], 'priority':10}"),
                    object : OneSignal.PostNotificationResponseHandler {
                        override fun onSuccess(response: JSONObject) {
                            L.d("OSignal", "postNotification Success: " + response.toString())
                        }

                        override fun onFailure(response: JSONObject) {
                            L.e("OSignal", "postNotification Failure: " + response.toString())
                        }
                    })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
