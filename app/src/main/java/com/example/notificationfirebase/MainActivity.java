package com.example.notificationfirebase;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notificationfirebase.API.ApiFCM;
import com.example.notificationfirebase.model.fcm.FcmNotificationPayload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };

    private static final String TAG = "MainActivity";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted",Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                            Toast.LENGTH_LONG).show();
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView etToken = findViewById(R.id.etToken);
        Button btnSend = findViewById(R.id.btnSend);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etBody = findViewById(R.id.etBody);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = token;
                        Log.d(TAG, msg);
                        etToken.setText(msg);
                    }
                });
        askNotificationPermission();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a ="";
                try {
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                .permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                        //your codes here
                        a = getAccessToken();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiFCM.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
                ApiFCM api = retrofit.create(ApiFCM.class);

                FcmNotificationPayload fcmNotificationPayload = new FcmNotificationPayload();
                FcmNotificationPayload.Message message = new FcmNotificationPayload.Message();
                FcmNotificationPayload.Message.Notification notification = new FcmNotificationPayload.Message.Notification();

                notification.setBody(etBody.getText().toString());
                notification.setTitle(etTitle.getText().toString());
                message.setNotification(notification);

                //hardcode terget device token
                message.setToken("fKd_KI1ORr6ofgBag5ksfW:APA91bFoMttNDNQtpIRjFS_lIjAJNTcxq5Yye4nrOEJ7ELPN7pI6574FmjWgIqZENkvYWcp_53r5cA4VcZcUErscULiMAjePmpJq0cFYal1kbBnY7_SjqNUthN6eObMltGqNH_4m7sSV");
                fcmNotificationPayload.setMessage(message);


                Call<ResponseBody>call = api.sendChatNotification("Bearer "+a, fcmNotificationPayload);

                call.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        Log.d("TAG", "onResponse: "+response);
                        Toast.makeText(MainActivity.this, "Notification Sent", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("TAG", "onFailure: "+t);
                        Toast.makeText(MainActivity.this, "gagal", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }

    private String getAccessToken() throws IOException {
        AssetManager assManager = getApplicationContext().getAssets();
        InputStream is = null;
        try {
            is = assManager.open("service-account.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream caInput = new BufferedInputStream(is);

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(caInput)
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

}