package com.example.bdsdcna;

import androidx.annotation.NonNull;

import com.example.bdsdcna.models.History;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HistoryHelper {

    private HistoryHelper() {
    }

    public static void log(
            @NonNull String action,
            String householdId,
            String memberId,
            String targetUserId,
            String targetName,
            String field,
            String oldValue,
            String newValue
    ) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userId = "";
        String userEmail;

        if (currentUser != null) {
            userId = currentUser.getUid();
            userEmail = currentUser.getEmail() == null
                    ? ""
                    : currentUser.getEmail();
        } else {
            userEmail = "";
        }

        String historyId = UUID.randomUUID().toString();

        Map<String, Object> history = new HashMap<>();

        history.put("historyId", historyId);
        history.put("action", action);
        history.put("householdId", householdId == null ? "" : householdId);
        history.put("memberId", memberId == null ? "" : memberId);
        history.put("targetUserId", targetUserId == null ? "" : targetUserId);
        history.put("targetName", targetName == null ? "" : targetName);
        history.put("field", field == null ? "" : field);
        history.put("oldValue", oldValue == null ? "" : oldValue);
        history.put("newValue", newValue == null ? "" : newValue);

        history.put("userId", userId);
        history.put("userEmail", userEmail);

        history.put("timestamp", System.currentTimeMillis());

        // Lấy fullName từ bảng users
        if (!userId.isEmpty()) {

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        String fullName = snapshot.child("fullName")
                                .getValue(String.class);

                        if (fullName == null || fullName.isEmpty()) {
                            fullName = userEmail;
                        }

                        history.put("userName", fullName);

                        FirebaseDatabase.getInstance()
                                .getReference("history")
                                .child(historyId)
                                .setValue(history);
                    })
                    .addOnFailureListener(e -> {

                        history.put("userName", userEmail);

                        FirebaseDatabase.getInstance()
                                .getReference("history")
                                .child(historyId)
                                .setValue(history);
                    });

        } else {

            history.put("userName", "Không xác định");

            FirebaseDatabase.getInstance()
                    .getReference("history")
                    .child(historyId)
                    .setValue(history);
        }
    }
    public static void logLogin(String method) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        log(
                "LOGIN",
                "",
                "",
                user.getUid(),
                user.getDisplayName() == null
                        ? user.getEmail()
                        : user.getDisplayName(),
                "Đăng nhập",
                "",
                method
        );
    }
    public static void logSignUp(String method) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        log(
                "SIGN_UP",
                "",
                "",
                user.getUid(),
                user.getDisplayName() == null
                        ? user.getEmail()
                        : user.getDisplayName(),
                "",
                "",
                method
        );
    }
}