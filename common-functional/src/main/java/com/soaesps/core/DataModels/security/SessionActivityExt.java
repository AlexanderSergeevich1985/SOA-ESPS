package com.soaesps.core.DataModels.security;

import com.soaesps.core.DataModels.BaseEntity;

public class SessionActivityExt extends BaseEntity {
    private String activityName;

    private String activityDesc;

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDesc() {
        return activityDesc;
    }

    public void setActivityDesc(String activityDesc) {
        this.activityDesc = activityDesc;
    }
}