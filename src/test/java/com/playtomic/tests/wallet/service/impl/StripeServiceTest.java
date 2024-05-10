package com.playtomic.tests.wallet.service.impl;


import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.boot.web.client.RestTemplateBuilder;

import com.playtomic.tests.wallet.service.stripe.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.stripe.StripeService;
import com.playtomic.tests.wallet.service.stripe.StripeServiceException;


/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 */

public class StripeServiceTest {
    private final static String CREDIT_CARD_NUMBER = "4242 4242 4242 4242";
    private final static int AMOUNT_TOO_SMALL = 3;
    private final static int AMOUNT = 300;

    private static ClientAndServer mockServer;


    StripeService s;

    @BeforeAll
    static void beforeAll() {
        mockServer = startClientAndServer(9999);
        while (!mockServer.hasStarted(3, 100L, TimeUnit.MILLISECONDS)) {
        }

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockAmountTooSmall(mockServerClient, CREDIT_CARD_NUMBER, AMOUNT_TOO_SMALL);
        mockSuccess(mockServerClient);

        while (!mockServerClient.hasStarted(3, 100L, TimeUnit.MILLISECONDS)) {
        }

    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
        while (!mockServer.hasStopped(3, 100L, TimeUnit.MILLISECONDS)) {
        }

    }

    @BeforeEach
    void setUp() {
        URI testUri = URI.create("http://localhost:9999");

        s = new StripeService(testUri, testUri, new RestTemplateBuilder());
    }

    @Test
    public void test_exception() {

        Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
            s.charge(CREDIT_CARD_NUMBER, new BigDecimal(AMOUNT_TOO_SMALL));
        });
    }

    @Test
    public void test_ok() throws StripeServiceException {
        s.charge(CREDIT_CARD_NUMBER, new BigDecimal(AMOUNT));
    }

    private static void mockSuccess(MockServerClient mockServerClient) {
        mockServerClient.when(
                                request()
                                        .withMethod("POST")
                                        .withBody("{\"credit_card\":\"%s\",\"amount\":%d}".formatted(CREDIT_CARD_NUMBER, AMOUNT))
                        )
                        .respond(
                                response()
                                        .withStatusCode(200)
                                        .withContentType(MediaType.APPLICATION_JSON)
                                        .withBody("{\"id\":\"py_12345\"}")
                        );
    }

    private static void mockAmountTooSmall(MockServerClient mockServerClient, String creditCardNumber, int amount) {
        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withBody("{\"credit_card\":\"%s\",\"amount\":%d}".formatted(creditCardNumber, amount))
                )
                .respond(
                        response()
                                .withStatusCode(422)
                );
    }
}
