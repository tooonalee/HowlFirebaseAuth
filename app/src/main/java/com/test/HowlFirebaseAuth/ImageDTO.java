package com.test.HowlFirebaseAuth;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/09/29.
 */

class ImageDTO {
    private String imageUrl;
    private String imageName;
    private String title;
    private String description;
    private String uid;
    private String userId;
    private int startCount = 0;
    // FIXME: これは何を管理するHashMapですか？名前をわかりやすくした方が良いかと思います
    private Map<String, Boolean> stars = new HashMap<>();

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStartCount() {
        return startCount;
    }

    public void setStartCount(int startCount) {
        this.startCount = startCount;
    }

    public Map<String, Boolean> getStars() {
        return stars;
    }

    public void setStars(Map<String, Boolean> stars) {
        this.stars = stars;
    }
}
