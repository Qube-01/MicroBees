package org.qube.microbeesapplication.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qube.microbeesapplication.config.MultiTenantMongoTemplate;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class UserInfoService {

    private final MultiTenantMongoTemplate mongoTemplate;

    private final MicroBeesMapper microBeesMapper = new MicroBeesMapper();

    public UserInfoDto newUserLogin(UserInfoDto userInfoDto, String tenantId) throws Exception {
//        Optional<UserInfoJpa> existingUserInfo = this.mongoTemplate.findByEmail(userInfoDto.getEmail());
//        if (existingUserInfo.isPresent()) {
//            throw new Exception("User already exists");
//        }
        UserInfoJpa userInfoJpa = microBeesMapper.convertModel(userInfoDto);
        try {
            this.mongoTemplate.getMongoTemplate(tenantId).save(userInfoJpa);
            return userInfoDto;
        } catch (Exception e) {
            log.error("Failed to store user info into database with message {}", e.getMessage());
            throw new Exception("Couldn't save user info");
        }
    }
}
