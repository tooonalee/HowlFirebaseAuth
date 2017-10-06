package com.test.HowlFirebaseAuth.ValueObject;

/**
 * Created by admin on 2017/10/05.
 */

public class Member {
    private String memberEmail;
    private String name;
    private String position;
    private boolean onWorkNotificationFlag;

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isOnWorkNotificationFlag() {
        return onWorkNotificationFlag;
    }

    public void setOnWorkNotificationFlag(boolean onWorkNotificationFlag) {
        this.onWorkNotificationFlag = onWorkNotificationFlag;
    }
}
