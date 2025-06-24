package DSW.TrabalhoDSW_Veiculos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@SuppressWarnings("serial")
@Entity
@Table(name = "Proposta")
public class Proposta extends AbstractEntity<Long> {

    @NotBlank 
    @Column(nullable = false, length = 19)
    private String data;
    
    @Min(value = 0, message = "{Min.proposta.valor}")
    @Column(columnDefinition = "DECIMAL(20,2) DEFAULT 0.0")
    private BigDecimal valor; // Valor da proposta original OU o valor da contraproposta aceita

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable=false)
    private StatusProposta status;

    @Column(columnDefinition = "TEXT")
    private String condicoesPagamento;

    @Min(value = 0, message = "{Min.proposta.contrapropostaValor}")
    @Column(columnDefinition = "DECIMAL(8,2) DEFAULT 0.0")
    private BigDecimal contrapropostaValor; 
    
    @Column(columnDefinition = "TEXT")
    private String contrapropostaCondicoes; 

    private LocalDateTime horarioReuniao; 
    
    
    private String linkReuniao;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public StatusProposta getStatus() {
		return status;
	}

	public void setStatus(StatusProposta status) {
		this.status = status;
	}

	public String getCondicoesPagamento() {
		return condicoesPagamento;
	}

	public void setCondicoesPagamento(String condicoesPagamento) {
		this.condicoesPagamento = condicoesPagamento;
	}

	public BigDecimal getContrapropostaValor() {
		return contrapropostaValor;
	}

	public void setContrapropostaValor(BigDecimal contrapropostaValor) {
		this.contrapropostaValor = contrapropostaValor;
	}

	public String getContrapropostaCondicoes() {
		return contrapropostaCondicoes;
	}

	public void setContrapropostaCondicoes(String contrapropostaCondicoes) {
		this.contrapropostaCondicoes = contrapropostaCondicoes;
	}

	public LocalDateTime getHorarioReuniao() {
		return horarioReuniao;
	}

	public void setHorarioReuniao(LocalDateTime horarioReuniao) {
		this.horarioReuniao = horarioReuniao;
	}

	public String getLinkReuniao() {
		return linkReuniao;
	}

	public void setLinkReuniao(String linkReuniao) {
		this.linkReuniao = linkReuniao;
	} 

    
}