package com.pbear.user.core.mapper;

import com.pbear.user.core.data.entity.UserInfo;
import com.pbear.user.rest.dto.ResUserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserInfoMapper {
  UserInfoMapper INSTANCE = Mappers.getMapper(UserInfoMapper.class);

  ResUserInfo toResGetUserInfo(UserInfo userInfo);
}
