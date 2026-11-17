package br.com.certifai.mappers;

import br.com.certifai.dto.UsuarioDTO;
import br.com.certifai.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUsuarioMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "tokenExpiresAt", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "resetTokenExpiresAt", source = "resetTokenExpiration")
    @Mapping(target = "itensChecklist", ignore = true)
    @Mapping(target = "eventos", ignore = true)
    @Mapping(target = "provas", ignore = true)
    Usuario toEntity(UsuarioDTO dto);

    @Mapping(target = "resetTokenExpiration", source = "resetTokenExpiresAt")
    UsuarioDTO toDTO(Usuario entity);
}
