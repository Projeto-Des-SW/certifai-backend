package br.com.certifai.controllers;

import br.com.certifai.enums.Dificuldades;
import br.com.certifai.enums.Roles;
import br.com.certifai.enums.Status;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Prova;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.repository.ProvaRepository;
import br.com.certifai.repository.UsuarioRepository;
import br.com.certifai.requests.MontarProvaRequest;
import br.com.certifai.service.interfaces.IAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProvaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProvaRepository provaRepository;
    @Autowired
    private CertificacaoRepository certificacaoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IAuthService authService;

    private String userToken;
    private Usuario usuario;
    private Certificacao certificacao;
    private Prova prova;

    @BeforeEach
    void setUp() {
        provaRepository.deleteAll();
        certificacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setName("Usuário Comum");
        usuario.setEmail("usuario.comum@email.com");
        usuario.setPassword(passwordEncoder.encode("senha456"));
        usuario.setRole(Roles.ESTUDANTE);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        userToken = "Bearer " + authService.gerarToken(usuario);

        certificacao = Certificacao.builder()
                .nome("Fundamentos de Docker")
                .dificuldade(Dificuldades.BASICO)
                .build();
        certificacaoRepository.save(certificacao);

        prova = Prova.builder()
                .nome("Prova de Teste")
                .usuario(usuario)
                .certificacao(certificacao)
                .status(Status.PENDENTE)
                .pontuacao(0)
                .build();
        provaRepository.save(prova);
    }

    @Test
    @DisplayName("Deve montar uma prova com sucesso")
    void deveMontarProvaComSucesso() throws Exception {
        MontarProvaRequest request = MontarProvaRequest.builder()
                .nome("Montar Prova")
                .usuarioId(usuario.getId())
                .certificacaoId(certificacao.getId())
                .numeroDeQuestoes(1)
                .dificuldadeQuestoes(Dificuldades.AVANCADO)
                .comTempo(false)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/provas/montar-personalizada")
                        .with(csrf())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDENTE"))
                .andExpect(jsonPath("$.data.usuarioId").value(usuario.getId()));
    }

    @Test
    @DisplayName("Deve listar todas as provas")
    void deveListarTodasAsProvas() throws Exception {
        mockMvc.perform(get("/provas")
                        .with(csrf())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(prova.getId()));
    }

    @Test
    @DisplayName("Deve buscar prova por ID com sucesso")
    void deveBuscarProvaPorIdComSucesso() throws Exception {
        mockMvc.perform(get("/provas/{id}", prova.getId())
                        .with(csrf())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(prova.getId()));
    }

    @Test
    @DisplayName("Deve iniciar uma prova com sucesso")
    void deveIniciarProvaComSucesso() throws Exception {
        mockMvc.perform(post("/provas/{id}/iniciar", prova.getId())
                        .with(csrf())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Prova iniciada com sucesso."))
                .andExpect(jsonPath("$.data.status").value("ANDAMENTO"));
    }

    @Test
    @DisplayName("Deve finalizar uma prova com sucesso")
    void deveFinalizarProvaComSucesso() throws Exception {
        prova.setStatus(Status.ANDAMENTO);
        provaRepository.save(prova);

        mockMvc.perform(post("/provas/{id}/finalizar", prova.getId())
                        .with(csrf())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Prova finalizada com sucesso."))
                .andExpect(jsonPath("$.data.status").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("Deve remover uma prova com sucesso")
    void deveRemoverProvaComSucesso() throws Exception {
        mockMvc.perform(delete("/provas/{id}", prova.getId())
                        .with(csrf())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Prova removida com sucesso."));
    }
}