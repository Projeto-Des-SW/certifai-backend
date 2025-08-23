package br.com.certifai.controllers;

import br.com.certifai.dto.QuestaoDTO;
import br.com.certifai.enums.Categorias;
import br.com.certifai.enums.Dificuldades;
import br.com.certifai.enums.Roles;
import br.com.certifai.model.Certificacao;
import br.com.certifai.model.Questao;
import br.com.certifai.model.Usuario;
import br.com.certifai.repository.AlternativaRepository;
import br.com.certifai.repository.CertificacaoRepository;
import br.com.certifai.repository.QuestaoRepository;
import br.com.certifai.repository.UsuarioRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class QuestaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private CertificacaoRepository certificacaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IAuthService authService;

    private String adminToken;
    private Certificacao certificacaoSalva;
    private Questao questaoSalva;

    @BeforeEach
    void setUp() {
        alternativaRepository.deleteAll();
        questaoRepository.deleteAll();
        certificacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario admin = Usuario.builder()
                .name("Admin Teste Questao")
                .email("admin.questao@email.com")
                .password(passwordEncoder.encode("senha123"))
                .role(Roles.ADMIN)
                .ativo(true)
                .build();
        usuarioRepository.save(admin);
        adminToken = "Bearer " + authService.gerarToken(admin);

        Certificacao certificacao = Certificacao.builder().nome("Certificação Java").dificuldade(Dificuldades.INTERMEDIARIO).build();
        certificacaoSalva = certificacaoRepository.save(certificacao);

        Questao questao = Questao.builder()
                .enunciado("O que é JVM?")
                .categoria(Categorias.DEV)
                .dificuldade(Dificuldades.BASICO)
                .certificacoes(List.of(certificacaoSalva))
                .build();
        questaoSalva = questaoRepository.save(questao);
    }

    @Test
    @DisplayName("Deve criar uma questão com sucesso e retornar status 201")
    void deveCriarQuestaoComSucesso() throws Exception {
        QuestaoDTO novaQuestaoDTO = QuestaoDTO.builder()
                .enunciado("O que é Spring Boot?")
                .categoria(Categorias.DEV)
                .dificuldade(Dificuldades.INTERMEDIARIO)
                .certificacaoIds(List.of(certificacaoSalva.getId()))
                .build();

        String jsonRequest = objectMapper.writeValueAsString(novaQuestaoDTO);

        mockMvc.perform(post("/questoes")
                        .with(csrf())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.enunciado").value("O que é Spring Boot?"))
                .andExpect(jsonPath("$.data.certificacaoIds[0]").value(certificacaoSalva.getId()));
    }

    @Test
    @DisplayName("Deve listar todas as questões e retornar status 200")
    void deveListarTodasAsQuestoes() throws Exception {
        mockMvc.perform(get("/questoes")
                        .with(csrf())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].enunciado").value("O que é JVM?"));
    }

    @Test
    @DisplayName("Deve buscar questão por ID com sucesso e retornar status 200")
    void deveBuscarQuestaoPorIdComSucesso() throws Exception {
        mockMvc.perform(get("/questoes/{id}", questaoSalva.getId())
                        .with(csrf())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(questaoSalva.getId()))
                .andExpect(jsonPath("$.data.enunciado").value(questaoSalva.getEnunciado()));
    }

    @Test
    @DisplayName("Deve atualizar uma questão com sucesso e retornar status 200")
    void deveAtualizarQuestaoComSucesso() throws Exception {
        QuestaoDTO questaoAtualizadaDTO = QuestaoDTO.builder()
                .enunciado("Qual a função da JVM?")
                .categoria(Categorias.DEV)
                .dificuldade(Dificuldades.AVANCADO)
                .certificacaoIds(List.of(certificacaoSalva.getId()))
                .build();

        String jsonRequest = objectMapper.writeValueAsString(questaoAtualizadaDTO);

        mockMvc.perform(put("/questoes/{id}", questaoSalva.getId())
                        .with(csrf())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Questão atualizada com sucesso."))
                .andExpect(jsonPath("$.data.enunciado").value("Qual a função da JVM?"))
                .andExpect(jsonPath("$.data.dificuldade").value("AVANCADO"));
    }

    @Test
    @DisplayName("Deve remover uma questão com sucesso e retornar status 200")
    void deveRemoverQuestaoComSucesso() throws Exception {
        mockMvc.perform(delete("/questoes/{id}", questaoSalva.getId())
                        .with(csrf())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Questão removida com sucesso."));
    }

    @Test
    @DisplayName("Deve retornar status 404 ao tentar criar questão com certificação inexistente")
    void deveRetornarNotFoundAoCriarComCertificacaoInexistente() throws Exception {
        QuestaoDTO novaQuestaoDTO = QuestaoDTO.builder()
                .enunciado("Teste")
                .categoria(Categorias.INFRA)
                .dificuldade(Dificuldades.BASICO)
                .certificacaoIds(List.of(999L))
                .build();

        String jsonRequest = objectMapper.writeValueAsString(novaQuestaoDTO);

        mockMvc.perform(post("/questoes")
                        .with(csrf())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }
}
