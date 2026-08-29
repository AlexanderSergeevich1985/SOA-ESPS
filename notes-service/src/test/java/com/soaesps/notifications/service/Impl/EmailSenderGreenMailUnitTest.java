package com.soaesps.notifications.service.Impl;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EmailSenderGreenMailUnitTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        // It is better to get the port directly from the GreenMail configuration to avoid hardcoding
        javaMailSender.setPort(greenMail.getSmtp().getPort());

        emailSender = new EmailSender(javaMailSender);
        ReflectionTestUtils.setField(emailSender, "from", "test-from@example.com");
    }

    @Test
    void testSendThroughGreenMail() throws MessagingException, IOException {
        String toEmail = "alekssergeevich1985@gmail.com";
        String subject = "Unit test with GreenMail";
        String text = "Hello from the embedded server!";

        MimeMessage message = emailSender.builder()
                .sendTo(toEmail)
                .subject(subject)
                .text(text)
                .build();

        emailSender.send(message);

        // The email is delivered, subject and body are in place
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        MimeMessage received = receivedMessages[0];
        assertEquals(subject, received.getSubject());

        String cleanText = "";
        if (received.getContent() instanceof jakarta.mail.internet.MimeMultipart) {
            jakarta.mail.internet.MimeMultipart mp = (jakarta.mail.internet.MimeMultipart) received.getContent();
            // Get the first part of the multipart message
            jakarta.mail.BodyPart bp = mp.getBodyPart(0);

            // If the first part contains another multipart (this happens with mixed HTML+TEXT content)
            if (bp.getContent() instanceof jakarta.mail.internet.MimeMultipart) {
                jakarta.mail.internet.MimeMultipart innerMp = (jakarta.mail.internet.MimeMultipart) bp.getContent();
                cleanText = innerMp.getBodyPart(0).getContent().toString();
            } else {
                cleanText = bp.getContent().toString();
            }
        } else {
            cleanText = received.getContent().toString();
        }

        assertTrue(cleanText.contains(text), "The email text does not contain the expected string");

        MimeMessage[] delivered = greenMail.getReceivedMessagesForDomain(toEmail);
        var greenMailUser = greenMail.getManagers().getUserManager().getUserByEmail(toEmail);
        assertNotNull(greenMailUser, "The user should be registered in the mail server");

        // Access the INBOX IMAP folder for a specific BCC user
        try {
            com.icegreen.greenmail.store.MailFolder inbox = greenMail.getManagers()
                    .getImapHostManager()
                    .getFolder(greenMailUser, "INBOX");

            assertNotNull(inbox, "The INBOX folder was not found for the user");

            // Get the message count directly from the mailbox store
            int messageCount = inbox.getMessageCount();

            assertEquals(1, messageCount, "The BCC recipient's mailbox should contain exactly 1 email");
        } catch (Exception e) {
            fail("Failed to read the INBOX folder: " + e.getMessage());
        }
    }
}