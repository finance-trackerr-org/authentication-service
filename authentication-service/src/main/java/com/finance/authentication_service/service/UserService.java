package com.finance.authentication_service.service;

import com.finance.authentication_service.dto.ApiResponse;
import com.finance.authentication_service.dto.LoginDto;
import com.finance.authentication_service.dto.RegisterDto;
import com.finance.authentication_service.entity.UserInfo;
import com.finance.authentication_service.exception.BadRequestException;
import com.finance.authentication_service.exception.ResourceNotFoundException;
import com.finance.authentication_service.exception.UnauthorizedAccessException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
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
        try {
            String hashedPassword = passwordEncoder.encode(registerDto.getPassword());
            registerDto.setPassword(hashedPassword);
            UserInfo userInfo = modelMapper.map(registerDto, UserInfo.class);
            Optional<UserInfo> userDetails = userRepository.findByEmail(registerDto.getEmail());
            if(userDetails.isPresent()) {
                System.out.println("HIiiiiiiiii");
                throw new BadRequestException(messageSource.getMessage("user.already.exists", null, Locale.ENGLISH));
            }
            userRepository.save(userInfo);
            ApiResponse<RegisterDto> apiResponse = new ApiResponse<>(
                    HttpStatus.OK,
                    messageSource.getMessage("user.register.success", null, Locale.ENGLISH),
                    null
            );
            return ResponseEntity.ok(apiResponse);
        } catch(BadRequestException ex) {
            throw ex;
        } catch (Exception ex){
            throw new RuntimeException(
                    messageSource.getMessage("record.saving.error", null, Locale.ENGLISH)
            );
        }
    }

    public ResponseEntity<ApiResponse<Object>> loginUser(LoginDto loginDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedAccessException(
                    messageSource.getMessage("user.login.failed", null, Locale.ENGLISH)
            );
        }
        UserInfo user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UnauthorizedAccessException(
                        messageSource.getMessage("user.login.failed", null, Locale.ENGLISH)
                ));
        String jwtToken = jwtService.generateToken(user.getEmail(),user.getId(), String.valueOf(user.getRole()));
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
            Optional<UserInfo> user = Optional.ofNullable(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(
                    messageSource.getMessage("user.found.fail", null, Locale.getDefault())
            )));
            response = new ApiResponse<>(
                    HttpStatus.OK,
                    messageSource.getMessage("users.retrieved.success",null, Locale.getDefault()),
                    user
            );
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse<Object>> getUserDetailsByUserName(String userName) {
        UserInfo userInfo = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageSource.getMessage("user.found.fail",null, Locale.getDefault())
                ));
        ApiResponse<Object> response = new ApiResponse<>(
                HttpStatus.OK,
                messageSource.getMessage("users.retrieved.success",null, Locale.getDefault()),
                userInfo
        );
        return ResponseEntity.ok(response);
    }
}
