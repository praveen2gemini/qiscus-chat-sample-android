package com.qiscus.chat.sample.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.qiscus.sdk.service.QiscusFirebaseIdService;

/**
 * Created on : May 16, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class FirebaseIdService extends QiscusFirebaseIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d("FirebaseIdService", "Fcm token: " + FirebaseInstanceId.getInstance().getToken());

        //Code for specific qisme apps here
    }
}