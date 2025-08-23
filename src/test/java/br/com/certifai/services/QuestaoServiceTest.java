package br.com.certifai.services;

import br.com.certifai.enums.Categorias;
import br.com.certifai.enums.Dificuldades;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.model.Questao;
import br.com.certifai.repository.QuestaoRepository;
import br.com.certifai.service.impl.QuestaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @InjectMocks
    private QuestaoService questaoService;

    private Questao questao;

    @BeforeEach
    void setUp() {
        questao = Questao.builder()
                .id(1L)
                .enunciado("O que é SOLID?")
                .categoria(Categorias.DEV)
                .dificuldade(Dificuldades.INTERMEDIARIO)
                .build();
    }

    @Test
    @DisplayName("Deve criar uma questão com sucesso")
    void criarComSucesso() {
        Questao questaoSemId = Questao.builder()
                .enunciado("O que é SOLID?")
                .categoria(Categorias.DEV)
                .dificuldade(Dificuldades.INTERMEDIARIO)
                .build();

        when(questaoRepository.save(any(Questao.class))).thenReturn(questao);

        Questao questaoSalva = questaoService.create(questaoSemId);

        assertNotNull(questaoSalva);
        assertEquals(1L, questaoSalva.getId());
        assertEquals("O que é SOLID?", questaoSalva.getEnunciado());

        ArgumentCaptor<Questao> questaoArgumentCaptor = ArgumentCaptor.forClass(Questao.class);
        verify(questaoRepository).save(questaoArgumentCaptor.capture());
        assertNull(questaoArgumentCaptor.getValue().getId());
    }

    @Test
    @DisplayName("Deve encontrar questão por ID e retornar um Optional com valor")
    void findByIdQuandoexiste() {
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));

        Optional<Questao> resultado = questaoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(questao.getId(), resultado.get().getId());
    }

    @Test
    @DisplayName("Deve retornar um Optional vazio se a questão não for encontrada")
    void findByIdQuandoNaoExiste() {
        when(questaoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Questao> resultado = questaoService.findById(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar uma lista de todas as questões")
    void findAllQuandoHaQuestoes() {
        when(questaoRepository.findAll()).thenReturn(List.of(questao));

        List<Questao> resultado = questaoService.findAll();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("O que é SOLID?", resultado.get(0).getEnunciado());
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver questões")
    void findAllQuandoNaoHaQuestoes() {
        when(questaoRepository.findAll()).thenReturn(Collections.emptyList());

        List<Questao> resultado = questaoService.findAll();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve atualizar uma questão com sucesso")
    void updateQuandoQuestaoExiste() {
        Questao detalhesAtualizacao = Questao.builder()
                .enunciado("Quais são os 5 princípios do SOLID?")
                .dificuldade(Dificuldades.AVANCADO)
                .build();

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(questaoRepository.save(any(Questao.class))).thenReturn(questao);

        Questao questaoAtualizada = questaoService.update(1L, detalhesAtualizacao);

        assertNotNull(questaoAtualizada);
        assertEquals("Quais são os 5 princípios do SOLID?", questaoAtualizada.getEnunciado());
        assertEquals(Dificuldades.AVANCADO, questaoAtualizada.getDificuldade());
        verify(questaoRepository, times(1)).findById(1L);
        verify(questaoRepository, times(1)).save(any(Questao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar questão que não existe")
    void updateQuandoQuestaoNaoExiste() {
        when(questaoRepository.findById(99L)).thenReturn(Optional.empty());
        Questao detalhesAtualizacao = new Questao();

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            questaoService.update(99L, detalhesAtualizacao);
        });
        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    @DisplayName("Deve deletar uma questão com sucesso")
    void deleteQuandoQuestaoExiste() {
        when(questaoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(questaoRepository).deleteById(1L);

        assertDoesNotThrow(() -> questaoService.delete(1L));

        verify(questaoRepository, times(1)).existsById(1L);
        verify(questaoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar questão que não existe")
    void deleteQuandoQuestaoNaoExiste() {
        when(questaoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            questaoService.delete(99L);
        });
        verify(questaoRepository, never()).deleteById(anyLong());
    }
}
