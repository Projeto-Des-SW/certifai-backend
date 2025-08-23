package br.com.certifai.service.impl;

import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.model.Questao;
import br.com.certifai.repository.QuestaoRepository;
import br.com.certifai.service.interfaces.IQuestaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestaoService implements IQuestaoService {

    private final QuestaoRepository questaoRepository;

    @Transactional
    public Questao create(Questao questao) {
        questao.setId(null);
        return questaoRepository.save(questao);
    }

    @Transactional(readOnly = true)
    public List<Questao> findAll() {
        return questaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Questao> findById(Long id) {
        return questaoRepository.findById(id);
    }

    @Transactional
    public Questao update(Long id, Questao questaoDetails) {
        Questao questaoExistente = questaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Questão com ID " + id + " não encontrada."));

        questaoExistente.setEnunciado(questaoDetails.getEnunciado());
        questaoExistente.setCategoria(questaoDetails.getCategoria());
        questaoExistente.setDificuldade(questaoDetails.getDificuldade());

        if (questaoDetails.getCertificacoes() != null) {
            questaoExistente.setCertificacoes(questaoDetails.getCertificacoes());
        }

        return questaoRepository.save(questaoExistente);
    }

    @Transactional
    public void delete(Long id) {
        if (!questaoRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Questão com ID " + id + " não encontrada para exclusão.");
        }
        questaoRepository.deleteById(id);
    }
}