package com.sparta.camp.java.FinalProject.domain.admin.service;

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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminMapper adminMapper;

  public AdminResponse getAdminById (Long id) {
    Admin admin = this.getAdmin(id);
    return adminMapper.toResponse(admin);
  }

  public AdminResponse createAdmin(AdminCreateRequest adminCreateRequest) {
    if (adminRepository.findByEmailAndDeletedAtIsNull(adminCreateRequest.getEmail()).isPresent()) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_EMAIL);
    }

    String encodedPassword = passwordEncoder.encode(adminCreateRequest.getPassword());

    Admin admin = Admin.builder()
        .email(adminCreateRequest.getEmail())
        .name(adminCreateRequest.getName())
        .role(Role.ROLE_ADMIN)
        .password(encodedPassword)
        .phoneNumber(adminCreateRequest.getPhoneNumber())
        .zipCode(adminCreateRequest.getZipCode())
        .address(adminCreateRequest.getAddress())
        .detailAddress(adminCreateRequest.getDetailAddress())
        .birthDate(adminCreateRequest.getBirthDate())
        .build();

    Admin savedAdmin = adminRepository.save(admin);
    return adminMapper.toResponse(savedAdmin);
  }

  public void updateAdmin(Long id, AdminUpdateRequest adminUpdateRequest) {
    Admin admin = this.getAdmin(id);

    admin.setName(adminUpdateRequest.getName());
    admin.setPhoneNumber(adminUpdateRequest.getPhoneNumber());
    admin.setZipCode(adminUpdateRequest.getZipCode());
    admin.setAddress(adminUpdateRequest.getAddress());
    admin.setDetailAddress(adminUpdateRequest.getDetailAddress());
    admin.setBirthDate(adminUpdateRequest.getBirthDate());
  }

  public void updatePassword (Long id, AdminPasswordChangeRequest adminPasswordChangeRequest) {
    Admin admin = this.getAdmin(id);

    if (!passwordEncoder.matches(adminPasswordChangeRequest.getCurrentPassword(), admin.getPassword())
        || !adminPasswordChangeRequest.getNewPassword().equals(adminPasswordChangeRequest.getConfirmPassword())) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PASSWORD);
    }

    if (passwordEncoder.matches(adminPasswordChangeRequest.getNewPassword(), admin.getPassword())) {
      throw new ServiceException(ServiceExceptionCode.DUPLICATE_CURRENT_PASSWORD);
    }

    admin.setPassword(passwordEncoder.encode(adminPasswordChangeRequest.getNewPassword()));
  }

  public void deleteAdmin(Long id, AdminDeleteRequest adminDeleteRequest) {
    Admin admin = this.getAdmin(id);

    if (!passwordEncoder.matches(adminDeleteRequest.getCurrentPassword(), admin.getPassword())) {
      throw new ServiceException(ServiceExceptionCode.NOT_MATCH_PASSWORD);
    }

    admin.setDeletedAt(LocalDateTime.now());
  }

  private Admin getAdmin (Long id) {
    return adminRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new ServiceException(ServiceExceptionCode.NOT_FOUND_ADMIN));
  }

}
