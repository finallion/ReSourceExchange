package com.resexchange.app.security;

import com.resexchange.app.model.User;
import com.resexchange.app.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class REUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public REUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository
                .findUserByMail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new REUserDetails(user);
    }
}