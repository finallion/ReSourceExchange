package com.resexchange.app.controller;

import com.resexchange.app.model.ChatMessage;
import com.resexchange.app.repositories.ChatRepository;
import com.resexchange.app.repositories.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    @Autowired
    public MessageController(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public ChatMessage sendMessage(@DestinationVariable Long chatId, @Payload ChatMessage message) {
        LOGGER.info("Received message for chatId: {}", chatId);

        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> {
                    LOGGER.error("Chat with ID: {} not found", chatId);
                    return new IllegalArgumentException("Chat not found");
                });

        message.setChat(chat);
        message = messageRepository.save(message);

        LOGGER.info("Message saved successfully for chatId: {}", chatId);
        return message;
    }
}
