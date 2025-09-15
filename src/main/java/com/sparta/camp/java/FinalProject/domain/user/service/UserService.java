package com.sparta.camp.java.FinalProject.domain.user.service;

import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserCreateRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserDeleteRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserPasswordChangeRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserResponse;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.mapper.UserMapper;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  public UserResponse getUserById (Long id) {
    User user = this.getUser(id);
    return userMapper.toResponse(user);
  }

  public UserResponse createUser(UserCreateRequest userCreateRequest) {
    if (userRepository.findByEmailAndDeletedAtIsNull(userCreateRequest.getEmail()).isPresent()) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_USER_EMAIL);
    }

    String encodedPassword = passwordEncoder.encode(userCreateRequest.getPassword());

    User user = User.builder()
        .email(userCreateRequest.getEmail())
        .name(userCreateRequest.getName())
        .role(Role.USER)
        .password(encodedPassword)
        .phoneNumber(userCreateRequest.getPhoneNumber())
        .zipCode(userCreateRequest.getZipCode())
        .address(userCreateRequest.getAddress())
        .detailAddress(userCreateRequest.getDetailAddress())
        .birthDate(userCreateRequest.getBirthDate())
        .build();

    User savedUser = userRepository.save(user);
    return userMapper.toResponse(savedUser);
  }

  public void updateUser(Long id, UserUpdateRequest userUpdateRequest) {
    User user = this.getUser(id);

    user.setName(userUpdateRequest.getName());
    user.setPhoneNumber(userUpdateRequest.getPhoneNumber());
    user.setZipCode(userUpdateRequest.getZipCode());
    user.setAddress(userUpdateRequest.getAddress());
    user.setDetailAddress(userUpdateRequest.getDetailAddress());
    user.setBirthDate(userUpdateRequest.getBirthDate());
  }

  public void updatePassword (Long id, UserPasswordChangeRequest userPasswordChangeRequest) {
    User user = this.getUser(id);

    if (!passwordEncoder.matches(user.getPassword(), userPasswordChangeRequest.getCurrentPassword())
      || !userPasswordChangeRequest.getNewPassword().equals(userPasswordChangeRequest.getConfirmPassword())) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PASSWORD);
    }

    if (passwordEncoder.matches(user.getPassword(), userPasswordChangeRequest.getNewPassword())) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_CURRENT_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(userPasswordChangeRequest.getNewPassword()));
  }

  public void deleteUser(Long id, UserDeleteRequest userDeleteRequest) {
    User user = this.getUser(id);

    if (!passwordEncoder.matches(user.getPassword(), userDeleteRequest.getCurrentPassword())) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PASSWORD);
    }

    user.setDeletedAt(LocalDateTime.now());
  }

  private User getUser (Long id) {
    return userRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_USER));
  }

}
