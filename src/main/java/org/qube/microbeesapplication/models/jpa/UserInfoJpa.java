package org.qube.microbeesapplication.models.jpa;

import lombok.Data;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collation = "com.qube.microbees::userInfo")
public class UserInfoJpa {
    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Indexed(unique = true)
    @Size(max = 50)
    private String email;
}
