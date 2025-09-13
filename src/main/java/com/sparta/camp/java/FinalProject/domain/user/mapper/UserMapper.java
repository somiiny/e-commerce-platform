package com.sparta.camp.java.FinalProject.domain.user.mapper;

import com.sparta.camp.java.FinalProject.domain.user.dto.UserResponse;
import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "id", target = "userId")
  UserResponse toResponse(User user);
}
