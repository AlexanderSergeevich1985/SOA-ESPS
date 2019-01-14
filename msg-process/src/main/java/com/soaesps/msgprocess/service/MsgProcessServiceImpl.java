package com.soaesps.msgprocess.service;

import com.google.gson.Gson;
import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import com.soaesps.msgprocess.DataModels.message.MsgResult;
import com.soaesps.msgprocess.Utils.BaseQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MsgProcessServiceImpl implements MsgProcessService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(MsgProcessServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private SimpMessagingTemplate smsgt;

    BaseQueue<MsgResult> msgQueue = new BaseQueue<>();

    public MsgResult process(final String topicName, final MsgIOTDevice msg) {
        ListenableFuture<SendResult> future = kafkaTemplate.send(topicName, msg);
        return msgQueue.pull();
    }

    public void send(final String topicName, final MsgResult msg) {
        smsgt.convertAndSend(topicName, msg);
    }

    @KafkaListener(topics = "${spring.kafka.topic.result}", groupId = "${spring.kafka.group-id}")
    public void consume(@Payload String message) {
        Gson gsonObj = new Gson();
        MsgResult result = gsonObj.fromJson(message, MsgResult.class);
        msgQueue.push(result);
    }
}