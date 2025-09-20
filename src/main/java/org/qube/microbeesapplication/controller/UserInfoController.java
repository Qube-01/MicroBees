package org.qube.microbeesapplication.controller;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.qube.microbeesapplication.models.dto.TokenDto;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.service.TokenService;
import org.qube.microbeesapplication.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/v1/microBees")
@Generated
public class UserInfoController {

    private final UserInfoService userInfoService;

    private final TokenService tokenService;

    @PostMapping("/userInfo")
    public ResponseEntity<?> createUser(@RequestParam(name = "tenantId") String tenantId,
                                                  @RequestBody UserInfoDto userInfoDto) {
        try {
            return new ResponseEntity<>(userInfoService.newUserLogin(userInfoDto, tenantId), HttpStatus.OK);
        } catch (DuplicateKeyException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("Failed to store user info into database with message {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/userInfo")
    public ResponseEntity<?> deleteUser(
            @RequestParam(name = "tenantId") String tenantId,
            @RequestParam(name = "email") String email) {
        try {
            boolean deleted = userInfoService.deleteUserByEmail(email, tenantId);
            if (deleted) {
                return new ResponseEntity<>("User deleted successfully.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/token")
    public ResponseEntity<?> createToken(@RequestParam(name = "tenantId") String tenantId,
                                                   @RequestBody TokenDto tokenDto) {
        try {
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("access_token", tokenService.getToken(tenantId, tokenDto));
            return new ResponseEntity<>(tokenMap, HttpStatus.OK);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Failed to store token into database with message {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
