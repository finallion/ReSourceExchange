package com.resexchange.app.repositories;

import com.resexchange.app.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByIdAsc(Long chatId);
}
