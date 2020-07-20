package com.soaesps.core.DataModels.message;

import com.soaesps.core.DataModels.BaseEntity;
import com.soaesps.core.Utils.convertor.json.LogMsgConvertor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "soa_esps.log_msgs")
public class BaseLogMsg<T extends Serializable> extends BaseEntity {
    @Column(name = "msg_hash", nullable = false)
    private String msgHash;

    @Column(name = "event_desc")
    @Convert(converter = LogMsgConvertor.class)
    private T message;

    public BaseLogMsg() {}

    public String getMsgHash() {
        return msgHash;
    }

    public void setMsgHash(final String msgHash) {
        this.msgHash = msgHash;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(final T message) {
        this.message = message;
    }
}