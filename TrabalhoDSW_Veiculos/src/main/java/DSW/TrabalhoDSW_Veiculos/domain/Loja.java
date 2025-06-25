package DSW.TrabalhoDSW_Veiculos.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; 

@SuppressWarnings("serial")
@Entity
@Table(name = "Loja")
public class Loja extends AbstractEntity<Long> {


    @NotBlank(message = "{NotBlank.loja.CNPJ}") 
    @Size(min = 18, max = 18, message = "{Size.loja.CNPJ}")
    @Column(nullable = false, unique = true, length = 60)
    private String CNPJ;

    @NotBlank(message = "{NotBlank.loja.nome}")
    @Size(min = 3, max = 60, message = "{Size.loja.nome}")
    @Column(nullable = false, unique = true, length = 60)
    private String nome;


    @Column(nullable = false, length = 10)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Size(max = 500, message = "{Size.loja.descricao}") 
    @Column(nullable = true, length = 500) 
    private String descricao;

    
    @NotBlank(message = "{NotBlank.loja.email}")
    @Column(nullable = false, length = 100, unique = true)
    private String email;


    @Column(nullable = false, length = 64) 
    private String senha;

    @JsonManagedReference("loja-veiculos")
    @OneToMany(mappedBy = "loja")
    private List<Veiculo> veiculos;

    public void setCNPJ(String CNPJ) {
        this.CNPJ = CNPJ;
    }

    public String getCNPJ() {
        return CNPJ;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
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

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}