package com.playtomic.tests.wallet.service.impl;


import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.math.BigDecimal;
import java.net.URI;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import com.playtomic.tests.wallet.service.stripe.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.stripe.StripeService;
import com.playtomic.tests.wallet.service.stripe.StripeServiceException;


/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 */

public class StripeServiceTest {

    private static final DockerImageName MOCKSERVER_IMAGE = DockerImageName
            .parse("mockserver/mockserver")
            .withTag("mockserver-" + StripeServiceTest.class.getPackage().getImplementationVersion());

    private static MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE);
    private URI TEST_URI = URI.create(mockServer.getHost());

    StripeService s = new StripeService(TEST_URI, TEST_URI, new RestTemplateBuilder());

    @BeforeAll
    static void beforeAll() {
        mockServer.start();
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @Test
    public void test_exception() {
        String creditCardNumber = "4242 4242 4242 4242";
        int amount = 5;
        try (MockServerClient mockServerClient = new MockServerClient("localhost", 9999)) {
            mockServerClient.when(
                                    request()
                                            .withMethod("POST")
                                            .withBody("{\"credit_card\": \"%s\", \"amount\": %d}".formatted(creditCardNumber, amount))
                            )
                            .respond(
                                    response()
                                            .withStatusCode(422)
                            );
        }

        Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
            s.charge(creditCardNumber, new BigDecimal(amount));
        });
    }

    @Test
    public void test_ok() throws StripeServiceException {
        s.charge("4242 4242 4242 4242", new BigDecimal(15));
    }
}
