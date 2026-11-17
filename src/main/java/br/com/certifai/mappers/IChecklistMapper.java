package br.com.certifai.mappers;

import br.com.certifai.dto.ChecklistDTO;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Checklist;
import br.com.certifai.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {IItemChecklistMapper.class})
public interface IChecklistMapper {

    @Mapping(target = "certificacao", expression = "java(toCertificacao(dto.getCertificacaoId()))")
    @Mapping(target = "usuario", expression = "java(toUsuario(dto.getUsuarioId()))")
    Checklist toEntity(ChecklistDTO dto);

    @Mapping(target = "certificacaoId", source = "certificacao.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    ChecklistDTO toDTO(Checklist entity);

    default Certificacao toCertificacao(Long id) {
        if (id == null) return null;
        Certificacao c = new Certificacao();
        c.setId(id);
        return c;
    }

    default Usuario toUsuario(Long id) {
        if (id == null) return null;
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }
}
