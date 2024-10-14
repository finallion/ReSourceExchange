package com.resexchange.app.services;

import com.resexchange.app.repositories.PrivateUserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class PrivateUserService {
    private final Logger LOGGER = LogManager.getLogger();

    private final PrivateUserRepository privateUserRepository;

    public PrivateUserService(PrivateUserRepository privateUserRepository) {
        super();
        this.privateUserRepository = privateUserRepository;
    }

}
