package com.test.HowlFirebaseAuth;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by admin on 2017/10/03.
 */

public class MyBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MyFirebaseMessagingService.class);
        // サービス起動
        startWakefulService(context, serviceIntent);
    }
}
