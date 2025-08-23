package br.com.certifai.service.interfaces;

import br.com.certifai.model.Questao;

import java.util.List;
import java.util.Optional;

public interface IQuestaoService {
    Questao create(Questao questao);
    List<Questao> findAll();
    Optional<Questao> findById(Long id);
    Questao update(Long id, Questao questaoDetails);
    void delete(Long id);
}
