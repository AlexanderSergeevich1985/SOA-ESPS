package com.soaesps.notifications.service.Impl;

import com.google.common.net.MediaType;
import com.soaesps.notifications.domain.MailAttachment;
import com.soaesps.notifications.service.SenderI;
import org.apache.commons.lang3.CharEncoding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("emailSender")
public class EmailSender implements SenderI {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(EmailSender.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Value("${mail.from}")
    private String from;

    @Value("${logo.path}")
    private String pathToLogo;

    private final JavaMailSenderImpl javaMailSender;

    private final SpringTemplateEngine templateEngine;

    @Autowired
    public EmailSender(final JavaMailSenderImpl javaMailSender, final SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void send(final MimeMessage message) {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            javaMailSender.send(mimeMessage);
            logger.log(Level.INFO, "[EmailSender/send]: E-Mail sent to User ");
        } catch (final Exception ex) {
            logger.log(Level.INFO, "[EmailSender/send]: E-mail could not be sent to user, exception is: {}", ex.getMessage());
        }
    }

    public class EmailBuilder {
        private final MimeMessage mimeMessage;

        private final MimeMessageHelper msgBuilder;

        private EmailBuilder() throws MessagingException {
            this.mimeMessage = javaMailSender.createMimeMessage();
            this.msgBuilder = new MimeMessageHelper(mimeMessage, true, CharEncoding.UTF_8);
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
            msgBuilder.setBcc(to.toArray(new String[to.size()]));

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
            for (final MailAttachment attachment: attachments) {
                this.attachment(attachment);
            }

            return this;
        }

        public EmailBuilder attachments(final Collection<MailAttachment> attachments) throws MessagingException {
            for (final MailAttachment attachment: attachments) {
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
            logo(pathToLogo);

            return this;
        }

        public MimeMessage build() {
            return mimeMessage;
        }
    }

    static public EmailBuilder builder(final EmailSender emailSender) throws MessagingException {
        final EmailBuilder emailBuilder = emailSender.new EmailBuilder();

        return emailBuilder;
    }
}