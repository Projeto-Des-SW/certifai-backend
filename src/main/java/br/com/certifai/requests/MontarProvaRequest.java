package br.com.certifai.requests;

import br.com.certifai.enums.Dificuldades;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MontarProvaRequest {
    @NotBlank(message = "Nome é obrigatório.")
    private String nome;

    @NotNull(message = "Usuário é obrigatório.")
    private Long usuarioId;

    @NotNull(message = "Ccertificação é obrigatório.")
    private Long certificacaoId;

    @NotNull(message = "Dificuldade é obrigatória.")
    private Dificuldades dificuldadeQuestoes;

    @Min(value = 1, message = "Número de questões deve ser no mínimo 1.")
    private int numeroDeQuestoes;

    private boolean comTempo = false;

    private Long tempoEmMinutos;
}
