package com.sparta.camp.java.FinalProject.domain.admin.service;

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
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminCreateRequest;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminDeleteRequest;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminPasswordChangeRequest;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminResponse;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import com.sparta.camp.java.FinalProject.domain.admin.mapper.AdminMapper;
import com.sparta.camp.java.FinalProject.domain.admin.repository.AdminRepository;
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
class AdminServiceTest {

  @InjectMocks
  private AdminService adminService;

  @Mock
  private AdminRepository adminRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AdminMapper adminMapper;

  private AdminCreateRequest adminCreateRequest;
  private AdminUpdateRequest adminUpdateRequest;
  private AdminPasswordChangeRequest adminPasswordChangeRequest;
  private AdminPasswordChangeRequest adminPasswordChangeRequest2;
  private AdminPasswordChangeRequest adminPasswordChangeRequest3;
  private AdminDeleteRequest adminDeleteRequest;

  private Admin testAdmin;
  private AdminResponse testResponse;

  @BeforeEach
  void setUp() {
    adminCreateRequest = new AdminCreateRequest();
    ReflectionTestUtils.setField(adminCreateRequest, "email", "somsom@test.com");
    ReflectionTestUtils.setField(adminCreateRequest, "name", "test1");
    ReflectionTestUtils.setField(adminCreateRequest, "password", "Test1234*");
    ReflectionTestUtils.setField(adminCreateRequest, "phoneNumber","010-1234-5678");

    adminUpdateRequest = new AdminUpdateRequest();
    ReflectionTestUtils.setField(adminUpdateRequest, "name", "test2");
    ReflectionTestUtils.setField(adminUpdateRequest, "phoneNumber", "010-1004-1004");

    adminPasswordChangeRequest = new AdminPasswordChangeRequest();
    ReflectionTestUtils.setField(adminPasswordChangeRequest, "currentPassword", "Test1234*");
    ReflectionTestUtils.setField(adminPasswordChangeRequest, "newPassword", "Test5678*");
    ReflectionTestUtils.setField(adminPasswordChangeRequest, "confirmPassword", "Test5678*");

    adminPasswordChangeRequest2 = new AdminPasswordChangeRequest();
    ReflectionTestUtils.setField(adminPasswordChangeRequest2, "currentPassword", "Test1235*");
    ReflectionTestUtils.setField(adminPasswordChangeRequest2, "newPassword", "Test5678*");
    ReflectionTestUtils.setField(adminPasswordChangeRequest2, "confirmPassword", "Test5678*");

    adminPasswordChangeRequest3 = new AdminPasswordChangeRequest();
    ReflectionTestUtils.setField(adminPasswordChangeRequest3, "currentPassword", "Test1234*");
    ReflectionTestUtils.setField(adminPasswordChangeRequest3, "newPassword", "Test1234*");
    ReflectionTestUtils.setField(adminPasswordChangeRequest3, "confirmPassword", "Test1234*");

    adminDeleteRequest = new AdminDeleteRequest();
    ReflectionTestUtils.setField(adminDeleteRequest, "currentPassword", "Test1234*");

    testAdmin = Admin.builder()
        .email(adminCreateRequest.getEmail())
        .name(adminCreateRequest.getName())
        .role(Role.ROLE_ADMIN)
        .password("ENCODED_PASSWORD")
        .phoneNumber(adminCreateRequest.getPhoneNumber())
        .build();

    testResponse = AdminResponse.builder()
        .id(testAdmin.getId())
        .email(testAdmin.getEmail())
        .name(testAdmin.getName())
        .role(testAdmin.getRole())
        .build();
  }

  @Test
  @DisplayName("정상적으로 관리자 등록이 된다.")
  void createAdmin_should_return_true() {
    when(adminRepository.findByEmailAndDeletedAtIsNull(adminCreateRequest.getEmail()))
        .thenReturn(Optional.empty());
    when(passwordEncoder.encode(adminCreateRequest.getPassword()))
        .thenReturn("ENCODED_PASSWORD");
    when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);
    when(adminMapper.toResponse(testAdmin)).thenReturn(testResponse);

    AdminResponse result = adminService.createAdmin(adminCreateRequest);

    assertThat(result).isNotNull();

