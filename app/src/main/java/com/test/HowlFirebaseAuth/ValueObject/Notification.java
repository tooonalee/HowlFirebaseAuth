package com.test.HowlFirebaseAuth.ValueObject;

import java.util.Date;

/**
 * Created by admin on 2017/10/05.
 */

public class Notification {
    // notificationID  = memberEmail + createOnWorkDate
    private String notificationId;
    // onWork Value Data
    private String onWorkImageUrl;
    private String onWorkImageName;
    private String onWorkTitle;
    private String onWorkDescription;
    private Date createOnWorkDate;
    // OffWork Value Data
    private String offWorkImageUrl;
    private String offWorkImageName;
    private String offWorkTitle;
    private String offWorkDescription;
    private Date createOffWorkDate;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getOnWorkImageUrl() {
        return onWorkImageUrl;
    }

    public void setOnWorkImageUrl(String onWorkImageUrl) {
        this.onWorkImageUrl = onWorkImageUrl;
    }

    public String getOnWorkImageName() {
        return onWorkImageName;
    }

    public void setOnWorkImageName(String onWorkImageName) {
        this.onWorkImageName = onWorkImageName;
    }

    public String getOnWorkTitle() {
        return onWorkTitle;
    }

    public void setOnWorkTitle(String onWorkTitle) {
        this.onWorkTitle = onWorkTitle;
    }

    public String getOnWorkDescription() {
        return onWorkDescription;
    }

    public void setOnWorkDescription(String onWorkDescription) {
        this.onWorkDescription = onWorkDescription;
    }

    public Date getCreateOnWorkDate() {
        return createOnWorkDate;
    }

    public void setCreateOnWorkDate(Date createOnWorkDate) {
        this.createOnWorkDate = createOnWorkDate;
    }

    public String getOffWorkImageUrl() {
        return offWorkImageUrl;
    }

    public void setOffWorkImageUrl(String offWorkImageUrl) {
        this.offWorkImageUrl = offWorkImageUrl;
    }

    public String getOffWorkImageName() {
        return offWorkImageName;
    }

    public void setOffWorkImageName(String offWorkImageName) {
        this.offWorkImageName = offWorkImageName;
    }

    public String getOffWorkTitle() {
        return offWorkTitle;
    }

    public void setOffWorkTitle(String offWorkTitle) {
        this.offWorkTitle = offWorkTitle;
    }

    public String getOffWorkDescription() {
        return offWorkDescription;
    }

    public void setOffWorkDescription(String offWorkDescription) {
        this.offWorkDescription = offWorkDescription;
    }

    public Date getCreateOffWorkDate() {
        return createOffWorkDate;
    }

    public void setCreateOffWorkDate(Date createOffWorkDate) {
        this.createOffWorkDate = createOffWorkDate;
    }
}
