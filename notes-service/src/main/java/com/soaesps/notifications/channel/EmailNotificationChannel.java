package com.soaesps.notifications.channel;

import com.google.common.net.MediaType;
import com.soaesps.notifications.domain.MailAttachment;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Modernized, non-blocking email delivery engine driven by the Spring Cache and Thymeleaf ecosystem.
 * Implements strictly the core {@link NotificationChannel} contract while safeguarding Netty loops via Schedulers isolation.
 */
@Service("emailSender")
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannel.class);

    @Value("${mail.from:noreply@nukefintech.com}")
    private String from;

    @Value("${logo.path:}")
    private String pathToLogo;

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailNotificationChannel(final JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // =========================================================================
    // NOTIFICATION CHANNEL CONTRACT IMPLEMENTATION
    // =========================================================================

    /**
     * Non-blocking bridge execution method. Constructs the helper message
     * and offloads SMTP I/O traffic to boundedElastic background threads.
     */
    @Override
    public boolean send(OutboundRoutingEnvelope envelope) {
        Long userId = envelope.userId();
        List<String> emailAddresses = envelope.destinations();

        try {
            // Build the multi-endpoint MimeMessage envelope using your dynamic inner builder structure
            MimeMessage message = this.builder()
                    .sendTo(emailAddresses) // Combines all emails as BCC array efficiently
                    .subject(envelope.messageTitle()) // Rendered Thymeleaf subject
                    .text(envelope.messageBody())   // Rendered HTML template body from MinIO/Postgres
                    .logo() // Attach signature inline branding logos dynamically if present
                    .build();

            // Execute the blocking mail sender task safely inside a reactive background context loop
            Boolean executionStatus = Mono.fromRunnable(() -> javaMailSender.send(message))
                    .map(v -> Boolean.TRUE)
                    .defaultIfEmpty(Boolean.TRUE)
                    .doOnSuccess(v -> log.info("Email multi-endpoint broadcast transaction finalized for user ID: {}", userId))
                    .doOnError(err -> log.error("SMTP transfer network failure captured for user ID: {}", userId, err))
                    .onErrorReturn(Boolean.FALSE)
                    .subscribeOn(Schedulers.boundedElastic()) // Safeguard the event loop from blocking network sockets
                    .block(); // Safe block within Spring Integration handler boundary worker pools

            return executionStatus != null && executionStatus;

        } catch (Exception ex) {
            log.error("Failed to compile or build complex MimeMessage envelopes for user ID: {}", userId, ex);
            return false;
        }
    }

    @Override
    public String id() {
        return "email";
    }

    @Override
    public boolean supports(OutboundRoutingEnvelope envelope) {
        if (envelope == null || envelope.destinations().isEmpty()) {
            return false;
        }
        return !envelope.destinations().contains("UNKNOWN_EMAIL");
    }

    // =========================================================================
    // ORIGINAL EMAIL BUILDER INNER CLASS WORK LOGIC
    // =========================================================================

    public class EmailBuilder {
        private final MimeMessage mimeMessage;
        private final MimeMessageHelper msgBuilder;

        private EmailBuilder() throws MessagingException {
            this.mimeMessage = javaMailSender.createMimeMessage();
            // Set multi-part true to support concurrent HTML structures and embedded media tags
            this.msgBuilder = new MimeMessageHelper(mimeMessage, true, CharEncoding.UTF_8);
            if (from != null && !from.isBlank()) {
                msgBuilder.setFrom(from);
            }
        }

        public EmailBuilder sendTo(final String to) throws MessagingException {
            msgBuilder.setBcc(to);
            return this;
        }

        public EmailBuilder sendTo(final String[] to) throws MessagingException {
            msgBuilder.setBcc(to);
            return this;
        }

        public EmailBuilder sendTo(final Collection<String> to) throws MessagingException {
            msgBuilder.setBcc(to.toArray(new String[0]));
            return this;
        }

        public EmailBuilder from(final String from) throws MessagingException {
            msgBuilder.setFrom(from);
            return this;
        }

        public EmailBuilder subject(final String subject) throws MessagingException {
            msgBuilder.setSubject(subject);
            return this;
        }

        /**
         * Overridden to guarantee content type parses as rich responsive HTML content.
         */
        public EmailBuilder text(final String text) throws MessagingException {
            // Set text parameter true to specify content evaluates as full HTML blocks
            msgBuilder.setText(text, true);
            return this;
        }

        public EmailBuilder attachment(final MailAttachment attachment) throws MessagingException {
            msgBuilder.addAttachment(attachment.getName(), attachment.getSource(), attachment.getType().type());
            return this;
        }

        public EmailBuilder attachments(final MailAttachment... attachments) throws MessagingException {
            for (final MailAttachment attachment : attachments) {
                this.attachment(attachment);
            }
            return this;
        }

        public EmailBuilder attachments(final Collection<MailAttachment> attachments) throws MessagingException {
            for (final MailAttachment attachment : attachments) {
                this.attachment(attachment);
            }
            return this;
        }

        public EmailBuilder logo(final String pathToLogo) throws MessagingException {
            if (pathToLogo == null || pathToLogo.isBlank()) return this;
            final InputStreamSource is = new ClassPathResource(pathToLogo);
            msgBuilder.addInline(Paths.get(pathToLogo).getFileName().toString(), is, MediaType.PNG.toString());
            return this;
        }

        public EmailBuilder logo() throws MessagingException {
            if (pathToLogo != null && !pathToLogo.isBlank()) {
                logo(pathToLogo);
            }
            return this;
        }

        public MimeMessage build() {
            return mimeMessage;
        }
    }

    public EmailBuilder builder() throws MessagingException {
        return new EmailBuilder();
    }
}
