package br.com.certifai.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.certifai.exception.EntidadeNaoEncontradaException;
import br.com.certifai.mappers.IItemChecklistMapper;
import br.com.certifai.model.Checklist;
import br.com.certifai.model.Evento;
import br.com.certifai.model.ItemChecklist;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.ChecklistRepository;
import br.com.certifai.repository.EventoRepository;
import br.com.certifai.repository.ItemChecklistRepository;
import br.com.certifai.repository.UsuarioRepository;
import br.com.certifai.service.interfaces.IItemChecklistService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemChecklistService implements IItemChecklistService {
    private final ItemChecklistRepository itemRepository;
    private final ChecklistRepository checklistRepository;
    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;

    @Override
    @Transactional
    public ItemChecklist createItem(ItemChecklist item) {
        Checklist checklist = checklistRepository.findById(item.getChecklist().getId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Checklist não encontrado"));

        Usuario usuario = usuarioRepository.findById(item.getUsuario().getId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));

        item.setChecklist(checklist);
        item.setUsuario(usuario);

        if (item.getEvento() != null && item.getEvento().getId() != null) {
            Evento evento = eventoRepository.findById(item.getEvento().getId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Evento não encontrado"));
            item.setEvento(evento);
        }

        aplicarRegraConclusao(item);
        return itemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemChecklist getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Item do checklist não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemChecklist> getItemsByChecklistId(Long checklistId) {
        return itemRepository.findByChecklistId(checklistId);
    }

    @Override
    @Transactional
    public ItemChecklist updateItem(Long id, ItemChecklist itemDetails) {
        ItemChecklist item = getItemById(id);

        item.setDescricao(itemDetails.getDescricao());
        item.setConcluido(itemDetails.isConcluido());

        aplicarRegraConclusao(item);

        ItemChecklist atualizado = itemRepository.save(item);
        return atualizado;
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        ItemChecklist item = getItemById(id);
        itemRepository.delete(item);
    }

    private void aplicarRegraConclusao(ItemChecklist item) {
        if (item.isConcluido() && item.getConclusao() == null) {
            item.setConclusao(LocalDateTime.now());
        } else if (!item.isConcluido()) {
            item.setConclusao(null);
        }
    }

    @Transactional
    public ItemChecklist vincularEvento(Long itemId, Long eventoId) {
        ItemChecklist item = getItemById(itemId);

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Evento não encontrado"));

        item.setEvento(evento);
        return itemRepository.save(item);
    }

    @Transactional
    public ItemChecklist desvincularEvento(Long itemId) {
        ItemChecklist item = getItemById(itemId);

        item.setEvento(null);
        return itemRepository.save(item);
    }

}
