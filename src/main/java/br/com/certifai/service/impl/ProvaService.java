package br.com.certifai.service.impl;

import br.com.certifai.enums.Status;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.model.*;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.repository.ProvaRepository;
import br.com.certifai.repository.QuestaoRepository;
import br.com.certifai.repository.UsuarioRepository;
import br.com.certifai.requests.MontarProvaRequest;
import br.com.certifai.service.interfaces.IProvaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProvaService implements IProvaService {
    private final ProvaRepository provaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CertificacaoRepository certificacaoRepository;
    private final QuestaoRepository questaoRepository;
    private final CertificacaoService certificacaoService;

    @Override
    @Transactional
    public Prova create(Prova prova) {
        prova.setId(null);
        prova.setStatus(Status.PENDENTE);
        prova.setPontuacao(0);
        return provaRepository.save(prova);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prova> findById(Long id) {
        return provaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prova> findAll() {
        return provaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prova> findByUsuarioId(Long usuarioId) {
        List<Prova> provas = provaRepository.findByUsuarioId(usuarioId);
        return (provas != null && !provas.isEmpty()) ? provas : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prova> findByCertificacaoId(Long certificacaoId) {
        List<Prova> provas = provaRepository.findByCertificacaoId(certificacaoId);
        return (provas != null && !provas.isEmpty()) ? provas : Collections.emptyList();

    }

    @Override
    @Transactional
    public Prova update(Long id, Prova provaDetails) {
        Prova provaExistente = provaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Prova não encontrada com o ID: " + id));

        provaExistente.setTempo(provaDetails.getTempo());
        provaExistente.setTempo(provaDetails.getTempo());

        return provaRepository.save(provaExistente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!provaRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Prova não encontrada com o ID: " + id);
        }
        provaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Prova montarProvaPersonalizada(MontarProvaRequest request) {
        if (request.isComTempo() && (request.getTempoEmMinutos() == null || request.getTempoEmMinutos() <= 0)) {
            throw new IllegalArgumentException("O tempo em minutos deve ser informado e maior que zero quando o cronômetro está ativado.");
        }
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado com o ID: " + request.getUsuarioId()));
        Certificacao certificacao = certificacaoRepository.findById(request.getCertificacaoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Certificação não encontrada com o ID: " + request.getCertificacaoId()));
        List<Questao> questoesSelecionadas = questaoRepository.findRandomQuestoesByDificuldade(
                request.getDificuldadeQuestoes().name(),
                request.getNumeroDeQuestoes()
        );
        if (questoesSelecionadas.size() < request.getNumeroDeQuestoes()) {
            throw new IllegalStateException("Não há questões suficientes no banco com a dificuldade solicitada.");
        }
        certificacao.getQuestoes().addAll(questoesSelecionadas);
        certificacaoRepository.save(certificacao);
        Prova novaProva = Prova.builder()
                .nome(request.getNome())
                .usuario(usuario)
                .certificacao(certificacao)
                .status(Status.PENDENTE)
                .pontuacao(0)
                .comTempo(request.isComTempo())
                .tempo(request.isComTempo() ? request.getTempoEmMinutos() : null)
                .questoes(new HashSet<>(questoesSelecionadas))
                .build();
        Prova provaSalva = provaRepository.save(novaProva);
        if (request.isComTempo()) {
            certificacaoService.calcularEAtualizarTempoTotal(certificacao.getId());
        }
        return provaSalva;
    }

    @Override
    @Transactional
    public Prova iniciarProva(Long provaId) {
        Prova prova = provaRepository.findById(provaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Prova não encontrada com o ID: " + provaId));
        if (prova.getStatus() != Status.PENDENTE) {
            throw new IllegalStateException("Esta prova não pode ser iniciada, pois seu status é: " + prova.getStatus());
        }
        prova.setStatus(Status.ANDAMENTO);
        return provaRepository.save(prova);
    }

    @Override
    @Transactional
    public Prova finalizarProva(Long provaId) {
        Prova prova = provaRepository.findById(provaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Prova não encontrada com o ID: " + provaId));
        if (prova.getStatus() != Status.ANDAMENTO) {
            throw new IllegalStateException("Esta prova não pode ser finalizada, pois seu status é: " + prova.getStatus());
        }
        int pontuacaoFinal = 0;
        for (Resposta resposta : prova.getRespostas()) {
            if (resposta.isAcertou()) {
                pontuacaoFinal++;
            }
        }
        prova.setPontuacao(pontuacaoFinal);
        prova.setStatus(Status.CONCLUIDA);
        return provaRepository.save(prova);
    }
}
