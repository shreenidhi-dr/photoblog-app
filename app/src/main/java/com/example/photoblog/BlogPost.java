package com.example.photoblog;


import java.util.Date;

public class BlogPost extends BlogPostId {
    public  String user_id, image_url, desc, thumb;
    public Date timestamp;


    public  BlogPost(){

    }

    public BlogPost(String user_id, String image_url, String desc, String thumb, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.thumb = thumb;
        this.timestamp = timestamp;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BlogPost{" +
                "user_id='" + user_id + '\'' +
                ", image_url='" + image_url + '\'' +
                ", desc='" + desc + '\'' +
                ", thumb='" + thumb + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
