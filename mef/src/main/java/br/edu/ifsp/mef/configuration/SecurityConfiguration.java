package br.edu.ifsp.mef.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.UsuarioRepository;

import java.security.SecureRandom;

import org.springframework.boot.CommandLineRunner;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	public SecurityFilterChain seguranca(HttpSecurity https) throws Exception {
		return https
				.authorizeHttpRequests((auth) -> auth
						.requestMatchers("/login", "/cadastro", "/cadastrar", "/css/**", "/img/**", "/error").permitAll()
						.requestMatchers("/categoria/excluirContaPsico/**").hasAnyRole("PSICOPEDAGOGO", "ADMIN")
						.requestMatchers("/categoria/excluirContaProfessor/**").hasAnyRole("PROFESSOR", "ADMIN")
						.requestMatchers("/categoria/excluirContaAluno/**")
						.hasAnyRole("ALUNO", "PSICOPEDAGOGO", "ADMIN")
						.anyRequest().authenticated())
				.formLogin(login -> login
						.loginPage("/login")
						.successHandler(authenticationSuccessHandler())
						.failureUrl("/login?error")
						.permitAll())
				.exceptionHandling(exception -> exception
					    .accessDeniedPage("/acesso-negado")
					)
				.logout(sair -> sair
						.logoutSuccessUrl("/?logout")
						.permitAll()).build();
	}

	@Bean
	CommandLineRunner criarAdmin(UsuarioRepository repoUsuario, PasswordEncoder encoder) {
		return args -> {
			if (repoUsuario.findByEmailIgnoreCase("mef@gmail.com").isEmpty()) {
				Usuario user = new Usuario();
				user.setEmail("mef@gmail.com");
				user.setNome("MEF");
				user.setSenha(encoder.encode("1234"));
				user.setTelefone("(11)555599999");
				user.setPerfil("ADMIN");
				user.setAtivado(true);
				repoUsuario.save(user);
			}
		};
	};

	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return (request, response, authentication) -> {
			Object principal = authentication.getPrincipal();
			String redirectUrl = "/dashboard";

			if (principal instanceof UsuarioDetails usuarioDetails) {
				String perfil = usuarioDetails.getUsuario().getPerfil();
				if ("ALUNO".equals(perfil)) {
					redirectUrl = "/aluno/dashboard";
				} else if ("PROFESSOR".equals(perfil)) {
					redirectUrl = "/professor/dashboard";
				}
			}

			response.sendRedirect(redirectUrl);
		};
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecureRandom secureRandom() {
	    return new SecureRandom();
	}

}