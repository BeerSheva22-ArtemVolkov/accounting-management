package telran.spring.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidatorImpl implements PasswordValidator {

	@Override
	public void validate(String password) {
		boolean res = true;
		String[] characters = password.split("");
		if (characters.length < 8) {
			throw new IllegalArgumentException("min length of the password must be 8 symbols");
		}
		int index = 0;
		boolean flCapital = false;
		boolean flLower = false;
		boolean flDigit = false;
		String message = "";
		while (index < characters.length && res) {
			String symbol = characters[index++];
			res = symbol.matches("[\\w.]");
			if (res) {
				flCapital = flCapital || symbol.matches("[A-Z]");
				flLower = flLower || symbol.matches("[a-z]");
				flDigit = flDigit || symbol.matches("\\d");
			}

		}
		message = res ? getMessage(flCapital, flLower, flDigit) : "password may contain only letters, digits, point, underscore";
		if(!message.isEmpty()) {
			throw new IllegalArgumentException("in password: " + message);
		}

	}

	private String getMessage(boolean flCapital, boolean flLower, boolean flDigit) {
		String res = "";
		if (!flCapital) {
			res += "No capital letter;";
		};
		if (!flLower) {
			res += "No lower letter";
		}
		if (!flDigit) {
			res += "No digit";
		}
		return res;
	}

}