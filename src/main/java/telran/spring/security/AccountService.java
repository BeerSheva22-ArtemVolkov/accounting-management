package telran.spring.security;

import telran.spring.security.dto.Account;

public interface AccountService {

	Account getAccount(String username);
	void addAccount(Account account);
	void updatePassword(String username, String newPassword);
	void deleteAccount(String username);
}
