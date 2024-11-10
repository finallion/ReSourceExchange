package com.resexchange.app.controller;

import com.resexchange.app.model.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat/{chatId}")  // receives messages from /app/chat/{chatId}
    @SendTo("/topic/chat/{chatId}") // broadcasts to /topic/chat/{chatId}
    public Message sendMessage(@DestinationVariable Long chatId, Message message) {
        return message;  // this message will be sent to all subscribers of /topic/chat/{chatId}
    }
}

