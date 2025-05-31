package com.finance.authentication_service.service;

import com.finance.authentication_service.dto.ApiResponse;
import com.finance.authentication_service.dto.LoginDto;
import com.finance.authentication_service.dto.RegisterDto;
import com.finance.authentication_service.entity.UserInfo;
import com.finance.authentication_service.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, MessageSource messageSource, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public ResponseEntity<ApiResponse<RegisterDto>> registerUser(RegisterDto registerDto){
        String hashedPassword = passwordEncoder.encode(registerDto.getPassword());
        registerDto.setPassword(hashedPassword);
        UserInfo userInfo = modelMapper.map(registerDto, UserInfo.class);
        userRepository.save(userInfo);
        ApiResponse<RegisterDto> apiResponse = new ApiResponse<>(
                HttpStatus.OK,
                messageSource.getMessage("user.register.success",null, Locale.ENGLISH),
                null
        );
        return ResponseEntity.ok(apiResponse);
    }

    public ResponseEntity<ApiResponse<Object>> loginUser(LoginDto loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(),loginDto.getPassword())
        );
        Optional<UserInfo> userOptional = userRepository.findByEmail(loginDto.getEmail());
        UserInfo user = userOptional.get();
        String jwtToken = jwtService.generateToken(user.getEmail(), String.valueOf(user.getRole()));
        ApiResponse<Object> apiResponse = new ApiResponse<>(
                HttpStatus.OK,
                messageSource.getMessage("token.creation.success",null, Locale.getDefault()),
                jwtToken
        );
        return ResponseEntity.ok(apiResponse);
    }

    public ResponseEntity<ApiResponse<?>> getUserDetails(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        ApiResponse<?> response;
        if(userId==null){
            Page<UserInfo> userInfos = userRepository.findAll(pageable);
            response = new ApiResponse<>(
                    HttpStatus.OK,
                    messageSource.getMessage("users.retrieved.success",null, Locale.getDefault()),
                    userInfos
            );
        }
        else{
            Optional<UserInfo> user = userRepository.findById(userId);
            response = new ApiResponse<>(
                    HttpStatus.OK,
                    messageSource.getMessage("users.retrieved.success",null, Locale.getDefault()),
                    user
            );
        }
        return ResponseEntity.ok(response);
    }
}