    verify(adminRepository).findByEmailAndDeletedAtIsNull(adminCreateRequest.getEmail());
    verify(passwordEncoder).encode(adminCreateRequest.getPassword());
    verify(adminRepository).save(any(Admin.class));
    verify(adminMapper).toResponse(testAdmin);
  }

  @Test
  @DisplayName("중복된 메일인 경우 오류가 발생한다.")
  void createAdmin_shouldThrowException_when_email_is_existed() {
    when(adminRepository.findByEmailAndDeletedAtIsNull(adminCreateRequest.getEmail()))
        .thenReturn(Optional.of(new Admin()));

    assertThatThrownBy(() -> adminService.createAdmin(adminCreateRequest))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_EMAIL.getMessage());

    verify(adminRepository).findByEmailAndDeletedAtIsNull(anyString());
    verifyNoMoreInteractions(adminRepository);
  }

  @Test
  @DisplayName("정상적으로 관리자 조회가 된다.")
  void getAdminById_should_return_true() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.of(testAdmin));
    when(adminMapper.toResponse(testAdmin)).thenReturn(testResponse);

    AdminResponse result = adminService.getAdminById(testAdmin.getId());

    assertThat(result).isNotNull();
    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
    verify(adminMapper).toResponse(testAdmin);
  }

  @Test
  @DisplayName("존재하지 않는 관리자로 오류가 발생한다.")
  void getAdminById_shouldThrowException_when_admin_is_not_existed() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminService.getAdminById(testAdmin.getId()))
      .isInstanceOf(ServiceException.class)
      .hasMessageContaining(ServiceExceptionCode.NOT_FOUND_ADMIN.getMessage());

    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
    verifyNoMoreInteractions(adminRepository);
  }

  @Test
  @DisplayName("정상적으로 관리자 수정이 된다.")
  void updateAdmin_should_return_true() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.of(testAdmin));

    adminService.updateAdmin(testAdmin.getId(), adminUpdateRequest);

    assertThat(testAdmin.getName()).isEqualTo(adminUpdateRequest.getName());
    assertThat(testAdmin.getPhoneNumber()).isEqualTo(adminUpdateRequest.getPhoneNumber());

    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
  }

  @Test
  @DisplayName("비밀번호가 정상적으로 수정 된다.")
  void updatePassword_should_return_true() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.of(testAdmin));
    when(passwordEncoder.matches(adminPasswordChangeRequest.getCurrentPassword(), testAdmin.getPassword()))
        .thenReturn(true);
    when(passwordEncoder.matches(adminPasswordChangeRequest.getNewPassword(), testAdmin.getPassword()))
        .thenReturn(false);
    when(passwordEncoder.encode(adminPasswordChangeRequest.getNewPassword()))
        .thenReturn("ENCODED_PASSWORD");

    adminService.updatePassword(testAdmin.getId(), adminPasswordChangeRequest);

    assertThat(testAdmin.getPassword()).isEqualTo("ENCODED_PASSWORD");
    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
  }

  @Test
  @DisplayName("기존 비밀번호와 일치하지 않는 경우 오류가 발생한다.")
  void updatePassword_shouldThrowException_when_currentPassword_is_wrong() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.of(testAdmin));
    when(passwordEncoder.matches(adminPasswordChangeRequest2.getCurrentPassword(), testAdmin.getPassword()))
        .thenReturn(false);

    assertThatThrownBy(() -> adminService.updatePassword(testAdmin.getId(), adminPasswordChangeRequest2))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.NOT_MATCH_PASSWORD.getMessage());

    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
    verifyNoMoreInteractions(adminRepository);
  }

  @Test
  @DisplayName("새 비밀번호가 확인 비밀번호와 일치하지 않는 경우 오류가 발생한다.")
  void updatePassword_shouldThrowException_when_newPassword_is_not_match() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.of(testAdmin));
    when(passwordEncoder.matches(adminPasswordChangeRequest3.getCurrentPassword(), testAdmin.getPassword()))
        .thenReturn(true);
    when(passwordEncoder.matches(adminPasswordChangeRequest3.getNewPassword(), testAdmin.getPassword()))
        .thenReturn(true);

    assertThatThrownBy(() -> adminService.updatePassword(testAdmin.getId(), adminPasswordChangeRequest3))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining(ServiceExceptionCode.DUPLICATE_CURRENT_PASSWORD.getMessage());

    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
    verifyNoMoreInteractions(adminRepository);
  }

  @Test
  @DisplayName("관리자 탈퇴가 정상적으로 처리 된다.")
  void deleteAdmin_should_return_true() {
    when(adminRepository.findByIdAndDeletedAtIsNull(testAdmin.getId()))
        .thenReturn(Optional.of(testAdmin));
    when(passwordEncoder.matches(adminDeleteRequest.getCurrentPassword(), testAdmin.getPassword()))
        .thenReturn(true);

    adminService.deleteAdmin(testAdmin.getId(), adminDeleteRequest);

    assertThat(testAdmin.getDeletedAt()).isNotNull();
    verify(adminRepository).findByIdAndDeletedAtIsNull(testAdmin.getId());
  }

}