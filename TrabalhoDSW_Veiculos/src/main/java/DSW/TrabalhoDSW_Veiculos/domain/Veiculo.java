package DSW.TrabalhoDSW_Veiculos.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@SuppressWarnings("serial")
@Entity
@Table(name = "Veiculo")
public class Veiculo extends AbstractEntity<Long> {


	@NotBlank(message = "{NotBlank.veiculo.placa}")
    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @NotBlank(message = "{NotBlank.veiculo.modelo}")
    @Size(max = 50, message = "{Size.veiculo.modelo}")
    @Column(nullable = false, unique = false, length = 50)
    private String modelo;

    @NotBlank(message = "{NotBlank.veiculo.chassi}")
    @Size(max = 50, message = "{Size.veiculo.chassi}")
    @Column(nullable = false, unique = true, length = 50)
    private String chassi;

    @NotBlank(message = "{NotBlank.veiculo.ano}")
    @Size(min = 4, max = 4, message = "{Size.veiculo.ano}") 
    private String ano;

    @NotNull(message = "{NotNull.veiculo.quilometragem}")
    @Min(value = 0, message = "{Min.veiculo.quilometragem}")
    @Column(nullable = false)
    private int quilometragem;

    @NotBlank(message = "{NotBlank.veiculo.descricao}")
    @Size(max = 300, message = "{Size.veiculo.descricao}")
    @Column(nullable = false, length = 300)
    private String descricao;

    @NotNull(message = "{NotNull.veiculo.valor}")
    @Min(value = 0, message = "{Min.veiculo.valor}")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "{NotNull.veiculo.loja}") 
    @ManyToOne
    @JoinColumn(name = "loja_id")
    private Loja loja;

	@OneToMany(mappedBy = "veiculo")
	private List<Imagem> fotos = new ArrayList<>();

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getChassi() {
		return chassi;
	}

	public void setChassi(String chassi) {
		this.chassi = chassi;
	}

	public String getAno() {
		return ano;
	}

	public void setAno(String ano) {
		this.ano = ano;
	}

	public int getQuilometragem() {
		return quilometragem;
	}

	public void setQuilometragem(int quilometragem) {
		this.quilometragem = quilometragem;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}


	public Loja getLoja() {
		return loja;
	}

	public void setLoja(Loja loja) {
		this.loja = loja;
	}

	public List<Imagem> getFotos() {
		return fotos;
	}

	public void setFotos(List<Imagem> fotos) {
		this.fotos = fotos;
	}

	public void addFoto(Imagem foto) {
        fotos.add(foto);
        foto.setVeiculo(this);
    }

    public void removeFoto(Imagem foto) {
        fotos.remove(foto);
        foto.setVeiculo(null);
    }

	
}