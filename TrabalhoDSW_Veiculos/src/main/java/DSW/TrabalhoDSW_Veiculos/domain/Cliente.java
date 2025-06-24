package DSW.TrabalhoDSW_Veiculos.domain;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; 


@SuppressWarnings("serial")
@Entity
@Table(name = "Cliente")
public class Cliente extends AbstractEntity<Long> {

    @NotBlank(message = "{NotBlank.cliente.email}") 
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 64)
    private String senha;


    @NotBlank(message = "{NotBlank.cliente.CPF}") 
    @Column(nullable = false, length = 14)
    private String CPF;

    @NotBlank(message = "{NotBlank.cliente.nome}")
    @Column(nullable = false, length = 14)
    private String nome;

    @NotBlank(message = "{NotBlank.cliente.telefone}")
    @Column(nullable = false, length = 15)
    private String telefone;

    @NotBlank(message = "{NotBlank.cliente.sexo}")
    @Column(nullable = false, length = 10)
    private String sexo;

    @NotNull(message = "{NotNull.cliente.dataNascimento}") 
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(nullable = false, length = 10)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 10) 
    private String role;

    @Column(nullable = false)
    private boolean enabled; 
    // --- Getters e Setters ---
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

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}