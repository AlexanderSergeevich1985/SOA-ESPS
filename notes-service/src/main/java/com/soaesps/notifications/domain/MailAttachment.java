package com.soaesps.notifications.domain;

import com.google.common.net.MediaType;
import org.springframework.core.io.InputStreamSource;

public class MailAttachment {
    private String name;

    private MediaType type;

    private InputStreamSource source;

    public MailAttachment() {}

    public String getName() {
        return name;
    }

    public MediaType getType() {
        return type;
    }

    public InputStreamSource getSource() {
        return source;
    }
}