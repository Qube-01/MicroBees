package org.qube.microbeesapplication.models.jpa;

import jakarta.persistence.Id;
import lombok.Data;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static org.qube.microbeesapplication.utils.Constants.NAME_SPACE;
import static org.qube.microbeesapplication.utils.Constants.USER_INFO_TABLE;

@Data
@Document(collection = NAME_SPACE+"_"+USER_INFO_TABLE)
public class UserInfoJpa {
    @Id
    private String id;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Indexed(unique = true)
    @Size(max = 50)
    private String email;
}
