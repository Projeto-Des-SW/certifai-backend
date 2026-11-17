package br.com.certifai.services;

import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.model.Checklist;
import br.com.certifai.model.Evento;
import br.com.certifai.model.ItemChecklist;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.ChecklistRepository;
import br.com.certifai.repository.EventoRepository;
import br.com.certifai.repository.ItemChecklistRepository;
import br.com.certifai.repository.UsuarioRepository;
import br.com.certifai.service.impl.ItemChecklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemChecklistServiceTest {

    @Mock
    private ItemChecklistRepository itemRepository;

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private ItemChecklistService itemService;

    private Checklist checklist;
    private Usuario usuario;
    private ItemChecklist item;

    @BeforeEach
    void setUp() {
        checklist = new Checklist();
        checklist.setId(1L);

        usuario = new Usuario();
        usuario.setId(2L);

        item = new ItemChecklist();
        item.setId(10L);
        item.setDescricao("Completar módulo de Redes");
        item.setConcluido(false);
        item.setChecklist(checklist);
        item.setUsuario(usuario);
    }

    @Test
    @DisplayName("Deve criar um item com sucesso vinculando checklist e usuário")
    void createItem_ComSucesso() {
        // Arrange
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(checklist));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(itemRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemChecklist salvo = itemService.createItem(item);

        // Assert
        assertNotNull(salvo);
        assertEquals("Completar módulo de Redes", salvo.getDescricao());
        assertEquals(checklist, salvo.getChecklist());
        assertEquals(usuario, salvo.getUsuario());
        assertNull(salvo.getConclusao());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException se checklist não existir")
    void createItem_ChecklistNaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(checklistRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> itemService.createItem(item)
        );
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar item por ID com sucesso")
    void getItemById_ComSucesso() {
        // Arrange
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        // Act
        ItemChecklist encontrado = itemService.getItemById(10L);

        // Assert
        assertNotNull(encontrado);
        assertEquals(10L, encontrado.getId());
    }

    @Test
    @DisplayName("Deve lançar EntidadeNaoEncontradaException ao buscar item inexistente")
    void getItemById_NaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> itemService.getItemById(999L)
        );
    }

    @Test
    @DisplayName("Deve listar itens de um checklist por ID")
    void getItemsByChecklistId_RetornaLista() {
        // Arrange
        when(itemRepository.findByChecklistId(1L)).thenReturn(List.of(item));

        // Act
        List<ItemChecklist> itens = itemService.getItemsByChecklistId(1L);

        // Assert
        assertFalse(itens.isEmpty());
        assertEquals(1, itens.size());
    }

    @Test
    @DisplayName("Deve atualizar item e preencher data de conclusão quando marcado como concluído")
    void updateItem_ComConclusao_ComSucesso() {
        // Arrange
        ItemChecklist detalhes = new ItemChecklist();
        detalhes.setDescricao("Descrição Atualizada");
        detalhes.setConcluido(true);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemChecklist atualizado = itemService.updateItem(10L, detalhes);

        // Assert
        assertNotNull(atualizado);
        assertEquals("Descrição Atualizada", atualizado.getDescricao());
        assertTrue(atualizado.isConcluido());
        assertNotNull(atualizado.getConclusao());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve deletar item existente com sucesso")
    void deleteItem_ComSucesso() {
        // Arrange
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        // Act
        itemService.deleteItem(10L);

        // Assert
        verify(itemRepository).delete(item);
    }

    @Test
    @DisplayName("Deve vincular item a um evento existente com sucesso")
    void vincularEvento_ComSucesso() {
        // Arrange
        Evento evento = new Evento();
        evento.setId(50L);
        evento.setTitulo("Simulado Geral");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(itemRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemChecklist resultado = itemService.vincularEvento(10L, 50L);

        // Assert
        assertNotNull(resultado);
        assertEquals(evento, resultado.getEvento());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve desvincular item de evento com sucesso")
    void desvincularEvento_ComSucesso() {
        // Arrange
        Evento evento = new Evento();
        evento.setId(50L);
        item.setEvento(evento);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(ItemChecklist.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemChecklist resultado = itemService.desvincularEvento(10L);

        // Assert
        assertNotNull(resultado);
        assertNull(resultado.getEvento());
        verify(itemRepository).save(item);
    }
}
