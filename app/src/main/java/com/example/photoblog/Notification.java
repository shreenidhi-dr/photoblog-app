package com.example.photoblog;



import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;



import java.util.Date;

public class Notification {

    public String blog_post_id, message,user_id;
    public Date timestamp;
    public FloatingActionButton addPostBtn;

    public Notification() {

    }

    public Notification(String blog_post_id, String message, String user_id, Date timestamp) {
        this.blog_post_id = blog_post_id;
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getBlog_post_id() {
        return blog_post_id;
    }

    public void setBlog_post_id(String blog_post_id) {
        this.blog_post_id = blog_post_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
