package com.gomes.photographer_manager.config.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Redefinição de senha");
        message.setText("Clique no link para redefinir sua senha: " + resetLink
                + "\n\nEste link expira em 1 hora.");
        mailSender.send(message);
    }

    public void sendDownloadEmail(String toEmail, String buyerName, String downloadLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Suas fotos estão prontas! - Framelio");
        message.setText(
                "Olá, " + buyerName + "!\n\n"
                        + "Seu pagamento foi confirmado. Acesse o link abaixo para baixar suas fotos:\n\n"
                        + downloadLink + "\n\n"
                        + "O link é válido por 72 horas e pode ser usado até 3 vezes.\n\n"
                        + "Obrigado por comprar no Framelio!"
        );
        mailSender.send(message);
    }
}