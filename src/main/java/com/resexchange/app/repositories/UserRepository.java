package com.resexchange.app.repositories;

import com.resexchange.app.model.Permission;
import com.resexchange.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByMail(String email);
    Optional<User> findByMail(String mail);
    boolean existsByMail(String mail);

    @Modifying
    @Query("UPDATE User u SET u.permissions = :permissions WHERE u.id = :id")
    void updatePermissions(@Param("id") Long id, @Param("permissions") Set<Permission> permissions);
}
