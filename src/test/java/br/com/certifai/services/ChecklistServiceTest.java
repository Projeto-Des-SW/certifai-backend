package br.com.certifai.services;

import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.mappers.IChecklistMapper;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Checklist;
import br.com.certifai.model.ItemChecklist;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.repository.ChecklistRepository;
import br.com.certifai.service.impl.AuthService;
import br.com.certifai.service.impl.ChecklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private CertificacaoRepository certificacaoRepository;

    @Mock
    private AuthService usuarioService;

    @Mock
    private IChecklistMapper checklistMapper;

    @InjectMocks
    private ChecklistService checklistService;

    private Checklist checklist;
    private Certificacao certificacao;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        certificacao = new Certificacao();
        certificacao.setId(1L);
        certificacao.setNome("AWS Solutions Architect");

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("estudante@certifai.com");

        checklist = new Checklist();
        checklist.setId(100L);
        checklist.setNome("Trilha de Estudos AWS");
        checklist.setDescricao("Passo a passo para certificação");
        checklist.setCertificacao(certificacao);
        checklist.setItensChecklist(new ArrayList<>());
    }

    @Test
    @DisplayName("Deve criar um checklist com sucesso associando usuário e certificação")
    void createChecklist_ComSucesso() {
        // Arrange
        when(certificacaoRepository.findById(1L)).thenReturn(Optional.of(certificacao));
        when(usuarioService.getPrincipal()).thenReturn(Optional.of(usuario));
        when(checklistRepository.save(any(Checklist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemChecklist item = new ItemChecklist();
        item.setDescricao("Revisar VPC");
        checklist.getItensChecklist().add(item);

        // Act
        Checklist salvo = checklistService.createChecklist(checklist);

        // Assert
        assertNotNull(salvo);
        assertEquals(certificacao, salvo.getCertificacao());
        assertEquals(usuario, salvo.getUsuario());
        assertEquals(checklist, item.getChecklist());
        verify(checklistRepository).save(checklist);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando certificação for nula")
    void createChecklist_SemCertificacao_DeveLancarExcecao() {
        // Arrange
        checklist.setCertificacao(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checklistService.createChecklist(checklist)
        );
        assertEquals("É obrigatório informar o ID da certificação.", exception.getMessage());
        verifyNoInteractions(checklistRepository);
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException quando certificação não existir")
    void createChecklist_CertificacaoNaoEncontrada_DeveLancarExcecao() {
        // Arrange
        when(certificacaoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> checklistService.createChecklist(checklist)
        );
        verify(checklistRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar checklist por ID com sucesso")
    void getChecklistById_Encontrado() {
        // Arrange
        when(checklistRepository.findById(100L)).thenReturn(Optional.of(checklist));

        // Act
        Optional<Checklist> resultado = checklistService.getChecklistById(100L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(100L, resultado.get().getId());
    }

    @Test
    @DisplayName("Deve retornar todos os checklists")
    void getAllChecklists_RetornaLista() {
        // Arrange
        when(checklistRepository.findAll()).thenReturn(List.of(checklist));

        // Act
        List<Checklist> lista = checklistService.getAllChecklists();

        // Assert
        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Deve atualizar checklist com sucesso")
    void updateChecklist_ComSucesso() {
        // Arrange
        Checklist novosDados = new Checklist();
        novosDados.setNome("Nome Atualizado");
        novosDados.setDescricao("Nova Descrição");
        novosDados.setCertificacao(certificacao);

        when(checklistRepository.findById(100L)).thenReturn(Optional.of(checklist));
        when(checklistRepository.save(any(Checklist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Checklist atualizado = checklistService.updateChecklist(100L, novosDados);

        // Assert
        assertNotNull(atualizado);
        assertEquals("Nome Atualizado", atualizado.getNome());
        assertEquals("Nova Descrição", atualizado.getDescricao());
        verify(checklistRepository).save(checklist);
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException ao atualizar checklist inexistente")
    void updateChecklist_NaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(checklistRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> checklistService.updateChecklist(999L, checklist)
        );
        verify(checklistRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar checklist existente com sucesso")
    void deleteChecklist_ComSucesso() {
        // Arrange
        when(checklistRepository.findById(100L)).thenReturn(Optional.of(checklist));

        // Act
        checklistService.deleteChecklist(100L);

        // Assert
        verify(checklistRepository).delete(checklist);
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar deletar checklist inexistente")
    void deleteChecklist_NaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(checklistRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> checklistService.deleteChecklist(999L)
        );
        verify(checklistRepository, never()).delete(any());
    }
}
