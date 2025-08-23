package br.com.certifai.service.interfaces;

import br.com.certifai.model.Alternativa;

import java.util.List;
import java.util.Optional;

public interface IAlternativaService {
    Alternativa create(Alternativa alternativa);
    List<Alternativa> findAll();
    Optional<Alternativa> findById(Long id);
    Alternativa update(Long id, Alternativa alternativa);
    void delete(Long id);
}
