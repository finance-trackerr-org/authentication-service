package com.finance.authentication_service.service;

import com.finance.authentication_service.entity.UserInfo;
import com.finance.authentication_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UserInfoDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    private final MessageSource messageSource;

    public UserInfoDetailService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserInfo userInfo = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage("user.found.fail",null, Locale.getDefault())
                ));
        return new UserInfoDetails(userInfo);
    }
}
