package com.resexchange.app.repositories;

import com.resexchange.app.model.Chat;
import com.resexchange.app.model.Listing;
import com.resexchange.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByListingAndCreatorAndInitiator(Listing listing, User creator, User initiator);

    List<Chat> findByCreatorOrInitiator(User creator, User initiator);
}
