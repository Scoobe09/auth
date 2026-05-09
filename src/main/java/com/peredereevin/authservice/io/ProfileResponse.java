package com.peredereevin.authservice.io;

import com.peredereevin.authservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse extends User {
    private String userId;
    private String name;
    private String email;
    private Boolean isAccountVerified;
}
