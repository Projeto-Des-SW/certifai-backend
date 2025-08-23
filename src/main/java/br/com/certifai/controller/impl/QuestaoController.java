package br.com.certifai.controller.impl;

import br.com.certifai.controller.interfaces.QuestaoApi;
import br.com.certifai.dto.QuestaoDTO;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.mappers.IQuestaoMapper;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Questao;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.response.AbstractResponse;
import br.com.certifai.service.interfaces.IQuestaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class QuestaoController implements QuestaoApi {

    private final IQuestaoService questaoService;
    private final IQuestaoMapper questaoMapper;
    private final CertificacaoRepository certificacaoRepository;

    @Override
    public ResponseEntity<AbstractResponse<QuestaoDTO>> criar(QuestaoDTO questaoDTO) {
        Questao questaoParaCriar = questaoMapper.toEntity(questaoDTO);

        if (questaoDTO.getCertificacaoIds() != null && !questaoDTO.getCertificacaoIds().isEmpty()) {
            List<Certificacao> certificacoes = certificacaoRepository.findAllById(questaoDTO.getCertificacaoIds());
            if (certificacoes.size() != questaoDTO.getCertificacaoIds().size()) {
                throw new EntidadeNaoEncontradaException("Uma ou mais certificações não foram encontradas.");
            }
            questaoParaCriar.setCertificacoes(certificacoes);
        } else {
            questaoParaCriar.setCertificacoes(Collections.emptyList());
        }

        Questao novaQuestao = questaoService.create(questaoParaCriar);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AbstractResponse.success(questaoMapper.toDTO(novaQuestao), "Questão criada com sucesso."));
    }

    @Override
    public ResponseEntity<AbstractResponse<List<QuestaoDTO>>> listarTodas() {
        List<Questao> questoes = questaoService.findAll();

        List<QuestaoDTO> dtos = questoes.stream()
                .map(questaoMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(AbstractResponse.success(dtos));
    }

    @Override
    public ResponseEntity<AbstractResponse<QuestaoDTO>> buscarPorId(Long id) {
        Questao questao = questaoService.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Questão com ID " + id + " não encontrada."));

        return ResponseEntity.ok(AbstractResponse.success(questaoMapper.toDTO(questao)));
    }

    @Override
    public ResponseEntity<AbstractResponse<QuestaoDTO>> atualizar(Long id, QuestaoDTO questaoDTO) {
        Questao detalhesQuestao = questaoMapper.toEntity(questaoDTO);

        if (questaoDTO.getCertificacaoIds() != null && !questaoDTO.getCertificacaoIds().isEmpty()) {
            List<Certificacao> certificacoes = certificacaoRepository.findAllById(questaoDTO.getCertificacaoIds());
            detalhesQuestao.setCertificacoes(certificacoes);
        } else {
            detalhesQuestao.setCertificacoes(Collections.emptyList());
        }

        Questao questaoAtualizada = questaoService.update(id, detalhesQuestao);

        return ResponseEntity.ok(AbstractResponse.success(questaoMapper.toDTO(questaoAtualizada), "Questão atualizada com sucesso."));
    }

    @Override
    public ResponseEntity<AbstractResponse<Void>> remover(Long id) {
        questaoService.delete(id);
        return ResponseEntity.ok(AbstractResponse.success(null, "Questão removida com sucesso."));
    }
}