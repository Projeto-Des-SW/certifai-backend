package br.com.certifai.mappers;

import br.com.certifai.dto.ProvaDTO;
import br.com.certifai.model.Prova;
import br.com.certifai.model.Resposta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IProvaMapper {
    @Mapping(source = "certificacao.id", target = "certificacaoId")
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "respostas", target = "respostaIds", qualifiedByName = "respostasToIds")
    @Mapping(target = "questaoIds", expression = "java(prova.getQuestoes() != null ? prova.getQuestoes().stream().map(q -> q.getId()).toList() : null)")
    ProvaDTO toDTO(Prova prova);

    @Mapping(target = "certificacao", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    Prova toEntity(ProvaDTO provaDTO);

    @Named("respostasToIds")
    default List<Long> respostasToIds(Set<Resposta> respostas) {
        if (respostas == null) {
            return null;
        }
        return respostas.stream()
                .map(Resposta::getId)
                .collect(Collectors.toList());
    }
}
