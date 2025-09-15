package com.sparta.camp.java.FinalProject.domain.user.mapper;

import com.sparta.camp.java.FinalProject.domain.user.dto.UserResponse;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserResponse toResponse(User user);
}
