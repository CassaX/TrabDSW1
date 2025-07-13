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
public class Loja extends Usuario {

    
    @NotBlank(message = "{NotBlank.loja.CNPJ}") 
    @Size(min = 18, max = 18, message = "{Size.loja.CNPJ}")
    @Column(nullable = false, unique = true, length = 60)
    private String CNPJ;


    @Size(max = 500, message = "{Size.loja.descricao}") 
    @Column(nullable = true, length = 500) 
    private String descricao;

    @JsonManagedReference("loja-veiculos")
    @OneToMany(mappedBy = "loja")
    private List<Veiculo> veiculos;

    public void setCNPJ(String CNPJ) {
        this.CNPJ = CNPJ;
    }

    public String getCNPJ() {
        return CNPJ;
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