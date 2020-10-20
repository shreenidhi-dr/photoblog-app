package com.example.photoblog;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class CommentId {
    @Exclude
    public String CommentId;

    public <T extends CommentId> T withId(@NonNull final String id) {
        this.CommentId = id;
        return (T) this;
    }
}
