package com.example.uber;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;

public class SplashScreenActivity extends AppCompatActivity {
    private final static int LOGIN_REQUEST_CODE = 711;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (auth != null && listener != null)
            auth.removeAuthStateListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        auth = FirebaseAuth.getInstance();

        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null)
                Toast.makeText(this, "Welcome: " + user.getUid(), Toast.LENGTH_SHORT).show();
            else
                showLoginLayout();
        };
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout pickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_sign_in_phone)
                .setGoogleButtonId(R.id.btn_sign_in_google)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(pickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginScreen)
                .setAvailableProviders(providers)
                .build(), LOGIN_REQUEST_CODE);
    }

    private void delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread())
                .subscribe(() -> auth.addAuthStateListener(listener));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "[ERROR] " + idpResponse.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}