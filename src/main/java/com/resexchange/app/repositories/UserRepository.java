package com.resexchange.app.repositories;

import com.resexchange.app.model.Company;
import com.resexchange.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByMail(String email);
}
