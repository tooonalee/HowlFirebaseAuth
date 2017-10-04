package com.test.HowlFirebaseAuth;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by admin on 2017/10/03.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService{

    private static final String TAG = "MyFirebaseIDService";

    @Override
    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        Log.d(TAG, "ON!!!!!!!!!!");


        acquireCpuWakeLock();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    private void acquireCpuWakeLock() {
        PowerManager.WakeLock cpuWakeLock;

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "hi");

        cpuWakeLock.acquire(5000);

/*        if (cpuWakeLock != null) {
            cpuWakeLock.release();
        }*/
    }
}
