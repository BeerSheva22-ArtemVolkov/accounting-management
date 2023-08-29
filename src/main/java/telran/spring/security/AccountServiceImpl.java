package telran.spring.security;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import jakarta.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.exceptions.NotFoundException;
import telran.spring.security.dto.Account;

@Configuration
@RequiredArgsConstructor
@Slf4j
// нужен чтобы быстро находить требуемый аккаунт и чтобы используя provider обеспечивать percistance 
public class AccountServiceImpl implements AccountService {

	final PasswordEncoder passwordEncoder;
	final AccountProvider provider;
	ConcurrentHashMap<String, Account> accountsMap = new ConcurrentHashMap<String, Account>();

	@Value("${app.expiration.period:600}")
	long expirationPeriodHours;
	
	@Value("${app.expiration.time.unit:HOURS}")
    private ChronoUnit unit;

	@Value("${app.security.passwords.limit:3}")
	int limitPasswords;

	@Value("${app.security.validation.period:3600000}")
	private long validationPeriod;

	@Value("${app.expiration.period:600}")
    private long expirationPeriod;
	
	// нужно чтобы делать синхранизацию аккаунтов на добавление с UserDetailsManager
	@Autowired
	// предоставляет методы для создания, обновления и удаления пользователей, а
	// также для управления их ролями и привилегиями.
	UserDetailsManager userDetailsManager; // Строится в AccountingConfiguration

	@Override
	public Account getAccount(String username) {
		Account res = accountsMap.get(username);
		if (res == null) {
			throw new NotFoundException("<" + username + "> not found");
		}
		return res;
	}

	@Override
	public void addAccount(Account account) {
		String username = account.getUsername();
		if (userDetailsManager.userExists(username)) {
			throw new IllegalArgumentException(String.format("User <%s> already exists", username));
		}
		if (accountsMap.containsKey(username)) {
			throw new RuntimeException("Error of synchronization between accounts and accounts manager");
		}
		String plainPassword = account.getPassword();
		String passwordHash = passwordEncoder.encode(plainPassword);
		Account user = new Account(username, passwordHash, account.getRoles());
		LocalDateTime localDateTime = getExpired();

		user.setExpDate(localDateTime);
		LinkedList<String> passwords = new LinkedList<String>();
		passwords.add(passwordHash);
		user.setPasswords(passwords);

		createUser(user);
		log.debug("User <{}> created", username);

	}

	@Override
	public void updatePassword(String username, String newPassword) {

		Account account = accountsMap.get(username);

		if (account == null) {
			throw new NotFoundException("<" + username + "> not found");
		}

		updateUser(account, newPassword);
		log.debug("User <{}> updated", username);

	}

	@Override
	public void deleteAccount(String username) {

		Account account = accountsMap.remove(username);

		if (account == null) {
			throw new NotFoundException(username + " not found");
		}

		userDetailsManager.deleteUser(username);
		log.debug("User <{}> deleted", username);

	}

	@PostConstruct
	void restoreAccount() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(validationPeriod);
				} catch (InterruptedException e) {

				}
				expirationValidation();
			}
		});
		thread.setDaemon(true);
		thread.start();

		List<Account> listAccounts = provider.getAccounts();
		listAccounts.forEach(a -> createUser(a));
	}

	@PreDestroy
	void saveAccounts() {
		provider.setAccounts(new LinkedList<Account>(accountsMap.values()));
	}

	void expirationValidation() {
		int[] count = { 0 };
		accountsMap.values().stream().filter(this::isExpired).forEach(a -> {
			log.debug("account {} expired", a);
			userDetailsManager.updateUser(
					User.withUsername(a.getUsername()).password(a.getPassword()).accountExpired(true).build());
			count[0]++;
		});
		log.debug("expiration validation {} accounts have been expired", count[0]);
	}

	private boolean isExpired(Account a) {
		return LocalDateTime.now().isAfter(a.getExpDate());
	}

	private void createUser(Account user) {
		accountsMap.putIfAbsent(user.getUsername(), user);
		userDetailsManager.createUser(createUserDetails(user));
	}

	private void updateUser(Account account, String newPassword) {

		if (account.getPasswords().stream().anyMatch(hash -> passwordEncoder.matches(newPassword, hash))) {
			throw new IllegalStateException("Mismatches Password Starategy");
		}

		LinkedList<String> passwords = account.getPasswords();
		String hashPassword = passwordEncoder.encode(newPassword);
		String username = account.getUsername();
		String[] roles = account.getRoles();
		LocalDateTime expTime = getExpired();

		if (passwords.size() == limitPasswords) {
			passwords.removeFirst();
		}

		passwords.add(hashPassword);
		Account newAccount = new Account(username, hashPassword, roles);
		newAccount.setPasswords(passwords);
		newAccount.setExpDate(expTime);
		accountsMap.put(username, newAccount);
		userDetailsManager.updateUser(createUserDetails(newAccount));
	}
	
	private UserDetails createUserDetails(Account account) {
		return User.withUsername(account.getUsername())
				.password(account.getPassword()).roles(account.getRoles())
				.accountExpired(isExpired(account)).build();
	}

	private LocalDateTime getExpired() {
		return LocalDateTime.now().plus(expirationPeriod, unit);
	}
	
}
