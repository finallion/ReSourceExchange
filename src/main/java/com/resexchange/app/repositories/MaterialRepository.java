package com.resexchange.app.repositories;

import com.resexchange.app.model.Material;
import com.resexchange.app.model.PrivateUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

}
