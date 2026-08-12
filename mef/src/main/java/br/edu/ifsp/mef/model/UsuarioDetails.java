package br.edu.ifsp.mef.model;

import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;
	@Autowired
	PasswordEncoder encoder;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNome() {
        return usuario.getNome();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority(
                "ROLE_" + usuario.getPerfil()
            )
        );
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public String getPrimeiroNome() {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            return "";
        }

        return usuario.getNome().trim().split("\\s+")[0];
    }
    
    public String getTelefone() {
        return usuario.getTelefone();
    }
    
    public String getEmail() {
        return usuario.getEmail();
    }

    public Long getId() {
        return usuario.getId();
    }	
    	
}