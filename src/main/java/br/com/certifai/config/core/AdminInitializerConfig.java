package br.com.certifai.config.core;

import br.com.certifai.enums.Roles;
import br.com.certifai.model.Usuario;
import br.com.certifai.service.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminInitializerConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializerConfig.class);

    @Autowired
    private AuthService authService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name:ADMIN}")
    private String adminName;

    @Override
    public void run(String... args) {
        try {
            if (authService.getUsuarioByEmail(adminEmail).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setEmail(adminEmail);
                admin.setPassword(adminPassword);
                admin.setName(adminName);
                admin.setRole(Roles.ADMIN);
                admin.setEmailVerified(true);

                authService.createUser(admin);
                logger.info("Admin user created successfully");
            }
        } catch (Exception e) {
            logger.error("Falha ao inicializar usuário admin", e);
        }
    }
}
