package com.example.uber;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uber.Model.DriverInfoModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";
    private final static int LOGIN_REQUEST_CODE = 711;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener listener;

    private FirebaseDatabase database;
    private DatabaseReference driverIfnoRef;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

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
        setContentView(R.layout.activity_splash_screen);
        init();
    }

    private void init() {

        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        driverIfnoRef = database.getReference(Common.DRIVER_INFO_REFERANCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        auth = FirebaseAuth.getInstance();

        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null)
                checkUserFromDatabase();
            else
                showLoginLayout();
        };
    }

    private void checkUserFromDatabase() {
        driverIfnoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(SplashScreenActivity.this, "User already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                        Toast.makeText(SplashScreenActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DilaogTheme);

        View view = LayoutInflater.from(this).inflate(R.layout.layout_register, null);

        TextInputLayout firstNameLayout = view.findViewById(R.id.edt_firts_name);
        TextInputLayout lastNameLayout = view.findViewById(R.id.edt_last_name);
        TextInputLayout phoneNumberLayout = view.findViewById(R.id.edt_phone_number);
        Button btnContinue = view.findViewById(R.id.btn_continue);

        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null
                && TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
            phoneNumberLayout.getEditText()
                    .setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnContinue.setOnClickListener(v -> {
            if (TextUtils.isEmpty(firstNameLayout.getEditText().getText().toString())) {
                firstNameLayout.setError("First name can't be empty");
                return;
            } else if (TextUtils.isEmpty(lastNameLayout.getEditText().getText().toString())) {
                lastNameLayout.setError("Last name can't be empty");
                return;
            } else if (TextUtils.isEmpty(phoneNumberLayout.getEditText().getText().toString())) {
                phoneNumberLayout.setError("Phone number can't be empty");
                return;
            } else {
                DriverInfoModel driver = new DriverInfoModel();
                driver.setFistName(firstNameLayout.getEditText().getText().toString());
                driver.setLastName(lastNameLayout.getEditText().getText().toString());
                driver.setPhoneNumber(phoneNumberLayout.getEditText().getText().toString());
                driver.setRate(0.0);


                driverIfnoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(driver)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Registerd Seuccsusfily", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }


        });
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