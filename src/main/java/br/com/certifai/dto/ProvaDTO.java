package br.com.certifai.dto;

import br.com.certifai.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProvaDTO {
    private Long id;
    private String nome;
    private Integer pontuacao;
    private Long tempo;
    private Status status;
    private Long certificacaoId;
    private Long usuarioId;
    private List<Long> respostaIds;
    private List<Long> questaoIds;
}