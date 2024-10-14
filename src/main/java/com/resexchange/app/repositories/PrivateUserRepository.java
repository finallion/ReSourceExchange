package com.resexchange.app.repositories;

import com.resexchange.app.model.PrivateUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrivateUserRepository extends JpaRepository<PrivateUser, Long> {

    List<PrivateUser> findByName(String name);
}
