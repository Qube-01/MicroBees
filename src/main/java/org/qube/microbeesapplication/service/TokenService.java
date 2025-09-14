package org.qube.microbeesapplication.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qube.microbeesapplication.config.MultiTenantMongoTemplate;
import org.qube.microbeesapplication.config.TokenUtils;
import org.qube.microbeesapplication.models.dto.TokenDto;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class TokenService {

    private final MultiTenantMongoTemplate  mongoTemplate;

    private final TokenUtils tokenUtils;

    public String getToken(String tenantId, TokenDto tokenDto) throws Exception {
        UserInfoJpa existingUserInfo = this.mongoTemplate.getMongoTemplate(tenantId).findOne(
            Query.query(Criteria.where("firstName").is(tokenDto.getFirstName()).and("email").is(tokenDto.getEmail())),
                UserInfoJpa.class
        );
        if(existingUserInfo == null) {
            log.error("Data not found for tenant {}", tenantId);
            throw new SecurityException("User not found");
        }
        return tokenUtils.getToken(existingUserInfo, tenantId);
    }
}
