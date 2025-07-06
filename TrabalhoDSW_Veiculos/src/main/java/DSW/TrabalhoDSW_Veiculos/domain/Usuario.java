package DSW.TrabalhoDSW_Veiculos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.NotBlank;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario extends AbstractEntity<Long>{

    @NotBlank(message = "{NotBlank.email}") 
    @Column(nullable = false, length = 100, unique = true)
    private String email;
    
	@NotBlank
    @Column(nullable = false, length = 64)
    private String senha;
       
    @NotBlank(message = "{NotBlank.nome}")
    @Column(nullable = false, length = 60)
    private String nome;
    
    @Column(nullable = false, length = 10)
    private String role;
    
    @Column(nullable = false)
    private boolean enabled;
		
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getSenha() {
		return senha;
	}
	
	public void setSenha(String senha) {
		this.senha = senha;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
