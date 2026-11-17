package br.com.certifai.controller.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.certifai.controller.interfaces.ChecklistApi;
import br.com.certifai.dto.ChecklistDTO;
import br.com.certifai.dto.UsuarioDTO;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.mappers.IChecklistMapper;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Checklist;
import br.com.certifai.model.ItemChecklist;
import br.com.certifai.response.AbstractResponse;
import br.com.certifai.service.impl.ChecklistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChecklistController implements ChecklistApi {

    private final ChecklistService checklistService;
    private final IChecklistMapper checklistMapper;

    @Override
    public ResponseEntity<AbstractResponse<ChecklistDTO>> criar(ChecklistDTO checklistDTO) {
        log.debug("Recebida requisição para criar checklist: {}", checklistDTO);

        Checklist checklistParaSalvar = checklistMapper.toEntity(checklistDTO);
        Checklist checklistSalvo = checklistService.createChecklist(checklistParaSalvar);
        ChecklistDTO responseDto = checklistMapper.toDTO(checklistSalvo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AbstractResponse.success(responseDto, "Checklist criado com sucesso"));

    }

    @Override
    public ResponseEntity<AbstractResponse<ChecklistDTO>> atualizar(Long id, ChecklistDTO checklistDTO) {
        Checklist checklist = checklistMapper.toEntity(checklistDTO);
        Checklist atualizado = checklistService.updateChecklist(id, checklist);
        ChecklistDTO response = checklistMapper.toDTO(atualizado);

        return ResponseEntity.ok(AbstractResponse.success(response, "Checklist atualizado com sucesso"));
    }

    @Override
    public ResponseEntity<AbstractResponse<Void>> remover(Long id) {
        checklistService.deleteChecklist(id);

        return ResponseEntity.ok(AbstractResponse.success(null, "Checklist removido com sucesso"));
    }

    @Override
    public ResponseEntity<AbstractResponse<List<ChecklistDTO>>> listarTodos() {
        List<ChecklistDTO> response = checklistService.getAllChecklists().stream()
                .map(checklistMapper::toDTO)
                .toList();

        return ResponseEntity.ok(AbstractResponse.success(response));
    }

    @Override
    public ResponseEntity<AbstractResponse<ChecklistDTO>> buscarPorId(Long id) {

        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");

    }
}
