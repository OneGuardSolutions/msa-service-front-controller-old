package solutions.oneguard.msa.front.controller.security;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountCredentials {
    private String username;
    private String password;
}
