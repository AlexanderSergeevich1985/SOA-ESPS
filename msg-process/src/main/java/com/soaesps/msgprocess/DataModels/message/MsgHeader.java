/**The MIT License (MIT)
Copyright (c) 2018 by AleksanderSergeevich
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.soaesps.msgprocess.DataModels.message;

import java.io.Serializable;
import java.util.UUID;

public class MsgHeader implements Serializable {
    private String messageId;
    private String messageType;
    private String timestamp;
    private long bodySize;
    private String deviceUID;

    public MsgHeader() {}
    public MsgHeader(String messageId, String messageType, String timestamp, Long bodySize) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.bodySize = bodySize;
    }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getMessageId() { return this.messageId; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getMessageType() { return this.messageType; }
    public void setTimeStamp(String timestamp) { this.timestamp = timestamp; }
    public String getTimeStamp() { return this.timestamp; }
    public void setBodySize(long bodySize) { this.bodySize = bodySize; }
    public long getBodySize() { return this.bodySize; }
    public void setDeviceUID(String deviceUID) {
        if(!deviceUID.isEmpty())
            this.deviceUID = deviceUID;
        else
            this.deviceUID = UUID.randomUUID().toString();
    }
    public String getDeviceUID() {
        return this.deviceUID;
    }
    @Override
    public String toString() {
        return "Message header [id=" + messageId + ", type=" + messageType + ", timestamp=" + timestamp +
                ", body size=" + bodySize + ", device UUID" + deviceUID +"]";
    }
}