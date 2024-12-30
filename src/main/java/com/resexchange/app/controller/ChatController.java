package com.resexchange.app.controller;

import com.resexchange.app.model.Chat;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Message;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.ChatRepository;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MessageRepository;
import com.resexchange.app.repositories.UserRepository;
import com.resexchange.app.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationService notificationService;


    @GetMapping("/{listingId}/{creatorId}/{initiatorId}")
    public String openChat(@PathVariable Long listingId,
                           @PathVariable Long creatorId,
                           @PathVariable Long initiatorId,
                           Principal principal,
                           Model model) {

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> {
                    LOGGER.error("Logged-in user not found for email: {}", principal.getName());
                    return new IllegalArgumentException("Logged-in user not found");
                });

        if (!loggedInUser.getId().equals(initiatorId) && !loggedInUser.getId().equals(creatorId)) {
            LOGGER.warn("Unauthorized access attempt to chat. User ID: {}, Listing ID: {}", loggedInUser.getId(), listingId);
            throw new IllegalArgumentException("Unauthorized access to chat");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> {
                    LOGGER.error("Listing not found with ID: {}", listingId);
                    return new IllegalArgumentException("Listing not found");
                });

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> {
                    LOGGER.error("Creator not found with ID: {}", creatorId);
                    return new IllegalArgumentException("Creator not found");
                });

        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> {
                    LOGGER.error("Initiator not found with ID: {}", initiatorId);
                    return new IllegalArgumentException("Initiator not found");
                });

        Chat chat = chatRepository.findByListingAndCreatorAndInitiator(listing, creator, initiator)
                .orElseGet(() -> {
                    LOGGER.info("Creating new chat for Listing ID: {}, Creator ID: {}, Initiator ID: {}", listingId, creatorId, initiatorId);
                    Chat newChat = new Chat();
                    newChat.setListing(listing);
                    newChat.setCreator(creator);
                    newChat.setInitiator(initiator);
                    return chatRepository.save(newChat);
                });

        List<Message> messages = messageRepository.findByChatIdOrderByIdAsc(chat.getId());

        model.addAttribute("chatId", chat.getId());
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("messages", messages);

        if(creatorId!=loggedInUser.getId()) {
        String notificationMessage = "Neue Nachricht von " + loggedInUser.getName();
        notificationService.createNotification(creator, notificationMessage, "chat/"+listing.getId()+"/"+creatorId+"/"+initiatorId);
        }

        return "chat/chat";
    }

    @GetMapping("/active")
    public String getActiveChats(Principal principal, Model model) {
        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> {
                    LOGGER.error("Logged-in user not found for email: {}", principal.getName());
                    return new IllegalArgumentException("Logged-in user not found");
                });

        LOGGER.info("Fetching active chats for User ID: {}", loggedInUser.getId());

        List<Chat> activeChats = chatRepository.findByCreatorOrInitiator(loggedInUser, loggedInUser);

        if (activeChats.isEmpty()) {
            LOGGER.info("No active chats found for User ID: {}", loggedInUser.getId());
        } else {
            LOGGER.info("Found {} active chats for User ID: {}", activeChats.size(), loggedInUser.getId());
        }

        model.addAttribute("activeChats", activeChats);
        model.addAttribute("loggedInUser", loggedInUser);

        return "chat/active-chats";
    }

}


