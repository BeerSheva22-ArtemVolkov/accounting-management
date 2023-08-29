package telran.spring;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.security.AccountService;
import telran.spring.security.PasswordValidator;
import telran.spring.security.dto.Account;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
@CrossOrigin 
@Slf4j
public class AccountController {

	final AccountService accountService;
	final PasswordValidator passwordValidator;
	
	@GetMapping("{username}")
	public Account getAccount(@PathVariable String username) {
		log.warn("AccountController - getAccount");
		return accountService.getAccount(username);
	}

	@PostMapping
	public void addAccount(@RequestBody @Valid Account account) {
		log.warn("AccountController - addAccount");
		passwordValidator.validate(account.getPassword());
		accountService.addAccount(account);
	}

	@PutMapping("{username}")
	public void updatePassword(@PathVariable String username, @RequestBody String newPassword) {
		log.warn("AccountController - updatePassword");
		passwordValidator.validate(newPassword);
		accountService.updatePassword(username, newPassword);
	}

	@DeleteMapping("{username}")
	public void deleteAccount(@PathVariable String username) {
		log.warn("AccountController - deleteAccount");
		accountService.deleteAccount(username);
	}
	
}
