package telran.spring.security.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;

import jakarta.validation.constraints.*;
import lombok.*;
@Data
//@AllArgsConstructor
//@RequiredArgsConstructor
//@Builder
//@NoArgsConstructor
public class Account implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 5, message = "Username must be not less than 5 letters")
	final String username;
	@Size(min = 8, message = "Password must be not less than 8 letters")
	final String password;
	@NotEmpty
	final String[] roles;
	LocalDateTime expDate;
	LinkedList<String> passwords;

//	public Account(String username, String password, String[] roles) {
//		this.username = username;
//		this.password = password;
//		this.roles = roles;
//	}
}