package org.antrack.app.ui

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity

import com.android.vending.billing.IInAppBillingService

import org.antrack.app.libs.L
import org.json.JSONException
import org.json.JSONObject

open class BillingActivity : AppCompatActivity() {
    internal var mService: IInAppBillingService? = null

    internal var mServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mService = IInAppBillingService.Stub.asInterface(service)

        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"

        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            unbindService(mServiceConn)
        }
    }

    fun buyItem(sku: String) {
        try {
            // FIXME token
            val buyIntentBundle = mService!!.getBuyIntent(3, packageName,
                    sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ")

            if (buyIntentBundle.getInt("BILLING_RESPONSE_RESULT_OK") != 0) {
                return
            }

            val pendingIntent = buyIntentBundle.getParcelable<PendingIntent>("BUY_INTENT")
            startIntentSenderForResult(pendingIntent!!.intentSender,
                    1001, Intent(), 0, 0, 0)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1001) {
            val responseCode = data.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
            val dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE")

            if (resultCode == Activity.RESULT_OK) {
                try {
                    val jo = JSONObject(purchaseData)
                    val sku = jo.getString("productId")
                    L.d("Billing", "You have bought the " + sku)
                } catch (e: JSONException) {
                    L.d("Billing", "Failed to parse purchase data.")
                    e.printStackTrace()
                }

            }
        }
    }
}
