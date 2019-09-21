package com.soaesps.core.DataModels.executor;

import java.io.Serializable;

public interface Payload extends Serializable {
    String getJobKey();
}