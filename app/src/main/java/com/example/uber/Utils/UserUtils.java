package com.example.uber.Utils;

import android.view.View;

import com.example.uber.Common;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserUtils {

    public static void updateUser(View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnSuccessListener(aVoid -> Snackbar.make(view, "Information update successfully", Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show());
    }
}
