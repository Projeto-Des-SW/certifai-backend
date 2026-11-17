package br.com.certifai.controller.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.certifai.dto.EventoDTO;
import br.com.certifai.response.AbstractResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Eventos e Agenda", description = "Endpoints para agendamento de eventos e sessões de estudo")
@RequestMapping("/eventos")
public interface EventoApi {
    @GetMapping
    @Operation(summary = "Listar todos os eventos")
    @ApiResponse(responseCode = "200", description = "Lista de eventos")
    ResponseEntity<AbstractResponse<List<EventoDTO>>> listarTodos();

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventoDTO> buscarPorId(@PathVariable Long id);

    @PostMapping
    @Operation(summary = "Criar um novo evento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    ResponseEntity<EventoDTO> criar(@RequestBody EventoDTO dto);

    @PutMapping("/{eventoId}")
    @Operation(summary = "Editar um evento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento associado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<EventoDTO> editarEvento(
            @PathVariable Long eventoId,
            @RequestBody EventoDTO dto);

    @PutMapping("/{eventoId}/itens/{itemId}")
    @Operation(summary = "Associar um item a um evento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item associado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Evento ou Item não encontrado")
    })
    ResponseEntity<EventoDTO> adicionarItem(
            @PathVariable Long eventoId,
            @PathVariable Long itemId);

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir evento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento excluído"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    ResponseEntity<Void> excluir(@PathVariable Long id);

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar a agenda de eventos de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de eventos do usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    ResponseEntity<List<EventoDTO>> listarAgendaPorUsuario(@PathVariable Long usuarioId);

}
