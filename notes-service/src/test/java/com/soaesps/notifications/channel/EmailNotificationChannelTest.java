package com.soaesps.notifications.channel;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailSender Integration Test with Embedded GreenMail SMTP Server")
class EmailNotificationChannelTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    private EmailNotificationChannel emailSender;

    @BeforeEach
    void setUp() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(greenMail.getSmtp().getPort());

        emailSender = new EmailNotificationChannel(javaMailSender);
        ReflectionTestUtils.setField(emailSender, "from", "test-from@example.com");
    }

    @Test
    @DisplayName("Should successfully route and deliver pre-compiled HTML layouts to multi-endpoint BCC recipients")
    void testSendThroughGreenMail() throws MessagingException, IOException {
        // Arrange: Setup data contracts matching our modernized OutboundRoutingEnvelope design
        Long userId = 777L;
        String toEmail = "alekssergeevich1985@gmail.com";
        String subject = "Unit test with GreenMail";
        String text = "Hello from the embedded server!";

        // Pack values into the unified routing envelope container
        OutboundRoutingEnvelope envelope = new OutboundRoutingEnvelope(
                userId,
                "EMAIL",
                List.of(toEmail), // Targets destinations collection array
                List.of(Map.of()),
                subject,
                text
        );

        // Act: Fire the message downstream through the non-blocking Scheduler-isolated channel pipeline
        boolean isSent = emailSender.send(envelope);

        // Assert Step 1: Ensure execution pipeline returned an explicit success status token
        assertTrue(isSent, "The channel execution sequence should return a true status signal");

        // Assert Step 2: The email is delivered, check subject and content headers via GreenMail registry
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        MimeMessage received = receivedMessages[0];
        assertEquals(subject, received.getSubject());

        String cleanText = "";
        if (received.getContent() instanceof MimeMultipart) {
            MimeMultipart mp = (MimeMultipart) received.getContent();
            BodyPart bp = mp.getBodyPart(0);

            // Handle nested multipart segments (typical for rich text/HTML Thymeleaf responses)
            if (bp.getContent() instanceof MimeMultipart) {
                MimeMultipart innerMp = (MimeMultipart) bp.getContent();
                cleanText = innerMp.getBodyPart(0).getContent().toString();
            } else {
                cleanText = bp.getContent().toString();
            }
        } else {
            cleanText = received.getContent().toString();
        }

        // Verify that the actual layout text content contains our expected text body
        assertTrue(cleanText.contains(text), "The email text does not contain the expected string template text");

        // Assert Step 3: Validate mailboxes and individual IMAP inbox counts for the BCC recipient
        var greenMailUser = greenMail.getManagers().getUserManager().getUserByEmail(toEmail);
        assertNotNull(greenMailUser, "The user should be registered in the embedded mail server store");

        // Access the INBOX IMAP folder for a specific BCC user
        try {
            MailFolder inbox = greenMail.getManagers()
                    .getImapHostManager()
                    .getFolder(greenMailUser, "INBOX");

            assertNotNull(inbox, "The INBOX folder was not found for the user");

            // Extract real metrics straight from the mailbox folder store index bounds
            int messageCount = inbox.getMessageCount();
            assertEquals(1, messageCount, "The BCC recipient's mailbox should contain exactly 1 email");
        } catch (Exception e) {
            fail("Failed to read the IMAP INBOX folder: " + e.getMessage());
        }
    }
}