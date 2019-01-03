package com.soaesps.msgprocess.service;

import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import com.soaesps.msgprocess.DataModels.message.MsgResult;

public interface MsgProcessService {
    MsgResult process(String topicName, MsgIOTDevice msg);
    void send(String destination, MsgResult result);
}