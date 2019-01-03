package com.soaesps.msgprocess.controller;

import com.soaesps.msgprocess.DataModels.message.MsgIOTDevice;
import com.soaesps.msgprocess.DataModels.message.MsgResult;
import com.soaesps.msgprocess.service.MsgProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class MsgProcessController {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(MsgProcessController.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private MsgProcessService mps;

    @MessageMapping("/apps/shared/{outputId}/process/{processorId}")
    @SendTo("/topic/apps/shared/{outputId}")
    public MsgResult processMsg(@DestinationVariable("outputId") String outputId, @DestinationVariable("processorId") String processorId, MsgIOTDevice msg) {
        MsgResult result = mps.process(processorId, msg);
        return result;
    }

    @MessageMapping("/apps/p2p/{recipientId}/process/{processorId}")
    public void processMsgShare(@DestinationVariable("recipientId") String recipientId, @DestinationVariable("processorId") String processorId, MsgIOTDevice msg) {
        MsgResult result = mps.process(processorId, msg);
        mps.send(recipientId, result);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
}