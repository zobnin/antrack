package org.antrack.app;

import com.onesignal.OneSignal;

import org.antrack.app.libs.Files;
import org.antrack.app.libs.L;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class OSignal {
    public static void writeId() {
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                try {
                    Files.writeTextFile(Init.MAIN_DIR + C.OSID_FILE, userId);
                } catch (IOException e) {
                    L.e("OSignal", "error: " + e);
                }
            }
        });
    }

    public static void push(String userId) {
        L.d("OSignal", "push to " + userId);

        try {
            OneSignal.postNotification(new JSONObject("{'contents': {'en':'ping'}, 'include_player_ids': ['" + userId + "'], 'priority':10}"),
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            L.d("OSignal", "postNotification Success: " + response.toString());
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            L.e("OSignal", "postNotification Failure: " + response.toString());
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
