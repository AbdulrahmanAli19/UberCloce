package com.example.uber;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.uber.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverHomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 202;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;

    private Uri imageUri;

    private AppBarConfiguration mAppBarConfiguration;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;

    private CircleImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                imgAvatar.setImageURI(imageUri);
                showDialogUpload();
            }
        }
    }

    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
        builder.setCancelable(false)
                .setTitle("Change avatar")
                .setMessage("Do you really want to change your avatar?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Upload", (dialog, which) -> {
                    waitingDialog.setMessage("uploading...");
                    waitingDialog.show();

                    String uniqueName = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    StorageReference avatarFolder = storageReference.child("avatars/" + uniqueName);

                    avatarFolder.putFile(imageUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            avatarFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                Map<String, Object> updateData = new HashMap<>();
                                updateData.put("avatar", uri.toString());
                                UserUtils.updateUser(drawer, updateData);

                            });
                        }
                        waitingDialog.dismiss();
                    }).addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        waitingDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                    }).addOnFailureListener(e -> {
                        waitingDialog.dismiss();
                        Snackbar.make(drawer, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorAccent));
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        });
        dialog.show();
    }

    private void init() {

        waitingDialog = new AlertDialog.Builder(DriverHomeActivity.this)
                .setCancelable(false)
                .setTitle("Waiting...")
                .create();

        storageReference = FirebaseStorage.getInstance().getReference();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_sing_out) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DriverHomeActivity.this);
                builder.setTitle("Sign out")
                        .setMessage("Do you really want to sign out?")
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Sign out", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(DriverHomeActivity.this, SplashScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }).setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(d -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.colorAccent));
                });
                dialog.show();
            }
            return true;
        });

        //set data for the user
        View headerView = navigationView.getHeaderView(0);
        TextView txtName = headerView.findViewById(R.id.txt_name);
        TextView txtPhone = headerView.findViewById(R.id.txt_phone);
        TextView txtRate = headerView.findViewById(R.id.txt_rate);
        imgAvatar = headerView.findViewById(R.id.img_avatar);
        ProgressBar progressBar = headerView.findViewById(R.id.progress_bar);

        txtName.setText(Common.buildWelocmeMessage());
        txtPhone.setText(Common.currentUser != null ? Common.currentUser.getPhoneNumber() : "");
        txtRate.setText(Common.currentUser != null ? Common.currentUser.getRate() + "" : "0.0");

        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        if (Common.currentUser != null && Common.currentUser.getAvatar() != null
                && !TextUtils.isEmpty(Common.currentUser.getAvatar())) {
            Picasso.get().load(Common.currentUser.getAvatar())
                    .into(imgAvatar, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            imgAvatar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(getCurrentFocus(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}