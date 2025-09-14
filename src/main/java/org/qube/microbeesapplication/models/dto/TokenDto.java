package org.qube.microbeesapplication.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class TokenDto {

    @JsonProperty("name")
    @NotBlank
    private String firstName;

    @JsonProperty("mailId")
    @NotBlank
    private String email;
}
