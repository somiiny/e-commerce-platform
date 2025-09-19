package com.sparta.camp.java.FinalProject.domain.auth.service;

import com.sparta.camp.java.FinalProject.domain.admin.repository.AdminRepository;
import com.sparta.camp.java.FinalProject.domain.auth.dto.CustomUserDetails;
import com.sparta.camp.java.FinalProject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;
  private final AdminRepository adminRepository;

  @Override
  public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmailAndDeletedAtIsNull(email)
        .map(user -> new CustomUserDetails(user.getEmail(), user.getPassword(), "ROLE_USER"))
        .orElseGet(() -> adminRepository.findByEmailAndDeletedAtIsNull(email)
            .map(admin -> new CustomUserDetails(admin.getEmail(), admin.getPassword(), "ROLE_ADMIN"))
            .orElseThrow(() -> new UsernameNotFoundException("No user/admin found with email: " + email))
        );
  }

}
