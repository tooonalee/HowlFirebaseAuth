package com.test.HowlFirebaseAuth;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.common.api.Response;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by admin on 2017/10/03.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("TAG", "Refreshed token: " + refreshedToken);

        Map<String, String> data = remoteMessage.getData();

        String msg = data.get("title");

        //Log.d("TAG", "remoteMessage.getNotification : " + remoteMessage.getNotification());
        System.out.println("안드로이드 파이어베이스");
       // Log.d("TAG", "Notification data: " + remoteMessage.getData().get("hoge"));
        // Turn on the screen for notification

//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//
//        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
//        wl.acquire();
//        PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
//        wl_cpu.acquire();

        // 이 부분이 바로 화면을 깨우는 부분 되시겠다.
        // 화면이 잠겨있을 때 보여주기

        //AlarmAlertWakeLock.acquireCpuWakeLock(getApplicationContext());

        showNotification(getApplicationContext(), remoteMessage, msg);

    }

    public void showNotification(Context context, RemoteMessage remoteMessage, String msg){

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE );
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG" );
        wakeLock.acquire(3000);

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //클릭할 때까지 액티비티 실행을 보류하고 있는 PendingIntent 객체 생성
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        //사운드 기능 추가
        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("NOTIFICATION");
        notificationBuilder.setContentText(msg /*remoteMessage.getNotification().getBody()*/);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setSound(soundUri);
        notificationBuilder.setVibrate(new long[] {100,200,300,400}); // pattern의 첫번째 파라미터는 wait시간, 두번째는 진동시간(단위 ms)

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(0, notificationBuilder.build());

/*        //NotificationManage 생성
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //NotificationManager가 알림(Notification)을 표시, id는 알림구분용
        notificationManager.notify(0, notificationBuilder.build());*/

    }

    private void sendNotification(){

    }


}


