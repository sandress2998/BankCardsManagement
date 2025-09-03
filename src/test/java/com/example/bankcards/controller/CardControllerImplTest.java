package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
public class CardControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    // Test data
    private String cardNumber1 = "1234567890123456";
    private String cardNumber2 = "1122334455667788";
    private String maskedCardNumber = "**** **** **** 3456";
    private UUID testUserId;
    private String testUserLogin;
    private UUID testCardId;
    private CardFullInfoResponse cardFullInfoResponse;
    private CardInfoResponse cardInfoResponse;
    private CardBalanceResponse cardBalanceResponse;
    private CardTransferMoney cardTransferMoney;
    private CardBalanceRequest cardBalanceRequest;
    private CardNumberBody cardNumberBody;
    private CardBlockingResponse cardBlockingResponse;
    private List<CardFullInfoResponse> cardFullInfoList;
    private List<CardInfoResponse> cardInfoList;
    private List<CardBlockingResponse> blockingRequestsList;

    @BeforeEach
    void setUp() {
        // Устанавливаем аутентификацию для всех тестов
        testUserId = UUID.randomUUID();
        testCardId = UUID.randomUUID();

        testUserLogin = "testUserLogin";

        Authentication auth = new UsernamePasswordAuthenticationToken(
                testUserId.toString(),
                "mock-jwt-token",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Инициализация тестовых данных
        initializeTestData();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void initializeTestData() {
        cardNumber1 = "1234567890123456";
        cardNumber2 = "1122334455667788";
        maskedCardNumber = "**** **** **** 3456";

        cardFullInfoResponse = new CardFullInfoResponse(
            testCardId,
            cardNumber1,
            testUserLogin,
            LocalDate.now().plusYears(2),
            Card.Status.ACTIVE,
            1000.0
        );

        cardInfoResponse = new CardInfoResponse(
                testCardId,
                maskedCardNumber,
                LocalDate.now().plusYears(2).toString(),
                Card.Status.ACTIVE,
                1000.0,
                testUserLogin
        );

        cardBalanceResponse = new CardBalanceResponse(
                1000.0
        );

        cardTransferMoney = new CardTransferMoney(
            cardNumber1,
            cardNumber2,
            1000.0
        );

        cardBalanceRequest = new CardBalanceRequest(
            cardNumber1,
            1000.0
        );

        cardNumberBody = new CardNumberBody(cardNumber1);

        cardBlockingResponse = new CardBlockingResponse(testCardId);

        cardFullInfoList = Arrays.asList(cardFullInfoResponse);
        cardInfoList = Arrays.asList(cardInfoResponse);
        blockingRequestsList = Arrays.asList(cardBlockingResponse);
    }

    // Тесты для GET /api/card/full-info
    @Test
    void getAllCards_ReturnsCardsList_OnSuccess() throws Exception {
        when(cardService.getAllCards()).thenReturn(cardFullInfoList);

        mockMvc.perform(get("/api/card/full-info"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(testCardId.toString()))
            .andExpect(jsonPath("$[0].owner").value(testUserLogin))
            .andExpect(jsonPath("$[0].number").value(cardNumber1));

        verify(cardService).getAllCards();
    }

    // Тесты для POST /api/card
    @Test
    void create_ReturnsCreatedCard_OnSuccess() throws Exception {
        when(cardService.create(eq(testUserId), eq(24))).thenReturn(cardInfoResponse);

        mockMvc.perform(post("/api/card")
                .param("ownerId", testUserId.toString())
                .param("monthsQuantityUntilExpires", "24"))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testCardId.toString()))
            .andExpect(jsonPath("$.owner").value(testUserLogin));

        verify(cardService).create(testUserId, 24);
    }

    @Test
    void create_ReturnsCreatedCard_WithoutMonthsParam() throws Exception {
        when(cardService.create(eq(testUserId), eq(null))).thenReturn(cardInfoResponse);

        mockMvc.perform(post("/api/card")
                .param("ownerId", testUserId.toString()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testCardId.toString()));

        verify(cardService).create(testUserId, null);
    }

    @Test
    void create_ReturnsNotFound_WhenUserNotFound() throws Exception {
        when(cardService.create(any(UUID.class), any()))
                .thenThrow(new com.example.bankcards.exception.NotFoundException("User not found"));

        mockMvc.perform(post("/api/card")
                .param("ownerId", testUserId.toString()))
            .andExpect(status().isNotFound());
    }

    // Тесты для PATCH /api/card/{cardId}
    @Test
    void updateCardStatus_ReturnsNoContent_OnSuccess() throws Exception {
        doNothing().when(cardService).updateCard(Card.CardAction.ACTIVATE, testCardId);

        mockMvc.perform(patch("/api/card/{cardId}", testCardId)
                .param("action", "ACTIVATE"))
            .andExpect(status().isNoContent());

        verify(cardService).updateCard(Card.CardAction.ACTIVATE, testCardId);
    }

    @Test
    void updateCardStatus_ReturnsNotFound_WhenCardNotFound() throws Exception {
        doThrow(new com.example.bankcards.exception.NotFoundException("Card not found"))
                .when(cardService).updateCard(any(Card.CardAction.class), any(UUID.class));

        mockMvc.perform(patch("/api/card/{cardId}", testCardId)
                .param("action", "ACTIVATE"))
            .andExpect(status().isNotFound());
    }

    // Тесты для DELETE /api/card/{cardId}
    @Test
    void delete_ReturnsNoContent_OnSuccess() throws Exception {
        doNothing().when(cardService).delete(testCardId);

        mockMvc.perform(delete("/api/card/{cardId}", testCardId))
            .andExpect(status().isNoContent());

        verify(cardService).delete(testCardId);
    }

    @Test
    void delete_ReturnsNotFound_WhenCardNotFound() throws Exception {
        doThrow(new com.example.bankcards.exception.NotFoundException("Card not found"))
            .when(cardService).delete(any(UUID.class));

        mockMvc.perform(delete("/api/card/{cardId}", testCardId))
            .andExpect(status().isNotFound());
    }

    // Тесты для POST /api/card/transfer
    @Test
    void doMoneyTransfer_ReturnsNoContent_OnSuccess() throws Exception {
        doNothing().when(cardService).doMoneyTransfer(any(CardTransferMoney.class));

        mockMvc.perform(post("/api/card/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardTransferMoney)))
            .andExpect(status().isNoContent());

        verify(cardService).doMoneyTransfer(any(CardTransferMoney.class));
    }

    @Test
    void doMoneyTransfer_ReturnsBadRequest_OnInvalidData() throws Exception {
        doThrow(new com.example.bankcards.exception.BadRequestException("Invalid transfer data"))
                .when(cardService).doMoneyTransfer(any(CardTransferMoney.class));

        mockMvc.perform(post("/api/card/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardTransferMoney)))
            .andExpect(status().isBadRequest());
    }

    // Тесты для GET /api/card
    @Test
    void getCardsInfo_ReturnsCardsList_OnSuccess() throws Exception {
        when(cardService.getCardsInfo(testUserId, Card.Status.ACTIVE, 0, 3))
            .thenReturn(cardInfoList);

        mockMvc.perform(get("/api/card")
                .param("id", testUserId.toString())
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(testCardId.toString()));

        verify(cardService).getCardsInfo(testUserId, Card.Status.ACTIVE, 0, 3);
    }

    @Test
    void getCardsInfo_UsesDefaultParameters_WhenNotProvided() throws Exception {
        when(cardService.getCardsInfo(null, null, 0, 3)).thenReturn(cardInfoList);

        mockMvc.perform(get("/api/card"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(cardService).getCardsInfo(null, null, 0, 3);
    }

    // Тесты для PATCH /api/card/balance/update
    @Test
    void updateCardBalance_ReturnsUpdatedBalance_OnSuccess() throws Exception {
        when(cardService.updateCardBalanceAction(Card.BalanceAction.DEPOSIT_MONEY, cardBalanceRequest))
            .thenReturn(cardBalanceResponse);

        mockMvc.perform(patch("/api/card/balance/update")
                .param("action", "DEPOSIT_MONEY")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardBalanceRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.balance").value(1000.00));

        verify(cardService).updateCardBalanceAction(Card.BalanceAction.DEPOSIT_MONEY, cardBalanceRequest);
    }

    @Test
    void updateCardBalance_ReturnsBadRequest_OnInvalidOperation() throws Exception {
        when(cardService.updateCardBalanceAction(any(Card.BalanceAction.class), any(CardBalanceRequest.class)))
            .thenThrow(new com.example.bankcards.exception.BadRequestException("Insufficient funds"));

        mockMvc.perform(patch("/api/card/balance/update")
                .param("action", "WITHDRAW_MONEY")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardBalanceRequest)))
            .andExpect(status().isBadRequest());
    }

    // Тесты для POST /api/card/balance/view
    @Test
    void getBalance_ReturnsBalance_OnSuccess() throws Exception {
        when(cardService.getBalance(any(CardNumberBody.class))).thenReturn(cardBalanceResponse);

        mockMvc.perform(post("/api/card/balance/view")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardNumberBody)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.balance").value(1000.00));

        verify(cardService).getBalance(any(CardNumberBody.class));
    }

    @Test
    void getBalance_ReturnsNotFound_WhenCardNotFound() throws Exception {
        when(cardService.getBalance(any(CardNumberBody.class)))
                .thenThrow(new com.example.bankcards.exception.NotFoundException("Card not found"));

        mockMvc.perform(post("/api/card/balance/view")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardNumberBody)))
            .andExpect(status().isNotFound());
    }

    // Тесты для POST /api/card/blocking/submit
    @Test
    void submitCardBlocking_ReturnsCreated_OnSuccess() throws Exception {
        doNothing().when(cardService).submitCardBlocking(any(CardNumberBody.class));

        mockMvc.perform(post("/api/card/blocking/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardNumberBody)))
            .andExpect(status().isCreated());

        verify(cardService).submitCardBlocking(any(CardNumberBody.class));
    }

    @Test
    void submitCardBlocking_ReturnsNotFound_WhenCardNotFound() throws Exception {
        doThrow(new com.example.bankcards.exception.NotFoundException("Card not found"))
                .when(cardService).submitCardBlocking(any(CardNumberBody.class));

        mockMvc.perform(post("/api/card/blocking/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardNumberBody)))
            .andExpect(status().isNotFound());
    }

    // Тесты для POST /api/card/blocking/process/{cardId}
    @Test
    void processBlockRequest_ReturnsNoContent_OnSuccess() throws Exception {
        doNothing().when(cardService).processBlockRequest(testCardId);

        mockMvc.perform(post("/api/card/blocking/process/{cardId}", testCardId))
            .andExpect(status().isNoContent());

        verify(cardService).processBlockRequest(testCardId);
    }

    @Test
    void processBlockRequest_ReturnsNotFound_WhenCardNotFound() throws Exception {
        doThrow(new com.example.bankcards.exception.NotFoundException("Card not found"))
            .when(cardService).processBlockRequest(any(UUID.class));

        mockMvc.perform(post("/api/card/blocking/process/{cardId}", testCardId))
            .andExpect(status().isNotFound());
    }

    // Тесты для GET /api/card/blocking
    @Test
    void getBlockRequests_ReturnsRequestsList_OnSuccess() throws Exception {
        when(cardService.getBlockRequests(0, 5)).thenReturn(blockingRequestsList);

        mockMvc.perform(get("/api/card/blocking")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].cardId").value(testCardId.toString()));

        verify(cardService).getBlockRequests(0, 5);
    }

    @Test
    void getBlockRequests_UsesDefaultParameters_WhenNotProvided() throws Exception {
        when(cardService.getBlockRequests(0, 5)).thenReturn(blockingRequestsList);

        mockMvc.perform(get("/api/card/blocking"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(cardService).getBlockRequests(0, 5);
    }

    @Test
    void getBlockRequests_ReturnsEmptyList_WhenNoRequests() throws Exception {
        when(cardService.getBlockRequests(0, 5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/card/blocking"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
