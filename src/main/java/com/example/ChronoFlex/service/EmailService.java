package com.example.ChronoFlex.service;

import com.example.ChronoFlex.model.EmailLog;
import com.example.ChronoFlex.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;


    @Async
    public void sendEmailFromTemplate(String templateFileName, String toEmail, Map<String, String> values) throws IOException {

        ClassPathResource resource = new ClassPathResource("templates/email/" + templateFileName);
        String body = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Replace placeholders {{KEY}}
        for (Map.Entry<String, String> entry : values.entrySet()) {
            body = body.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        boolean isHtml = templateFileName.endsWith(".html") || templateFileName.endsWith(".htm");

        EmailLog log = new EmailLog();
        log.setRecipientEmail(toEmail);
        log.setSubject("Chrono Flex Notification");
        log.setBody(body);
        log.setTimestamp(LocalDateTime.now());

        try {
            if (isHtml) {
                // ===========================
                //      SEND HTML EMAIL
                // ===========================
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

                helper.setTo(toEmail);
                helper.setSubject("Chrono Flex Notification");
                helper.setText(body, true); // <-- true = HTML

                mailSender.send(mimeMessage);
                log.setStatus("SENT_HTML");

            } else {
                // ===========================
                //   SEND PLAIN TEXT EMAIL
                // ===========================
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Chrono Flex Notification");
                message.setText(body);

                mailSender.send(message);
                log.setStatus("SENT_TEXT");
            }

        } catch (MessagingException e) {
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());

        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());
        }

        emailLogRepository.save(log);
    }



    // ============================================================
    // ✅ New Methods for Admin Registration Email Logs
    // ============================================================

    /**
     * Logs when OTP is sent to the college during admin registration.
     */
    public void logCollegeOtpEmail(String collegeEmail, String collegeCode, String otp) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(collegeEmail);
        log.setSubject("Admin Registration - College OTP Sent");
        log.setBody("OTP for verifying college code " + collegeCode + " is: " + otp);
        log.setStatus("LOGGED");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    /**
     * Logs when OTP is sent to the admin after college verification.
     */
    public void logAdminOtpEmail(String adminEmail, String adminName, String otp) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(adminEmail);
        log.setSubject("Admin Registration - Admin OTP Sent");
        log.setBody("OTP for admin " + adminName + " is: " + otp);
        log.setStatus("LOGGED");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    /**
     * Logs final admin registration success.
     */
    public void logAdminRegistrationSuccess(String adminEmail, String adminName, String collegeCode) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(adminEmail);
        log.setSubject("Admin Registration - Completed");
        log.setBody("Admin '" + adminName + "' successfully registered under college code: " + collegeCode);
        log.setStatus("COMPLETED");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    /**
     * Logs failed admin registration attempt.
     */
    public void logAdminRegistrationFailure(String adminEmail, String reason) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(adminEmail);
        log.setSubject("Admin Registration - Failed");
        log.setBody("Admin registration failed due to: " + reason);
        log.setStatus("FAILED");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    // ============================================================
    // Generic email activity logger
    // ============================================================
    public void logEmailActivity(String recipient, String subject, String body) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(recipient);
        log.setSubject(subject);
        log.setBody(body);
        log.setStatus("INFO");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    // ============================================================
    // Password Reset Logging
    // ============================================================

    // Log OTP sent for password reset
    public void logPasswordResetOtp(String recipient, String otp) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(recipient);
        log.setSubject("Admin Password Reset OTP Sent");
        log.setBody("Password reset OTP sent to admin. OTP: " + otp);
        log.setStatus("SENT");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    // Log successful password reset
    public void logPasswordResetSuccess(String recipient) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(recipient);
        log.setSubject("Admin Password Reset Successful");
        log.setBody("Admin password reset completed successfully.");
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }

    // Log failed password reset attempt
    public void logPasswordResetFailure(String recipient, String errorMessage) {
        EmailLog log = new EmailLog();
        log.setRecipientEmail(recipient);
        log.setSubject("Admin Password Reset Failed");
        log.setBody("Password reset failed. Reason: " + errorMessage);
        log.setStatus("FAILED");
        log.setErrorMessage(errorMessage);
        log.setTimestamp(LocalDateTime.now());
        emailLogRepository.save(log);
    }
}
