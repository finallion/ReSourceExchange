package com.resexchange.app.repositories;

import com.resexchange.app.model.Company;
import com.resexchange.app.model.PrivateUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}
