package br.com.certifai.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class EmailService {

    private final SendGrid sendGrid;

    @Value("${sendgrid.sender-email:certifai.suporte@gmail.com}")
    private String fromEmail;

    @Value("${app.base-url:https://certifai-backend.onrender.com/certifai}")
    private String baseUrl;

    public EmailService(@Value("${sendgrid.api-key:SG.placeholder}") String sendGridApiKey) {
        this.sendGrid = new SendGrid(sendGridApiKey);
    }

    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "Verifique seu E-mail para Ativar sua Conta";
        String verificationLink = "https://certifai-front-ruby.vercel.app/auth/verify?token=" + token;

        String htmlContent = String.format("""
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
            <h2>Olá %s,</h2>
            <p>Obrigado por se cadastrar em nossa aplicação!</p>
            <p>Por favor, clique no botão abaixo para verificar seu endereço de e-mail e ativar sua conta:</p>
            <p style="margin: 20px 0;">
                <a href="%s" style="
                    padding: 12px 24px;
                    background-color: #003366;
                    color: white;
                    text-decoration: none;
                    border-radius: 6px;
                    display: inline-block;
                ">Verificar E-mail Agora</a>
            </p>
            <p><small>Este link é válido por 24 horas.</small></p>
            <hr/>
            <p style="font-size: 12px; color: #888;">
                Se você não solicitou este e-mail, pode ignorá-lo com segurança.<br/>
                Atenciosamente,<br/>Equipe CertifAI
            </p>
        </body>
        </html>
        """, username, verificationLink);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            log.info("E-mail de verificação processado para {}. Status Code: {}", toEmail, response.getStatusCode());
            if (response.getStatusCode() >= 400) {
                log.error("Erro SendGrid ao enviar verificação para {}: {}", toEmail, response.getBody());
            }
        } catch (IOException ex) {
            log.error("Exceção de I/O ao enviar e-mail de verificação via SendGrid para {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "Recuperação de Senha - CertifAI";
        String resetLink = "https://certifai-front-ruby.vercel.app/reset-password?token=" + token;

        String htmlContent = String.format("""
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
            <h2>Olá %s,</h2>
            <p>Recebemos uma solicitação para redefinir a senha da sua conta na <strong>CertifAI</strong>.</p>
            <p>Para criar uma nova senha, clique no botão abaixo:</p>
            <p style="margin: 20px 0;">
                <a href="%s" style="padding: 12px 24px; background-color: #f44336; color: white; text-decoration: none; border-radius: 6px;">Redefinir Senha</a>
            </p>
            <p><small>Este link é válido por 1 hora.</small></p>
            <hr/>
            <p style="font-size: 12px; color: #888;">
                Se você não solicitou este e-mail, pode ignorá-lo com segurança.<br/>
                Atenciosamente,<br/>Equipe CertifAI
            </p>
        </body>
        </html>
    """, username, resetLink);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            log.info("E-mail de recuperação de senha processado para {}. Status Code: {}", toEmail, response.getStatusCode());
            if (response.getStatusCode() >= 400) {
                log.error("Erro SendGrid ao enviar recuperação para {}: {}", toEmail, response.getBody());
            }
        } catch (IOException ex) {
            log.error("Exceção de I/O ao enviar e-mail de recuperação de senha via SendGrid para {}: {}", toEmail, ex.getMessage(), ex);
        }
    }
}
