package com.soaesps.core.DataModels.security;

import com.soaesps.core.Utils.DataStructure.BaseDynEnumI;

import java.util.Set;
import java.util.HashSet;

public enum SessionActivity implements BaseDynEnumI {
    RED,
    YELLOW,
    GREEN;

    @Override
    public Set<BaseDynEnumI> getDynItems() {
        return new HashSet<>();
    }

    @Override
    public int getItemId() {
        return 1;
    }

    @Override
    public String getItemName() {
        return "";
    }
}