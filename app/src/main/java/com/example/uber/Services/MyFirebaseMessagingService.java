package com.example.uber.Services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.uber.Common;
import com.example.uber.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            UserUtils.updateToken(this, s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: called.");
        Map<String, String> dataRcev = remoteMessage.getData();
        if (dataRcev != null)
            Common.showNotification(this, new Random().nextInt()
                    , dataRcev.get(Common.NOTI_FILE)
                    , dataRcev.get(Common.NOTI_CONTECT), null);
    }
}
