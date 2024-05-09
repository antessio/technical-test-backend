package com.playtomic.tests.wallet.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.playtomic.tests.wallet.service.WalletService;
import com.playtomic.tests.wallet.service.domain.PaymentProviderBadRequestException;
import com.playtomic.tests.wallet.service.domain.PaymentProviderException;
import com.playtomic.tests.wallet.service.domain.Wallet;
import com.playtomic.tests.wallet.service.domain.WalletException;
import com.playtomic.tests.wallet.service.domain.WalletId;
import com.playtomic.tests.wallet.service.domain.WalletLockException;
import com.playtomic.tests.wallet.service.domain.WalletTopUpCommand;

@RestController
public class WalletController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }



    

    @RequestMapping("/")
    void log() {
        LOGGER.info("Logging from /");
    }

    @RequestMapping(value = "/wallet/{walletId}/top-up", method = RequestMethod.POST)
    void walletTopUp(@PathVariable String walletId, @RequestBody WalletTopUpCommand walletTopUpCommand) {
        walletService.topUpWallet(
                new WalletId(walletId),
                walletTopUpCommand
        );
    }
    @RequestMapping(value = "/wallet/{walletId}")
    ResponseEntity<Wallet> getWallet(@PathVariable String walletId){
        return walletService.getWallet(new WalletId(walletId))
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }
    @ExceptionHandler(WalletLockException.class)
    public ResponseEntity<?> walletLocked(WalletLockException e) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                             .body(errorResponseBody(e.getMessage()));
    }
    @ExceptionHandler(WalletException.class)
    public ResponseEntity<?> internalWalletException(WalletException e) {
        return ResponseEntity.internalServerError()
                             .body(errorResponseBody(e.getMessage()));
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> internalServerError(RuntimeException e) {
        return ResponseEntity.internalServerError()
                             .body(errorResponseBody(e.getMessage()));
    }
    @ExceptionHandler(PaymentProviderBadRequestException.class)
    public ResponseEntity<?> paymentProviderBadRequest(PaymentProviderBadRequestException e) {
        return ResponseEntity.badRequest()
                             .body(errorResponseBody(e.getMessage()));
    }
    @ExceptionHandler(PaymentProviderException.class)
    public ResponseEntity<?> paymentProviderError(PaymentProviderException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                             .body(errorResponseBody(e.getMessage()));
    }
    @JsonComponent
    public static class WalletIdSerializer extends JsonSerializer<WalletId> {

        @Override
        public void serialize(WalletId walletId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeObject(walletId.id());
        }

    }

    private static String errorResponseBody(String message) {
        return "{\"errorMessage\": \"" + message + "\"}";
    }

}
