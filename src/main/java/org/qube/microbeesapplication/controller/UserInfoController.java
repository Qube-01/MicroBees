package org.qube.microbeesapplication.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController("/v1/microBees")
public class UserInfoController {

    private final UserInfoService userInfoService;

    @PostMapping("/userInfo")
    public ResponseEntity<UserInfoDto> createUser(@RequestParam(name = "tenantId") String tenantId,
                                                  @RequestBody UserInfoDto userInfoDto) {
        try {
            return new ResponseEntity<>(userInfoService.newUserLogin(userInfoDto, tenantId), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to store user info into database with message {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
