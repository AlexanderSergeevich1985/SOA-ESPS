package com.soaesps.core.component.storage;

import com.soaesps.core.DataModels.BaseOnlyIdEntity;

/**
 * Created by sniper on 19.11.22.
 */
public class Attribute extends BaseOnlyIdEntity {
    private Integer type;

    protected String value;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Number getNumericValue() {
        return this.value.length();
    }
}