package telran.spring;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;
import telran.spring.exceptions.NotFoundException;
import telran.spring.security.AccountProvider;
import telran.spring.security.AccountProviderImpl;
import telran.spring.security.AccountService;
import telran.spring.security.AccountServiceImpl;
import telran.spring.security.AccountingConfiguration;
import telran.spring.security.dto.Account;

@Slf4j
@SpringBootTest(classes = { AccountProviderImpl.class, AccountServiceImpl.class, AccountingConfiguration.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountingServiceTest {

	Account account = new Account("user", "userPass", new String[] { "USER" });

	@Autowired
	AccountService service;
	@Autowired
	UserDetailsManager userDetailsManager;
	@Autowired
	AccountProvider provider;

	@AfterAll
	static void deleteFileAfter() throws IOException {
		Files.deleteIfExists(Path.of("test.data"));
	}

	@BeforeAll
	static void deleteFileBefore() throws IOException {
		Files.deleteIfExists(Path.of("test.data"));
	}

	@Test
	void adminExtsTest() {
		assertTrue(userDetailsManager.userExists("admin"));
	}

	@Test
	@Order(1)
	void addAccountNormalFlow() {
		service.addAccount(account);
		log.debug("current time is {}, expired time is {}", LocalDateTime.now(),
				service.getAccount("user").getExpDate());
		assertTrue(userDetailsManager.userExists("user"));
		assertTrue(userDetailsManager.loadUserByUsername("user").isAccountNonExpired());
	}

	@Order(2)
	void addAccountExistsFlow() {
		assertThrowsExactly(IllegalStateException.class, () -> service.addAccount(account));

	}

	@Test
	@Order(3)
	void updatePasswordSamePasswordFlow() {
		assertThrowsExactly(IllegalStateException.class, () -> service.updatePassword("user", "userPass"));
	}

	@Test
	@Order(4)
	void updatePasswordNormalFlow() {
		service.updatePassword("user", "userPass1");

	}

	@Test
	@Order(5)
	void deleteUserNormalFlow() {
		service.deleteAccount("user");
		service.addAccount(account);
	}

	@Test
	@Order(6)
	void deleteUserNotExistsFlow() {
		assertThrowsExactly(NotFoundException.class, () -> service.deleteAccount("xxxxx"));

	}

	@Test
	@Order(7)
	void expirationTest() throws InterruptedException {
		Thread.sleep(2500);
		assertFalse(userDetailsManager.loadUserByUsername("user").isAccountNonExpired());
		service.updatePassword("user", "userPass2");
		service.updatePassword("user", "userPass3");
		service.updatePassword("user", "userPass4");
		service.updatePassword("user", "userPass"); // after three updates the first password may be repeated
		assertTrue(userDetailsManager.loadUserByUsername("user").isAccountNonExpired());

	}

	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
	@Test
	@Order(8)
	void persistenceTest() {
		assertEquals("user", service.getAccount("user").getUsername());
		assertTrue(userDetailsManager.loadUserByUsername("user").isAccountNonExpired());
		assertThrowsExactly(IllegalStateException.class, () -> service.updatePassword("user", "userPass"));
	}

}