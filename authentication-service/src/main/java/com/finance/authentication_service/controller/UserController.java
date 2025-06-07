package com.finance.authentication_service.controller;

import com.finance.authentication_service.dto.ApiResponse;
import com.finance.authentication_service.dto.LoginDto;
import com.finance.authentication_service.dto.RegisterDto;
import com.finance.authentication_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<RegisterDto>> register(@Valid @RequestBody RegisterDto registerDto){
        return userService.registerUser(registerDto);
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginDto loginDto){
        return userService.loginUser(loginDto);
    }

    @GetMapping("/admin/users")
    ResponseEntity<ApiResponse<?>> userDetails(@RequestParam(value="userId",required = false,defaultValue = "") UUID userId,
                                                            @RequestParam(value="page",required = false,defaultValue = "0") int page,
                                                            @RequestParam(value = "size",required = false,defaultValue = "2") int size){
        return userService.getUserDetails(userId,page,size);
    }
}
