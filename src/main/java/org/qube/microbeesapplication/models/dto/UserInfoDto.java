package org.qube.microbeesapplication.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class UserInfoDto {

    @JsonProperty("name")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("mailId")
    private String email;
}
