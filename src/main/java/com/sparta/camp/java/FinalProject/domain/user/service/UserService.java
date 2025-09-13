package com.sparta.camp.java.FinalProject.domain.user.service;

import com.sparta.camp.java.FinalProject.common.enums.Role;
import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserCreateRequest;
import com.sparta.camp.java.FinalProject.domain.user.dto.UserResponse;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import com.sparta.camp.java.FinalProject.domain.user.mapper.UserMapper;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Transactional
  public UserResponse createUser(UserCreateRequest userCreateRequest) {

    if (userRepository.findByEmail(userCreateRequest.getEmail()).isPresent()) {
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

}
