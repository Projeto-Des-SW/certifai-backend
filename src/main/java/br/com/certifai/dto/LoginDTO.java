package br.com.certifai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private UsuarioDTO usuario;
    private String token;

    public LoginDTO(Optional<UsuarioDTO> user, String token) {
        this.usuario = user.orElse(null);
        this.token = token;
    }
}
