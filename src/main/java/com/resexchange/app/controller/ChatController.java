package com.resexchange.app.controller;

import com.resexchange.app.model.Chat;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.Message;
import com.resexchange.app.model.User;
import com.resexchange.app.repositories.ChatRepository;
import com.resexchange.app.repositories.ListingRepository;
import com.resexchange.app.repositories.MessageRepository;
import com.resexchange.app.repositories.UserRepository;
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

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private MessageRepository messageRepository;


    @GetMapping("/{listingId}/{creatorId}/{initiatorId}")
    public String openChat(@PathVariable Long listingId,
                           @PathVariable Long creatorId,
                           @PathVariable Long initiatorId,
                           Principal principal,
                           Model model) {

        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

        if (!loggedInUser.getId().equals(initiatorId) && !loggedInUser.getId().equals(creatorId)) {
            throw new IllegalArgumentException("Unauthorized access to chat");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found"));

        Chat chat = chatRepository.findByListingAndCreatorAndInitiator(listing, creator, initiator)
                .orElseGet(() -> {
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

        return "chat/chat";
    }




    @GetMapping("/active")
    public String getActiveChats(Principal principal, Model model) {
        User loggedInUser = userRepository.findByMail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));

        List<Chat> activeChats = chatRepository.findByCreatorOrInitiator(loggedInUser, loggedInUser);

        model.addAttribute("activeChats", activeChats);
        model.addAttribute("loggedInUser", loggedInUser);

        return "chat/active-chats";
    }
}


