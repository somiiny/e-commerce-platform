package com.sparta.camp.java.FinalProject.domain.admin.mapper;

import com.sparta.camp.java.FinalProject.domain.admin.dto.AdminResponse;
import com.sparta.camp.java.FinalProject.domain.admin.entity.Admin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {

  AdminResponse toResponse(Admin admin);

}
