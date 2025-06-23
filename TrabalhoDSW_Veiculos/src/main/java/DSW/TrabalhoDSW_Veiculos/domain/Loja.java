package DSW.TrabalhoDSW_Veiculos.domain;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.validation.UniqueCNPJ;
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

    @UniqueCNPJ(message = "{Unique.editora.CNPJ}")
    @NotBlank
    @Size(min = 18, max = 18, message = "{Size.editora.CNPJ}")
    @Column(nullable = false, unique = true, length = 60)
    private String CNPJ;

    @NotBlank
    @Size(min = 3, max = 60)
    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @NotBlank(message = "{NotBlank.loja.descricao}") 
    @Size(max = 500, message = "{Size.loja.descricao}") 
    @Column(nullable = true, length = 500) 
    private String descricao;

    

    @NotBlank
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 64)
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @OneToMany(mappedBy = "loja")
	private List<Veiculo> veiculos;

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