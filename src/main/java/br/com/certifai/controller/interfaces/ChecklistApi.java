package br.com.certifai.controller.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import br.com.certifai.dto.ChecklistDTO;
import br.com.certifai.response.AbstractResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Checklists", description = "Endpoints para planos e listas de estudo preparatórios")
@RequestMapping("/checklist")
public interface ChecklistApi {
    @PostMapping
    @Operation(summary = "Criar um novo checklist")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Checklist criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    ResponseEntity<AbstractResponse<ChecklistDTO>> criar(@Valid @RequestBody ChecklistDTO checklistDTO);

    @GetMapping("/{id}")
    @Operation(summary = "Buscar checklist por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checklist encontrado"),
        @ApiResponse(responseCode = "404", description = "Checklist não encontrado")
    })
    ResponseEntity<AbstractResponse<ChecklistDTO>> buscarPorId(@PathVariable Long id);

    @GetMapping
    @Operation(summary = "Listar todos os checklists")
    @ApiResponse(responseCode = "200", description = "Lista de checklists")
    ResponseEntity<AbstractResponse<List<ChecklistDTO>>> listarTodos();

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar checklist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checklist atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Checklist não encontrado")
    })
    ResponseEntity<AbstractResponse<ChecklistDTO>> atualizar(
        @PathVariable Long id, 
        @Valid @RequestBody ChecklistDTO checklistDTO
    );

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir checklist")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Checklist excluído"),
        @ApiResponse(responseCode = "404", description = "Checklist não encontrado")
    })
    ResponseEntity<AbstractResponse<Void>> remover(@PathVariable Long id);
}
