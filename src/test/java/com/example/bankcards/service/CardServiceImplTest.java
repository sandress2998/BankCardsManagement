package com.example.bankcards.service;

import com.example.bankcards.dto.CardTransferMoney;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardBlockingRequestRepository;
import com.example.bankcards.repository.CardHashRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock CardRepository cardRepository;
    @Mock CardHashRepository cardHashRepository;
    @Mock UserRepository userRepository;
    @Mock CardSecurityService cardSecurityService;
    @Mock CardBlockingRequestRepository cardBlockingRequestRepository;
    @Mock Authentication authentication;
    @Mock SecurityContext securityContext;

    CardServiceImpl service;

    int defaultExpiresMonths = 24;

    @BeforeEach
    void setUp() {
        service = new CardServiceImpl(
                defaultExpiresMonths,
                cardRepository,
                cardSecurityService,
                userRepository,
                cardHashRepository,
                cardBlockingRequestRepository
        );

        service.monthsQuantityUntilExpiresDefault = defaultExpiresMonths;

        // Создаем моки для аутентификации, чтобы getAuthData не выбрасывал UnauthorizedException
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // setValidityPeriod tests
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void setValidityPeriod_givenCustomMonth_returnsCorrectDate() {
        int months = 2;
        LocalDate expected = LocalDate.now().plusMonths(months).with(TemporalAdjusters.lastDayOfMonth());
        assertEquals(expected, service.setValidityPeriod(months));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void setValidityPeriod_givenNull_usesDefault() {
        LocalDate expected = LocalDate.now().plusMonths(defaultExpiresMonths).with(TemporalAdjusters.lastDayOfMonth());
        assertEquals(expected, service.setValidityPeriod(null));
    }

    // create - unique card number generation failure
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void create_throwsRuntimeException_whenCannotGenerateUniqueCardNumber() {
        UUID ownerId = UUID.randomUUID();
        User owner = new User("owner", "password", User.Role.USER);
        when(userRepository.findUserById(ownerId)).thenReturn(owner);

        CardServiceImpl spyService = spy(service);
        doThrow(new RuntimeException("Too much attempts to generate card number. Something went wrong."))
            .when(spyService).generateUniqueCardNumber();

        assertThrows(RuntimeException.class, () -> spyService.create(ownerId, 1));
    }

    // create - encryptNumber throws internal error
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void create_throwsRuntimeException_whenEncryptNumberFails() {
        UUID ownerId = UUID.randomUUID();
        User owner = new User("owner", "password", User.Role.USER);
        when(userRepository.findUserById(ownerId)).thenReturn(owner);

        CardServiceImpl spyService = spy(service);
        doReturn("1234567890123456").when(spyService).generateUniqueCardNumber();

        SecretKey secretKey = mock(SecretKey.class);
        when(cardSecurityService.generateKey()).thenReturn(secretKey);
        when(cardSecurityService.encryptNumber(anyString(), eq(secretKey))).thenThrow(new RuntimeException("Fail"));

        assertThrows(RuntimeException.class, () -> spyService.create(ownerId, 1));
    }

    // getCardByNumber - throws NotFoundException if card not found
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getCardByNumber_throwsNotFoundException_whenNoMatchingCard() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(cardRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());

        Throwable thrown = assertThrows(Throwable.class, () -> {
            try {
                service.getClass()
                        .getDeclaredMethod("getCardByNumber", String.class)
                        .invoke(service, "1234567890123456");
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getTargetException();  // распаковываем реальное исключение
            }
        });

        assertTrue(thrown instanceof NotFoundException);
    }


    // doMoneyTransfer - successful flow
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void doMoneyTransfer_success() {
        // Prepare cards
        Card cardFrom = mock(Card.class);
        Card cardTo = mock(Card.class);
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(cardFrom.getId()).thenReturn(fromId);
        when(cardTo.getId()).thenReturn(toId);
        when(cardFrom.getBalance()).thenReturn(1000.0);
        when(cardTo.getBalance()).thenReturn(500.0);
        when(cardFrom.getStatus()).thenReturn(Card.Status.ACTIVE);
        when(cardTo.getStatus()).thenReturn(Card.Status.ACTIVE);
        when(cardFrom.getValidityPeriod()).thenReturn(LocalDate.now().plusDays(10));
        when(cardTo.getValidityPeriod()).thenReturn(LocalDate.now().plusDays(10));

        CardServiceImpl spy = spy(service);

        doReturn(cardFrom).when(spy).getCardByNumber("1111");
        doReturn(cardTo).when(spy).getCardByNumber("2222");

        doNothing().when(spy).checkCardAvailable(cardFrom);
        doNothing().when(spy).checkCardAvailable(cardTo);
        doNothing().when(spy).validateBalanceUpdateCorrect(cardFrom, -200.0);
        doNothing().when(spy).validateBalanceUpdateCorrect(cardFrom, 200.0);

        doNothing().when(cardRepository).transferMoney(fromId, toId, 200.0);

        CardTransferMoney transferBody = new CardTransferMoney("1111", "2222", 200.0);

        spy.doMoneyTransfer(transferBody);

        verify(cardRepository).transferMoney(fromId, toId, 200.0);
    }

    // doMoneyTransfer - negative amount throws BadRequestException
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void doMoneyTransfer_negativeAmount_throwsBadRequest() {
        CardTransferMoney transferBody = new CardTransferMoney("1111", "2222", -100.0);
        assertThrows(BadRequestException.class, () -> service.doMoneyTransfer(transferBody));
    }

    // doMoneyTransfer - getCardByNumber throws NotFoundException
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void doMoneyTransfer_cardFromNotFound_throwsNotFound() {
        CardServiceImpl spy = spy(service);
        doThrow(new NotFoundException("Card not found")).when(spy).getCardByNumber("1111");

        CardTransferMoney transferBody = new CardTransferMoney("1111", "2222", 10.0);
        assertThrows(NotFoundException.class, () -> spy.doMoneyTransfer(transferBody));
    }

    // processBlockRequest - cardId null throws BadRequestException
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void processBlockRequest_cardIdNull_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.processBlockRequest(null));
    }

    // processBlockRequest - cardId not found throws NotFoundException
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void processBlockRequest_cardIdNotExisting_throwsNotFound() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsCardById(cardId)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.processBlockRequest(cardId));
    }

    // processBlockRequest - success updates card and deletes block request
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void processBlockRequest_success() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsCardById(cardId)).thenReturn(true);

        service.processBlockRequest(cardId);

        verify(cardRepository).updateCardStatus(cardId, Card.Status.BLOCKED);
        verify(cardBlockingRequestRepository).deleteById(cardId);
    }
}

