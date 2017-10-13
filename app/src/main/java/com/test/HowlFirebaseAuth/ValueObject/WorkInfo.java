package com.test.HowlFirebaseAuth.ValueObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2017/10/05.
 */

public class WorkInfo {
    // notificationID  = memberEmail + createOnWorkDate
    private String key;
    private String name;
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
    // walkTime
    private int workingTime;
    private String memberEmail;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(int workingTime) {
        this.workingTime = workingTime;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("name", name);
        result.put("onWorkImageUrl", onWorkImageUrl);
        result.put("onWorkImageName", onWorkImageName);
        result.put("onWorkTitle", onWorkTitle);
        result.put("onWorkDescription", onWorkDescription);
        result.put("createOnWorkDate", createOnWorkDate);
        result.put("offWorkImageUrl", offWorkImageUrl);
        result.put("offWorkImageName", offWorkImageName);
        result.put("offWorkTitle", offWorkTitle);
        result.put("offWorkDescription", offWorkDescription);
        result.put("createOffWorkDate", createOffWorkDate);
        result.put("workingTime", workingTime);
        result.put("memberEmail", memberEmail);

        return result;
    }

    @Override
    public String toString() {
        return "WorkInfo{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", onWorkImageUrl='" + onWorkImageUrl + '\'' +
                ", onWorkImageName='" + onWorkImageName + '\'' +
                ", onWorkTitle='" + onWorkTitle + '\'' +
                ", onWorkDescription='" + onWorkDescription + '\'' +
                ", createOnWorkDate=" + createOnWorkDate +
                ", offWorkImageUrl='" + offWorkImageUrl + '\'' +
                ", offWorkImageName='" + offWorkImageName + '\'' +
                ", offWorkTitle='" + offWorkTitle + '\'' +
                ", offWorkDescription='" + offWorkDescription + '\'' +
                ", createOffWorkDate=" + createOffWorkDate +
                ", workingTime=" + workingTime +
                ", memberEmail='" + memberEmail + '\'' +
                '}';
    }
}
