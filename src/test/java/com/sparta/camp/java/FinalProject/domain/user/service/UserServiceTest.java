package com.sparta.camp.java.FinalProject.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserMapper userMapper;

  private UserCreateRequest userCreateRequest;
  private UserUpdateRequest userUpdateRequest;
  private UserPasswordChangeRequest userPasswordChangeRequest;
  private UserPasswordChangeRequest userPasswordChangeRequest2;
  private UserPasswordChangeRequest userPasswordChangeRequest3;
  private UserDeleteRequest userDeleteRequest;

  private User testUser;
  private UserResponse testResponse;

  @BeforeEach
  void setUp() {
    userCreateRequest = new UserCreateRequest();
    ReflectionTestUtils.setField(userCreateRequest, "email", "somsom@test.com");
    ReflectionTestUtils.setField(userCreateRequest, "name", "test1");
    ReflectionTestUtils.setField(userCreateRequest, "password", "Test1234*");
    ReflectionTestUtils.setField(userCreateRequest, "phoneNumber","010-1234-5678");

    userUpdateRequest = new UserUpdateRequest();
    ReflectionTestUtils.setField(userUpdateRequest, "name", "test2");
    ReflectionTestUtils.setField(userUpdateRequest, "phoneNumber", "010-1004-1004");

    userPasswordChangeRequest = new UserPasswordChangeRequest();
    ReflectionTestUtils.setField(userPasswordChangeRequest, "currentPassword", "Test1234*");
    ReflectionTestUtils.setField(userPasswordChangeRequest, "newPassword", "Test5678*");
    ReflectionTestUtils.setField(userPasswordChangeRequest, "confirmPassword", "Test5678*");

    userPasswordChangeRequest2 = new UserPasswordChangeRequest();
    ReflectionTestUtils.setField(userPasswordChangeRequest2, "currentPassword", "Test1235*");
    ReflectionTestUtils.setField(userPasswordChangeRequest2, "newPassword", "Test5678*");
    ReflectionTestUtils.setField(userPasswordChangeRequest2, "confirmPassword", "Test5678*");

    userPasswordChangeRequest3 = new UserPasswordChangeRequest();
    ReflectionTestUtils.setField(userPasswordChangeRequest3, "currentPassword", "Test1234*");
    ReflectionTestUtils.setField(userPasswordChangeRequest3, "newPassword", "Test1234*");
    ReflectionTestUtils.setField(userPasswordChangeRequest3, "confirmPassword", "Test1234*");

    userDeleteRequest = new UserDeleteRequest();
    ReflectionTestUtils.setField(userDeleteRequest, "currentPassword", "Test1234*");

    testUser = User.builder()
        .email(userCreateRequest.getEmail())
        .name(userCreateRequest.getName())
        .role(Role.ROLE_USER)
        .password("ENCODED_PASSWORD")
        .phoneNumber(userCreateRequest.getPhoneNumber())
        .build();

    testResponse =  UserResponse.builder()
        .id(testUser.getId())
        .email(testUser.getEmail())
        .name(testUser.getName())
        .role(testUser.getRole())
        .build();
  }

  @Test
  @DisplayName("정상적으로 회원 등록이 된다.")
  void createUser_should_return_true() {
    when(userRepository.findByEmailAndDeletedAtIsNull(userCreateRequest.getEmail()))
        .thenReturn(Optional.empty());
    when(passwordEncoder.encode(userCreateRequest.getPassword()))
        .thenReturn("ENCODED_PASSWORD");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userMapper.toResponse(testUser)).thenReturn(testResponse);

    UserResponse result = userService.createUser(userCreateRequest);

    assertThat(result).isNotNull();

    verify(userRepository).findByEmailAndDeletedAtIsNull(userCreateRequest.getEmail());
    verify(passwordEncoder).encode(userCreateRequest.getPassword());
    verify(userRepository).save(any(User.class));
    verify(userMapper).toResponse(testUser);
  }

  @Test
  @DisplayName("중복된 메일인 경우 오류가 발생한다.")
  void createUser_shouldThrowException_when_email_is_existed() {
    when(userRepository.findByEmailAndDeletedAtIsNull(userCreateRequest.getEmail()))
        .thenReturn(Optional.of(new User()));

    assertThatThrownBy(() -> userService.createUser(userCreateRequest))
      .isInstanceOf(ServiceException.class)
      .hasMessageContaining(ServiceExceptionCode.DUPLICATE_EMAIL.getMessage());

    verify(userRepository).findByEmailAndDeletedAtIsNull(anyString());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  @DisplayName("정상적으로 회원 조회가 된다.")
  void getUserById_should_return_true() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
      .thenReturn(Optional.of(testUser));
    when(userMapper.toResponse(testUser)).thenReturn(testResponse);

    UserResponse result = userService.getUserById(testUser.getId());

    assertThat(result).isNotNull();
    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
    verify(userMapper).toResponse(testUser);
  }

  @Test
  @DisplayName("존재하지 않는 사용자로 오류가 발생한다.")
  void getUserById_shouldThrowException_when_user_is_not_existed() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(testUser.getId()))
      .isInstanceOf(ServiceException.class)
      .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_USER.getMessage());

    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
  }

  @Test
  @DisplayName("정상적으로 회원 수정이 된다.")
  void updateUser_should_return_true() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
        .thenReturn(Optional.of(testUser));

    userService.updateUser(testUser.getId(), userUpdateRequest);

    assertThat(testUser.getName()).isEqualTo(userUpdateRequest.getName());
    assertThat(testUser.getPhoneNumber()).isEqualTo(userUpdateRequest.getPhoneNumber());

    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
  }

  @Test
  @DisplayName("비밀번호가 정상적으로 수정 된다.")
  void updatePassword_should_return_true() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
      .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(userPasswordChangeRequest.getCurrentPassword(), testUser.getPassword()))
      .thenReturn(true);
    when(passwordEncoder.matches(userPasswordChangeRequest.getNewPassword(), testUser.getPassword()))
      .thenReturn(false);
    when(passwordEncoder.encode(userPasswordChangeRequest.getNewPassword()))
      .thenReturn("ENCODED_PASSWORD");

    userService.updatePassword(testUser.getId(), userPasswordChangeRequest);

    assertThat(testUser.getPassword()).isEqualTo("ENCODED_PASSWORD");
    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
  }

  @Test
  @DisplayName("기존 비밀번호와 일치하지 않는 경우 오류가 발생한다.")
  void updatePassword_shouldThrowException_when_currentPassword_is_wrong() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
        .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(userPasswordChangeRequest2.getCurrentPassword(), testUser.getPassword()))
        .thenReturn(false);

    assertThatThrownBy(() -> userService.updatePassword(testUser.getId(), userPasswordChangeRequest2))
      .isInstanceOf(ServiceException.class)
      .hasMessageContaining(ServiceExceptionCode.NOT_MATCH_PASSWORD.getMessage());

    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  @DisplayName("새 비밀번호가 확인 비밀번호와 일치하지 않는 경우 오류가 발생한다.")
  void updatePassword_shouldThrowException_when_newPassword_is_not_match() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
        .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(userPasswordChangeRequest3.getCurrentPassword(), testUser.getPassword()))
        .thenReturn(true);
    when(passwordEncoder.matches(userPasswordChangeRequest3.getNewPassword(), testUser.getPassword()))
        .thenReturn(true);

    assertThatThrownBy(() -> userService.updatePassword(testUser.getId(), userPasswordChangeRequest3))
      .isInstanceOf(ServiceException.class)
      .hasMessageContaining(ServiceExceptionCode.DUPLICATE_CURRENT_PASSWORD.getMessage());

    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  @DisplayName("회원탈퇴가 정상적으로 처리 된다.")
  void deleteUser_should_return_true() {
    when(userRepository.findByIdAndDeletedAtIsNull(testUser.getId()))
      .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(userDeleteRequest.getCurrentPassword(), testUser.getPassword()))
      .thenReturn(true);

    userService.deleteUser(testUser.getId(), userDeleteRequest);

    assertThat(testUser.getDeletedAt()).isNotNull();
    verify(userRepository).findByIdAndDeletedAtIsNull(testUser.getId());
  }


}