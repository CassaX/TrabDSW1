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
public class Cliente extends Usuario {

    
    @NotBlank(message = "{NotBlank.cliente.CPF}") 
    @Column(nullable = false, length = 14,unique = true)
    private String CPF;

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

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}