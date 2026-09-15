package br.edu.ifsp.mef.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Turma {
	
	@Id
	@GeneratedValue
	private Long id;
	private String nome;
	@ManyToMany
	@JoinTable (
	  name = "turma_usuario", 
	  joinColumns = @JoinColumn(name = "idTurma"), 
	  inverseJoinColumns = @JoinColumn(name = "idUsuario"))
	private Set<Usuario> usuarios = new HashSet<>();
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public Set<Usuario> getUsuarios() {
	    return usuarios;
	}

	public void setUsuario(Set<Usuario> usuarios) {
	    this.usuarios = usuarios;
	}
	
	public void addUsuario(Usuario usuario) {
	    this.usuarios.add(usuario);
	    usuario.getTurmas().add(this);
	}

	public void removeUsuario(Usuario usuario) {
	    this.usuarios.remove(usuario);
	    usuario.getTurmas().remove(this);
	}
	
}
