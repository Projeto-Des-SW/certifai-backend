package br.com.certifai.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.certifai.dto.ChecklistDTO;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.mappers.IChecklistMapper;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Checklist;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.repository.ChecklistRepository;
import br.com.certifai.service.interfaces.IChecklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistService implements IChecklistService {
    private final ChecklistRepository checklistRepository;
    private final CertificacaoRepository certificacaoRepository;
    private final AuthService usuarioService;
    private final IChecklistMapper checklistMapper;

    @Override
    @Transactional
    public Checklist createChecklist(Checklist checklist) {
        if (checklist.getCertificacao() == null || checklist.getCertificacao().getId() == null) {
            throw new IllegalArgumentException("É obrigatório informar o ID da certificação.");
        }
        log.debug("Processando criação de checklist: {}", checklist);


        Certificacao certificacaoGerenciada = certificacaoRepository.findById(checklist.getCertificacao().getId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Certificação com ID " + checklist.getCertificacao().getId() + " não encontrada."));

        Usuario usuarioLogado = usuarioService.getPrincipal()
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário logado não encontrado."));

        checklist.setId(null);

        checklist.setCertificacao(certificacaoGerenciada);
        checklist.setUsuario(usuarioLogado);

        if (checklist.getItensChecklist() != null) {
            checklist.getItensChecklist().forEach(item -> {
                item.setId(null);
                item.setChecklist(checklist);
            });
        }

        return checklistRepository.save(checklist);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Checklist> getChecklistById(Long id) {
        return checklistRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Checklist> getAllChecklists() {
        return checklistRepository.findAll();
    }

    @Override
    @Transactional
    public Checklist updateChecklist(Long id, Checklist checklist) {
        Checklist existente = getChecklistById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Checklist não encontrada com o ID: " + id));

        existente.setNome(checklist.getNome());
        existente.setDescricao(checklist.getDescricao());

        if (checklist.getCertificacao() != null &&
                !checklist.getCertificacao().getId().equals(existente.getCertificacao().getId())) {

            Certificacao novaCertificacao = certificacaoRepository.findById(checklist.getCertificacao().getId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Certificação não encontrada"));
            existente.setCertificacao(novaCertificacao);
        }

        Checklist atualizado = checklistRepository.save(existente);
        return atualizado;
    }

    @Override
    @Transactional
    public void deleteChecklist(Long id) {
        Checklist checklist = getChecklistById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Checklist não encontrada com o ID: " + id));

        checklistRepository.delete(checklist);
    }
}
