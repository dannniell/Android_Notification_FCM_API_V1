package com.example.notificationfirebase.API;

import com.example.notificationfirebase.model.fcm.FcmNotificationPayload;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiFCM {
    String BASE_URL="https://fcm.googleapis.com/";

    @Headers({"Content-Type:application/json"})
    @POST("v1/projects/notification-firebase-976f9/messages:send")
    Call<ResponseBody> sendChatNotification(@Header("Authorization") String authHeader, @Body FcmNotificationPayload fcmNotificationPayload);
}