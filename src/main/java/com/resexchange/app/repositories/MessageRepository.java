package com.resexchange.app.repositories;

import com.resexchange.app.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatIdOrderByIdAsc(Long chatId);
}
