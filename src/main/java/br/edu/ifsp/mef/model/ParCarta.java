package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ParCarta {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "idJogo")
	private JogoMemoria jogoMemoria;

	private String cartaA;
	private String cartaB;

	public ParCarta() {}

	public ParCarta(String cartaA, String cartaB) {
		this.cartaA = cartaA;
		this.cartaB = cartaB;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public JogoMemoria getJogoMemoria() {
		return jogoMemoria;
	}

	public void setJogoMemoria(JogoMemoria jogoMemoria) {
		this.jogoMemoria = jogoMemoria;
	}

	public String getCartaA() {
		return cartaA;
	}

	public void setCartaA(String cartaA) {
		this.cartaA = cartaA;
	}

	public String getCartaB() {
		return cartaB;
	}

	public void setCartaB(String cartaB) {
		this.cartaB = cartaB;
	}
	
	
}
