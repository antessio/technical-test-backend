package com.playtomic.tests.wallet.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletController.class);


    @RequestMapping("/")
    void log() {
        LOGGER.info("Logging from /");
    }
}
