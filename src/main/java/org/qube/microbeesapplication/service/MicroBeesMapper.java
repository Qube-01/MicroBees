package org.qube.microbeesapplication.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;

@Slf4j
public class MicroBeesMapper {

    private final ModelMapper modelMapper = new ModelMapper();


    public MicroBeesMapper() {
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        TypeMap<UserInfoDto, UserInfoJpa> userInfoTypeMap = modelMapper.typeMap(UserInfoDto.class, UserInfoJpa.class);
        userInfoTypeMap.addMapping(UserInfoDto::getFirstName, UserInfoJpa::setFirstName);
        userInfoTypeMap.addMapping(UserInfoDto::getLastName, UserInfoJpa::setLastName);
        userInfoTypeMap.addMapping(UserInfoDto::getEmail, UserInfoJpa::setEmail);
    }

    public UserInfoJpa convertModel(UserInfoDto userInfoDto) {
        try {
            return modelMapper.map(userInfoDto, UserInfoJpa.class);
        } catch (Exception e) {
            log.error("Failed to map user info {}", e.getMessage());
            throw e;
        }
    }
}
