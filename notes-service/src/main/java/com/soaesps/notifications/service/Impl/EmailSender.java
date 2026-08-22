package com.soaesps.notifications.service.Impl;

import com.google.common.net.MediaType;
import com.soaesps.notifications.domain.MailAttachment;
import com.soaesps.notifications.service.SenderI;
import org.apache.commons.lang3.CharEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("emailSender")
public class EmailSender implements SenderI {
    private static final Logger logger;

    static {
        logger = Logger.getLogger(EmailSender.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Value("${mail.from}")
    private String from;

    @Value("${logo.path}")
    private String pathToLogo;

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailSender(final JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(final MimeMessage message) {
        try {
            javaMailSender.send(message);
            logger.log(Level.INFO, "[EmailSender/send]: E-Mail sent to User");
        } catch (final Exception ex) {
            logger.log(Level.SEVERE, "[EmailSender/send]: E-mail could not be sent to user, exception is: " + ex.getMessage(), ex);
        }
    }

    public class EmailBuilder {
        private final MimeMessage mimeMessage;
        private final MimeMessageHelper msgBuilder;

        private EmailBuilder() throws MessagingException {
            this.mimeMessage = javaMailSender.createMimeMessage();
            this.msgBuilder = new MimeMessageHelper(mimeMessage, true, CharEncoding.UTF_8);
            if (from != null) {
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

        public EmailBuilder text(final String text) throws MessagingException {
            msgBuilder.setText(text);
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
            final InputStreamSource is = new ClassPathResource(pathToLogo);
            msgBuilder.addInline(Paths.get(pathToLogo).getFileName().toString(), is, MediaType.PNG.toString());
            return this;
        }

        public EmailBuilder logo() throws MessagingException {
            if (pathToLogo != null) {
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