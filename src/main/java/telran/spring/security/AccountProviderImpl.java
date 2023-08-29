package telran.spring.security;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import telran.spring.security.dto.Account;

@Service
@Slf4j
public class AccountProviderImpl implements AccountProvider {

	@Value("${app.security.accounts.file.name:accounts.data}")
	private String fileName;

	@SuppressWarnings("unchecked")
	@Override
	public List<Account> getAccounts() {
		log.warn("AccountProviderImpl - getAccounts");
		log.info("Start restoring information");
		List<Account> res = Collections.emptyList();
		if (Files.exists(Path.of(fileName))) {
			try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(fileName))) {
				res = (List<Account>) stream.readObject();
				log.info("Accounts have been restored from the file {}", fileName);
			} catch (Exception e) {
				throw new IllegalStateException(
						String.format("Error $s during resotring from file $s", e.getMessage(), fileName));
			}
		}
		return res;
	}

	@Override
	public void setAccounts(List<Account> accounts) {
		log.info("Start saving information");
		log.warn("AccountProviderImpl - setAccounts");
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(fileName))) {
			stream.writeObject(accounts);
			log.info("{} accounts have been saved to the file {}", accounts.size(), fileName);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error $s during saving to file $s", e.toString(), fileName));
		}
	}

}
