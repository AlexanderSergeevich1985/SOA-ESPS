package com.soaesps.notifications.service;

import com.soaesps.notifications.dto.ContactRegistrationRequest;
import com.soaesps.notifications.dto.ContactResponseDto;
import com.soaesps.notifications.dto.UserContactsProfileSummary;
import com.soaesps.notifications.domain.reactive.*;
import com.soaesps.notifications.repository.reactive.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enterprise reactive service layer managing lifecycle boundaries (CRUD) for multi-channel user contacts.
 * Operates on top of Spring Data R2DBC repositories using fully non-blocking asynchronous pipelines.
 */
@Service
public class UserContactService {
    private static final Logger log = LoggerFactory.getLogger(UserContactService.class);

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final ReactiveSmsContactRepository smsContactRepository;
    private final ReactiveEmailContactRepository emailContactRepository;
    private final ReactiveTelegramContactRepository telegramContactRepository;
    private final ReactivePushContactRepository pushContactRepository;

    public UserContactService(ReactiveSmsContactRepository smsContactRepository,
                              ReactiveEmailContactRepository emailContactRepository,
                              ReactiveTelegramContactRepository telegramContactRepository,
                              ReactivePushContactRepository pushContactRepository) {
        this.smsContactRepository = smsContactRepository;
        this.emailContactRepository = emailContactRepository;
        this.telegramContactRepository = telegramContactRepository;
        this.pushContactRepository = pushContactRepository;
    }

    // =========================================================================
    // CREATE OPERATOR (Registration Hub)
    // =========================================================================

    @Transactional
    public Mono<ContactResponseDto> processContactRegistration(Long userId, ContactRegistrationRequest request) {
        return Mono.defer(() -> {
            String channelType = request.type().toUpperCase();
            log.info("[Service-Create] Registering contact route '{}' for userId: {}", channelType, userId);

            return switch (channelType) {
                case "SMS" -> {
                    String phone = request.meta().get("phoneNumber");
                    validateRegexp(phone, PHONE_PATTERN, "Malformed E.164 phone layout configuration format");
                    var smsRow = new SmsContactRow(null, userId, "SMS", true, request.primary(), LocalDateTime.now(), phone);
                    yield smsContactRepository.save(smsRow)
                            .map(saved -> new ContactResponseDto(saved.id(), "SMS", "SUCCESS", "Phone routing registration finalized."));
                }
                case "EMAIL" -> {
                    String email = request.meta().get("emailAddress");
                    validateRegexp(email, EMAIL_PATTERN, "Invalid structural localized email parameters provided");
                    var emailRow = new EmailContactRow(null, userId, "EMAIL", true, request.primary(), LocalDateTime.now(), email);
                    yield emailContactRepository.save(emailRow)
                            .map(saved -> new ContactResponseDto(saved.id(), "EMAIL", "SUCCESS", "Email identity routing synchronized."));
                }
                case "TELEGRAM" -> {
                    String chatId = request.meta().get("telegramChatId");
                    String username = request.meta().get("telegramUsername");
                    if (chatId == null || chatId.isBlank()) {
                        yield Mono.error(new IllegalArgumentException("Missing 'telegramChatId' parameter inside meta bag"));
                    }
                    String cleanUsername = (username != null) ? username : "";
                    var tgRow = new TelegramContactRow(null, userId, "TELEGRAM", true, request.primary(), LocalDateTime.now(), chatId, cleanUsername);
                    yield telegramContactRepository.save(tgRow)
                            .map(saved -> new ContactResponseDto(saved.id(), "TELEGRAM", "SUCCESS", "Telegram authentication tunnel established."));
                }
                case "PUSH" -> {
                    String token = request.meta().get("pushToken");
                    String deviceId = request.meta().get("deviceId");
                    String deviceType = request.meta().get("deviceType");

                    if (token == null || token.isBlank()) yield Mono.error(new IllegalArgumentException("Missing 'pushToken' target value inside meta criteria"));
                    if (deviceId == null || deviceId.isBlank()) yield Mono.error(new IllegalArgumentException("Missing secure target hardware parameter 'deviceId'"));
                    if (deviceType == null || deviceType.isBlank()) yield Mono.error(new IllegalArgumentException("Missing targeting architectural flag 'deviceType'"));

                    var pushRow = new PushContactRow(null, userId, "PUSH", true, request.primary(), LocalDateTime.now(), token, deviceId, deviceType.toUpperCase());
                    yield pushContactRepository.save(pushRow)
                            .map(saved -> new ContactResponseDto(saved.id(), "PUSH", "SUCCESS", "Mobile device FCM broadcast key linked successfully."));
                }
                default -> Mono.error(new IllegalArgumentException("Unsupported message dispatch target communication channel type: " + channelType));
            };
        });
    }

    // =========================================================================
    // READ OPERATOR (Consolidated Pipeline Profiling)
    // =========================================================================

    public Mono<UserContactsProfileSummary> fetchUserProfileSummary(Long userId) {
        log.debug("[Service-Read] Asynchronously gathering multi-endpoint metadata matrices for user: {}", userId);

        // Все 4 запроса выполнятся параллельно (concurrently), так как Mono.zip подпишется на них одновременно
        Mono<List<String>> phones = smsContactRepository.findSmsByUserId(userId)
                .filter(SmsContactRow::active).map(SmsContactRow::phoneNumber).collectList();

        Mono<List<String>> emails = emailContactRepository.findEmailByUserId(userId)
                .filter(EmailContactRow::active).map(EmailContactRow::emailAddress).collectList();

        Mono<List<String>> tgChats = telegramContactRepository.findTelegramByUserId(userId)
                .filter(TelegramContactRow::active).map(TelegramContactRow::telegramChatId).collectList();

        Mono<List<String>> pushTokens = pushContactRepository.findPushByUserId(userId)
                .filter(PushContactRow::active).map(PushContactRow::pushToken).collectList();

        return Mono.zip(phones, emails, tgChats, pushTokens)
                .map(tuple -> new UserContactsProfileSummary(
                        userId,
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4()
                ));
    }

