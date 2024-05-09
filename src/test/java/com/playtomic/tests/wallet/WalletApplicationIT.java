package com.playtomic.tests.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.playtomic.tests.wallet.service.WalletLockRepository;
import com.playtomic.tests.wallet.service.WalletRepository;
import com.playtomic.tests.wallet.service.domain.Lock;
import com.playtomic.tests.wallet.service.domain.Wallet;
import com.playtomic.tests.wallet.service.domain.WalletId;
import com.playtomic.tests.wallet.service.stripe.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.stripe.StripeService;
import com.playtomic.tests.wallet.service.stripe.StripeServiceException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@Transactional
public class WalletApplicationIT {

	private static final String CREDIT_CARD_NUMBER = "42424242424242424242";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private WalletLockRepository walletLockRepository;
	@Autowired
	private WalletRepository walletRepository;
	@MockBean
	private StripeService stripeService;


	@Test
	public void baseTest() throws Exception {

		mvc.perform(get("/"))
				.andExpect(status().isOk());

	}

	@Test
	void shouldReturnWalletFound() throws Exception {
		// given
		long expectedBalance = 3000L;
		WalletId walletId = aWalletWithBalance(expectedBalance);

		// when
		ResultActions result = mvc.perform(get("/wallet/" + walletId.id())
												   .contentType("application/json"));

		// then
		result
				.andExpect(status().isOk())
				.andExpect(content().json("{'id':  '" + walletId.id() + "', 'balanceAmountUnit':  " + expectedBalance + "}"));
	}

	@Test
	void shouldReturnWalletNotFound() {

	}

	@Test
	public void shouldReturnHTTP423IfLocked() throws Exception {
		// given
		Instant now = Instant.now();
		long initialBalance = 0L;
		WalletId walletId = aWalletWithBalance(initialBalance);
		aLockOnWalletWithExpiration(walletId, now.plus(3, ChronoUnit.DAYS));

		// when
		ResultActions result = mvc.perform(post(walletTopUpPath(walletId))
												   .contentType("application/json")
												   .content("{\"amountUnit\":  3000}"));
		// then
		result
				.andExpect(status().isLocked())
				.andExpect(content().json("{'errorMessage':  '" + walletId.id() + " is locked'}"));
		walletBalanceIs(walletId, initialBalance);
		noInteractionWithStripe();
	}

	@Test
	public void shouldReturnHTTP400IfAmountTooSmall() throws Exception {
		// given
		long initialBalance = 0L;
		int amount = 100;
		WalletId walletId = aWalletWithBalance(initialBalance);
		stripeThrowsAnError(amount, new StripeAmountTooSmallException());
		// when
		ResultActions result = mvc.perform(post(walletTopUpPath(walletId))
												   .contentType("application/json")
												   .content(getFormattedTopUpRequestCreditCard(amount)));
		// then
		result
				.andExpect(status().isBadRequest())
				.andExpect(content().json("{'errorMessage':  'top-up amount is too small'}"));
		walletBalanceIs(walletId, initialBalance);
		walletIsUnlocked(walletId);
	}

	@Test
	public void shouldReturnHTTP502IfAnErrorOccursOnStripe() throws Exception {
		// given
		long initialBalance = 0L;
		int amount = 30000;
		WalletId walletId = aWalletWithBalance(initialBalance);
		stripeThrowsAnError(amount, new StripeServiceException());
		// when
		ResultActions result = mvc.perform(post(walletTopUpPath(walletId))
												   .contentType("application/json")
												   .content(getFormattedTopUpRequestCreditCard(amount)));
		// then
		result
				.andExpect(status().isBadGateway())
				.andExpect(content().json("{'errorMessage':  'error with payment provider'}"));
		walletBalanceIs(walletId, initialBalance);
	}


	@Test
	public void shouldReturnHTTP200AndUpdateBalance() throws Exception {
		// given
		int amount = 30000;
		long initialBalance = 0L;
		WalletId walletId = aWalletWithBalance(initialBalance);
		// when
		ResultActions result = mvc.perform(post(walletTopUpPath(walletId))
												   .contentType("application/json")
												   .content(getFormattedTopUpRequestCreditCard(amount)));
		// then
		result
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").doesNotExist());
		isChargedOnStripe(amount, CREDIT_CARD_NUMBER);
		walletBalanceIs(walletId, initialBalance + amount);
		walletIsUnlocked(walletId);
	}

	private void isChargedOnStripe(int amount, String creditCardNumber) {
		verify(stripeService).charge(eq(creditCardNumber), eq(new BigDecimal(amount / 100)));
	}

	private void walletIsUnlocked(WalletId walletId) {
		assertThat(walletLockRepository.getLock(getLockWalletLockName(walletId)))
				.isEmpty();
	}

	private void walletBalanceIs(WalletId walletId, long initialBalance) {
		assertThat(walletRepository.getWallet(walletId))
				.get()
				.matches(w -> w.getBalanceAmountUnit().equals(initialBalance));
	}

	private void noInteractionWithStripe() {
		verifyNoMoreInteractions(stripeService);
	}

	private void stripeThrowsAnError(int amount, RuntimeException whatever) {
		when(stripeService.charge(any(), eq(new BigDecimal(amount / 100))))
				.thenThrow(whatever);
	}

	private String walletTopUpPath(WalletId walletId) {
		return "/wallet/" + walletId.id() + "/top-up";
	}

	private String getFormattedTopUpRequestCreditCard(int amount) {
		return "{\"amountUnit\":%d, \"creditCardNumber\": \"%s\" }"
				.formatted(amount, CREDIT_CARD_NUMBER);
	}

	private void aLockOnWalletWithExpiration(WalletId walletId, Instant lockExpiration) {
		walletLockRepository.insertLock(
				new Lock(getLockWalletLockName(walletId), lockExpiration)
		);
	}

	private static String getLockWalletLockName(WalletId walletId) {
		return "WALLET#" + walletId.id();
	}

	private WalletId aWalletWithBalance(long balanceAmountUnit) {
		WalletId walletId = new WalletId(UUID.randomUUID().toString());
		walletRepository.insertWallet(new Wallet(
				walletId,
				balanceAmountUnit
		));
		return walletId;
	}

}
