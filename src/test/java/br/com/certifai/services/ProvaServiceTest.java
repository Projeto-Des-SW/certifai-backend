package br.com.certifai.services;

import br.com.certifai.enums.Dificuldades;
import br.com.certifai.enums.Status;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.model.*;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.repository.ProvaRepository;
import br.com.certifai.repository.QuestaoRepository;
import br.com.certifai.repository.UsuarioRepository;
import br.com.certifai.requests.MontarProvaRequest;
import br.com.certifai.service.impl.CertificacaoService;
import br.com.certifai.service.impl.ProvaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvaServiceTest {

    @Mock
    private ProvaRepository provaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CertificacaoRepository certificacaoRepository;
    @Mock
    private QuestaoRepository questaoRepository;
    @Mock
    private CertificacaoService certificacaoService;

    @InjectMocks
    private ProvaService provaService;

    private Prova prova;
    private Usuario usuario;
    private Certificacao certificacao;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).name("Usuário Teste").build();
        certificacao = Certificacao.builder().id(1L).nome("Certificação Teste").questoes(new ArrayList<>()).build();
        prova = Prova.builder()
                .id(1L)
                .nome("Prova Teste")
                .usuario(usuario)
                .certificacao(certificacao)
                .status(Status.PENDENTE)
                .pontuacao(0)
                .build();
    }

    @Test
    @DisplayName("Deve criar uma prova com status PENDENTE e pontuação 0")
    void create_deveRetornarProvaSalvaComStatusPendente() {
        Prova provaParaCriar = Prova.builder().build();
        when(provaRepository.save(any(Prova.class))).thenAnswer(invocation -> {
            Prova p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        Prova provaSalva = provaService.create(provaParaCriar);

        assertNotNull(provaSalva);
        assertNotNull(provaSalva.getId());
        assertEquals(Status.PENDENTE, provaSalva.getStatus());
        assertEquals(0, provaSalva.getPontuacao());
        verify(provaRepository, times(1)).save(provaParaCriar);
    }

    @Test
    @DisplayName("Deve encontrar uma prova pelo ID existente")
    void findById_quandoIdExistente_deveRetornarOptionalComProva() {
        when(provaRepository.findById(1L)).thenReturn(Optional.of(prova));

        Optional<Prova> resultado = provaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(prova, resultado.get());
        verify(provaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar por ID inexistente")
    void findById_quandoIdNaoExistente_deveRetornarOptionalVazio() {
        when(provaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Prova> resultado = provaService.findById(99L);

        assertFalse(resultado.isPresent());
        verify(provaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve retornar todas as provas existentes")
    void findAll_deveRetornarListaDeProvas() {
        when(provaRepository.findAll()).thenReturn(Collections.singletonList(prova));

        List<Prova> provas = provaService.findAll();

        assertNotNull(provas);
        assertEquals(1, provas.size());
        verify(provaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia quando não há provas cadastradas")
    void findAll_quandoNenhumaProvaExiste_deveRetornarListaVazia() {
        when(provaRepository.findAll()).thenReturn(Collections.emptyList());

        List<Prova> provas = provaService.findAll();

        assertNotNull(provas);
        assertTrue(provas.isEmpty());
        verify(provaRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Deve encontrar provas pelo ID do usuário")
    void findByUsuarioId_deveRetornarListaDeProvas() {
        when(provaRepository.findByUsuarioId(1L)).thenReturn(Collections.singletonList(prova));

        List<Prova> provas = provaService.findByUsuarioId(1L);

        assertNotNull(provas);
        assertEquals(1, provas.size());
        verify(provaRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    @DisplayName("Deve encontrar provas pelo ID da certificação")
    void findByCertificacaoId_deveRetornarListaDeProvas() {
        when(provaRepository.findByCertificacaoId(1L)).thenReturn(Collections.singletonList(prova));

        List<Prova> provas = provaService.findByCertificacaoId(1L);

        assertNotNull(provas);
        assertEquals(1, provas.size());
        verify(provaRepository, times(1)).findByCertificacaoId(1L);
    }

    @Test
    @DisplayName("Deve atualizar uma prova existente")
    void update_quandoProvaExiste_deveAtualizarERetornarProva() {
        Prova provaDetalhes = Prova.builder().tempo(60L).build();
        when(provaRepository.findById(1L)).thenReturn(Optional.of(prova));
        when(provaRepository.save(any(Prova.class))).thenReturn(prova);

        prova.setTempo(60L);
        Prova provaAtualizada = provaService.update(1L, provaDetalhes);

        assertNotNull(provaAtualizada);
        assertEquals(60, provaAtualizada.getTempo());
        verify(provaRepository, times(1)).findById(1L);
        verify(provaRepository, times(1)).save(prova);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar prova inexistente")
    void update_quandoProvaNaoExiste_deveLancarEntidadeNaoEncontradaException() {
        when(provaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> provaService.update(99L, new Prova()));
        verify(provaRepository, times(1)).findById(99L);
        verify(provaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar uma prova existente")
    void delete_quandoProvaExiste_deveChamarDeleteById() {
        when(provaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(provaRepository).deleteById(1L);

        provaService.delete(1L);

        verify(provaRepository, times(1)).existsById(1L);
        verify(provaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar prova inexistente")
    void delete_quandoProvaNaoExiste_deveLancarEntidadeNaoEncontradaException() {
        when(provaRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> provaService.delete(99L));
        verify(provaRepository, times(1)).existsById(99L);
        verify(provaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve montar uma prova personalizada com sucesso")
    void montarProvaPersonalizada_comRequestValido_deveCriarERetornarProva() {
        MontarProvaRequest request = MontarProvaRequest.builder()
                .usuarioId(1L)
                .nome("Prova Personalizada")
                .certificacaoId(1L)
                .dificuldadeQuestoes(Dificuldades.BASICO)
                .numeroDeQuestoes(1)
                .comTempo(false)
                .build();
        List<Questao> questoes = Collections.singletonList(new Questao());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(certificacaoRepository.findById(1L)).thenReturn(Optional.of(certificacao));
        when(questaoRepository.findRandomQuestoesByDificuldade(Dificuldades.BASICO.name(), 1)).thenReturn(questoes);
        when(provaRepository.save(any(Prova.class))).thenReturn(prova);

        Prova novaProva = provaService.montarProvaPersonalizada(request);

        assertNotNull(novaProva);
        assertEquals(usuario, novaProva.getUsuario());
        assertEquals(certificacao, novaProva.getCertificacao());
        verify(certificacaoRepository, times(1)).save(certificacao);
        verify(provaRepository, times(1)).save(any(Prova.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se não houver questões suficientes")
    void montarProvaPersonalizada_semQuestoesSuficientes_deveLancarIllegalStateException() {
        MontarProvaRequest request = MontarProvaRequest.builder()
                .usuarioId(1L)
                .nome("Prova Personalizada sem Questões")
                .certificacaoId(1L)
                .dificuldadeQuestoes(Dificuldades.BASICO)
                .numeroDeQuestoes(5)
                .comTempo(false)
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(certificacaoRepository.findById(1L)).thenReturn(Optional.of(certificacao));
        when(questaoRepository.findRandomQuestoesByDificuldade(anyString(), anyInt())).thenReturn(new ArrayList<>());

        assertThrows(IllegalStateException.class, () -> provaService.montarProvaPersonalizada(request));
    }


    @Test
    @DisplayName("Deve iniciar uma prova com status PENDENTE")
    void iniciarProva_comProvaPendente_deveMudarStatusParaAndamento() {
        prova.setStatus(Status.PENDENTE);
        when(provaRepository.findById(1L)).thenReturn(Optional.of(prova));
        when(provaRepository.save(any(Prova.class))).thenAnswer(inv -> inv.getArgument(0));

        Prova provaIniciada = provaService.iniciarProva(1L);

        assertEquals(Status.ANDAMENTO, provaIniciada.getStatus());
        verify(provaRepository, times(1)).findById(1L);
        verify(provaRepository, times(1)).save(prova);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar iniciar prova que não está PENDENTE")
    void iniciarProva_comProvaNaoPendente_deveLancarIllegalStateException() {
        prova.setStatus(Status.CONCLUIDA);
        when(provaRepository.findById(1L)).thenReturn(Optional.of(prova));

        assertThrows(IllegalStateException.class, () -> provaService.iniciarProva(1L));
        verify(provaRepository, times(1)).findById(1L);
        verify(provaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve finalizar prova em ANDAMENTO, calcular pontuação e mudar status")
    void finalizarProva_comProvaEmAndamento_deveCalcularPontuacaoEMudarStatus() {
        prova.setStatus(Status.ANDAMENTO);
        Set<Resposta> respostas = new HashSet<>(Arrays.asList(
                Resposta.builder().acertou(true).build(),
                Resposta.builder().acertou(false).build()
        ));
        prova.setRespostas(respostas);

        when(provaRepository.findById(1L)).thenReturn(Optional.of(prova));
        when(provaRepository.save(any(Prova.class))).thenAnswer(inv -> inv.getArgument(0));

        Prova provaFinalizada = provaService.finalizarProva(1L);

        assertEquals(Status.CONCLUIDA, provaFinalizada.getStatus());
        assertEquals(1, provaFinalizada.getPontuacao());
        verify(provaRepository, times(1)).findById(1L);
        verify(provaRepository, times(1)).save(prova);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar finalizar prova que não está em ANDAMENTO")
    void finalizarProva_comProvaNaoEmAndamento_deveLancarIllegalStateException() {
        prova.setStatus(Status.PENDENTE);
        when(provaRepository.findById(1L)).thenReturn(Optional.of(prova));

        assertThrows(IllegalStateException.class, () -> provaService.finalizarProva(1L));
        verify(provaRepository, times(1)).findById(1L);
        verify(provaRepository, never()).save(any());
    }
}