    // =========================================================================
    // UPDATE OPERATOR (Mute/Unmute State Mutations)
    // =========================================================================

    @Transactional
    public Mono<ContactResponseDto> updateContactStatus(Long userId, String type, Long contactId, boolean activeState) {
        return Mono.defer(() -> {
            log.info("[Service-Update] Altering status state onto {} for contact ID [{}] matching user {}", type, contactId, userId);

            return switch (type.toUpperCase()) {
                case "SMS" -> smsContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .flatMap(sms -> {
                            validateRowOwnership(sms, userId, contactId);
                            var updated = new SmsContactRow(sms.id(), userId, sms.contactType(), activeState, sms.primary(), sms.createdAt(), sms.phoneNumber());
                            return smsContactRepository.save(updated);
                        })
                        .map(saved -> new ContactResponseDto(saved.id(), "SMS", "SUCCESS", "Contact entity parameters mutated accurately."));

                case "EMAIL" -> emailContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .flatMap(email -> {
                            validateRowOwnership(email, userId, contactId);
                            var updated = new EmailContactRow(email.id(), userId, email.contactType(), activeState, email.primary(), email.createdAt(), email.emailAddress());
                            return emailContactRepository.save(updated);
                        })
                        .map(saved -> new ContactResponseDto(saved.id(), "EMAIL", "SUCCESS", "Contact entity parameters mutated accurately."));

                case "TELEGRAM" -> telegramContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .flatMap(tg -> {
                            validateRowOwnership(tg, userId, contactId);
                            var updated = new TelegramContactRow(tg.id(), userId, tg.contactType(), activeState, tg.primary(), tg.createdAt(), tg.telegramChatId(), tg.telegramUsername());
                            return telegramContactRepository.save(updated);
                        })
                        .map(saved -> new ContactResponseDto(saved.id(), "TELEGRAM", "SUCCESS", "Contact entity parameters mutated accurately."));

                case "PUSH" -> pushContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .flatMap(push -> {
                            validateRowOwnership(push, userId, contactId);
                            var updated = new PushContactRow(push.id(), userId, push.contactType(), activeState, push.primary(), push.createdAt(), push.pushToken(), push.deviceId(), push.deviceType());
                            return pushContactRepository.save(updated);
                        })
                        .map(saved -> new ContactResponseDto(saved.id(), "PUSH", "SUCCESS", "Contact entity parameters mutated accurately."));

                default -> Mono.error(new IllegalArgumentException("Unknown channel category bound descriptor: " + type));
            };
        });
    }

    // =========================================================================
    // DELETE OPERATOR (Purge Evictions)
    // =========================================================================

    @Transactional
    public Mono<ContactResponseDto> deleteUserContact(Long userId, String type, Long contactId) {
        return Mono.defer(() -> {
            log.warn("[Service-Delete] Initiating full physical row deletion for channel [{}] ID [{}] mapped under user {}", type, contactId, userId);

            Mono<Void> deleteOperation = switch (type.toUpperCase()) {
                case "SMS" -> smsContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .doOnNext(row -> validateRowOwnership(row, userId, contactId))
                        .flatMap(smsContactRepository::delete);

                case "EMAIL" -> emailContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .doOnNext(row -> validateRowOwnership(row, userId, contactId))
                        .flatMap(emailContactRepository::delete);

                case "TELEGRAM" -> telegramContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .doOnNext(row -> validateRowOwnership(row, userId, contactId))
                        .flatMap(telegramContactRepository::delete);

                case "PUSH" -> pushContactRepository.findById(contactId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Operation denied: target contact footprint record [" + contactId + "] does not exist.")))
                        .doOnNext(row -> validateRowOwnership(row, userId, contactId))
                        .flatMap(pushContactRepository::delete);

                default -> Mono.error(new IllegalArgumentException("Cannot purge records: unexpected channel key format: " + type));
            };

            return deleteOperation.thenReturn(new ContactResponseDto(contactId, type, "SUCCESS", "Contact completely purged from core cluster maps."));
        });
    }

    // ---------- private infrastructure validation tools ----------

    private void validateRegexp(String target, Pattern pattern, String exceptionMessage) {
        if (target == null || !pattern.matcher(target).matches()) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    private void validateRowOwnership(Object row, Long requestingUserId, Long targetContactId) {
        Long genuineOwnerId = switch (row) {
            case SmsContactRow sms -> sms.userId();
            case EmailContactRow email -> email.userId();
            case TelegramContactRow tg -> tg.userId();
            case PushContactRow push -> push.userId();
            default -> null;
        };

        if (genuineOwnerId == null || !genuineOwnerId.equals(requestingUserId)) {
            log.error("SECURITY VIOLATION: User {} attempted to access/mutate contact row ID {} belonging to owner: {}", requestingUserId, targetContactId, genuineOwnerId);
            throw new SecurityException("Data isolation guard access breach: Access denied to target records parameters.");
        }
    }
}