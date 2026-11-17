package br.com.certifai.controller.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.certifai.dto.CertificacaoDTO;
import br.com.certifai.dto.ItemChecklistDTO;
import br.com.certifai.model.ItemChecklist;
import br.com.certifai.response.AbstractResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Itens de Checklist", description = "Endpoints para itens e tarefas individuais de checklist")
@RequestMapping("/checklists/{checklistId}/itens")
public interface ItemChecklistApi {

    @PostMapping
    @Operation(summary = "Adicionar um novo item a um checklist")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Checklist não encontrado")
    })
    ResponseEntity<AbstractResponse<ItemChecklistDTO>> criar(
            @PathVariable("checklistId") Long checklistId,
            @Valid @RequestBody ItemChecklistDTO itemChecklistDTO);

    @GetMapping("/{itemId}")
    @Operation(summary = "Buscar item por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    ResponseEntity<AbstractResponse<ItemChecklistDTO>> getById(
            @PathVariable("checklistId") Long checklistId,
            @PathVariable("itemId") Long itemId);

    @GetMapping
    @Operation(summary = "Listar todos os itens do checklist")
    @ApiResponse(responseCode = "200", description = "Lista de itens")
    ResponseEntity<AbstractResponse<List<ItemChecklistDTO>>> getAllByChecklist(
            @PathVariable("checklistId") Long checklistId);

    @PutMapping("/{itemId}")
    @Operation(summary = "Atualizar item do checklist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    ResponseEntity<AbstractResponse<ItemChecklistDTO>> update(
            @PathVariable("checklistId") Long checklistId,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody ItemChecklistDTO itemDTO);

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Excluir item do checklist")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item excluído"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    ResponseEntity<Void> delete(
            @PathVariable("checklistId") Long checklistId,
            @PathVariable("itemId") Long itemId);

}
