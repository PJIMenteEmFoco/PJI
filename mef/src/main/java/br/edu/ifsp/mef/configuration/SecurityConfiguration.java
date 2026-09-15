package br.edu.ifsp.mef.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
	public SecurityFilterChain seguranca (HttpSecurity https) throws Exception {
		return https
				.authorizeHttpRequests((auth) -> auth
						.requestMatchers("/", "/login", "/cadastro", "/cadastrar", "/css/**", "/img/**").permitAll()
						.requestMatchers("/categoria/excluirContaPsico/**").hasAnyRole("PSICOPEDAGOGO", "ADMIN")
						.requestMatchers("/categoria/excluirContaProfessor/**").hasAnyRole("PROFESSOR", "ADMIN")
						.requestMatchers("/categoria/excluirContaAluno/**").hasAnyRole("ALUNO", "PSICOPEDAGOGO", "ADMIN")
						.anyRequest().authenticated())
				.formLogin(login -> login
						.loginPage("/login")
						.defaultSuccessUrl("/dashboard", true)
						.failureUrl("/login?error")
						.permitAll())
				.logout(sair -> sair
						.logoutSuccessUrl("/?logout")
						.permitAll())
				.build();
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
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
    
}