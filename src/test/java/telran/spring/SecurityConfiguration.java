package telran.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Спринг будет вызывать методы, внутри них будет строиться бин
public class SecurityConfiguration {
	
	@Bean // аннотация должна быть внутри класса с аннотацией @Configuration
	// будет зарегистрирован как бин и будет использоваться для конфигурации безопасности
	// Задаем свои Authorization rules (из схемы) вместо какой-то дефолтной из спринга
	// объект этого класса станет бином
	SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {

		return httpSecurity
		// отключает механизм CSRF (Cross-Site Request Forgery), который защищает приложение от атак на подделку запросов
				.csrf(custom -> custom.disable())
		// отключает конфигурацию CORS (Cross-Origin Resource Sharing), что позволяет контролировать доступ к ресурсам с разных источников
				.cors(custom -> custom.disable())
		// определяет, какие HTTP запросы требуют авторизации
				.authorizeHttpRequests(
				// все запросы разрешены без авторизации
//						custom -> custom.anyRequest().authenticated())
				// GET могут выполнить все аутентифицированные, остальные - только с ролью ADMIN
						custom -> custom.requestMatchers(HttpMethod.GET).authenticated().anyRequest().hasAnyRole("ADMIN"))
				// при повторном запросе не будет проверяться пароль
						.sessionManagement(custom -> custom.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
				// настраивает базовую HTTP аутентификацию
				// Customizer.withDefaults() - используется для настройки базовой аутентификации с настройками по умолчанию
				.httpBasic(Customizer.withDefaults())
				.build();
	} 
	
}
