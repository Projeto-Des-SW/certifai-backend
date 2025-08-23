package br.com.certifai.controller.interfaces;

import br.com.certifai.dto.QuestaoDTO;
import br.com.certifai.response.AbstractResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Questões", description = "API para gerenciamento de Questões")
@RequestMapping("/questoes")
public interface QuestaoApi {

    @PostMapping
    @Operation(summary = "Criar uma nova questão")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Questão criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Certificação associada não encontrada")
    })
    ResponseEntity<AbstractResponse<QuestaoDTO>> criar(@Valid @RequestBody QuestaoDTO questaoDTO);

    @GetMapping
    @Operation(summary = "Listar todas as questões")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso")
    })
    ResponseEntity<AbstractResponse<List<QuestaoDTO>>> listarTodas();

    @GetMapping("/{id}")
    @Operation(summary = "Buscar questão por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questão encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada")
    })
    ResponseEntity<AbstractResponse<QuestaoDTO>> buscarPorId(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma questão existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questão atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Questão ou certificação associada não encontrada")
    })
    ResponseEntity<AbstractResponse<QuestaoDTO>> atualizar(@PathVariable("id") Long id, @Valid @RequestBody QuestaoDTO questaoDTO);

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover uma questão")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questão removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada")
    })
    ResponseEntity<AbstractResponse<Void>> remover(@PathVariable("id") Long id);
}
