package com.sparta.camp.java.FinalProject.domain.user.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserCreateRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserDeleteRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserPasswordChangeRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserResponse;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<UserResponse> getUserById(@PathVariable Long userId){
    return ApiResponse.success(userService.getUserById(userId));
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
    return ApiResponse.success(userService.createUser(userCreateRequest));
  }

  @PutMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
    userService.updateUser(userId, userUpdateRequest);
    return ApiResponse.success();
  }

  @PutMapping("/{userId}/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> changePassword(@PathVariable Long userId, @Valid @RequestBody UserPasswordChangeRequest userPasswordChangeRequest) {
    userService.updatePassword(userId, userPasswordChangeRequest);
    return ApiResponse.success();
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> deleteUser(@PathVariable Long userId, @Valid @RequestBody UserDeleteRequest userDeleteRequest) {
    userService.deleteUser(userId, userDeleteRequest);
    return ApiResponse.success();
  }

}
