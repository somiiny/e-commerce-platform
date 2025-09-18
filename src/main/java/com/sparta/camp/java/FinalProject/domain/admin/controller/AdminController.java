package com.sparta.camp.java.FinalProject.domain.admin.controller;

import com.sparta.camp.java.FinalProject.common.response.ApiResponse;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminCreateRequest;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminDeleteRequest;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminPasswordChangeRequest;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminResponse;
import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminUpdateRequest;
import com.sparta.camp.java.FinalProject.domain.admin.service.AdminService;
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
@RequestMapping("/api/admins")
public class AdminController {

  private final AdminService adminService;

  @GetMapping("/{adminId}")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<AdminResponse> getAdminById(@PathVariable Long adminId){
    return ApiResponse.success(adminService.getAdminById(adminId));
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<AdminResponse> createAdmin(@Valid @RequestBody AdminCreateRequest adminCreateRequest) {
    return ApiResponse.success(adminService.createAdmin(adminCreateRequest));
  }

  @PutMapping("/{adminId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> updateAdmin(@PathVariable Long adminId, @Valid @RequestBody AdminUpdateRequest adminUpdateRequest) {
    adminService.updateAdmin(adminId, adminUpdateRequest);
    return ApiResponse.success();
  }

  @PutMapping("/{adminId}/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> changePassword(@PathVariable Long adminId,
      @Valid @RequestBody AdminPasswordChangeRequest adminPasswordChangeRequest) {
    adminService.updatePassword(adminId, adminPasswordChangeRequest);
    return ApiResponse.success();
  }

  @DeleteMapping("/{adminId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ApiResponse<Void> deleteAdmin(@PathVariable Long adminId,
      @Valid @RequestBody AdminDeleteRequest adminDeleteRequest) {
    adminService.deleteAdmin(adminId, adminDeleteRequest);
    return ApiResponse.success();
  }

}
