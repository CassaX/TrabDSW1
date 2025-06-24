package DSW.TrabalhoDSW_Veiculos.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType; // Importar EnumType
import jakarta.persistence.Enumerated; // Importar Enumerated
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern; // Para validar URL

@SuppressWarnings("serial")
@Entity
@Table(name = "Proposta")
public class Proposta extends AbstractEntity<Long> {

    @NotNull
    @Column(nullable = false, length = 19)
    private String data;
    
    @NotNull(message = "{NotNull.proposta.valor}")
    @Min(value = 0, message = "{Min.proposta.valor}")
    @Column(columnDefinition = "DECIMAL(8,2) DEFAULT 0.0")
    private BigDecimal valor; // Valor da proposta original
    
    @NotNull(message = "{NotNull.proposta.veiculo}")
    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @NotNull(message = "{NotNull.proposta.cliente}")
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // AQUI: Mudei o tipo de String para StatusProposta e adicionei @Enumerated
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable=false) // Aumente o tamanho da coluna para o nome do enum
    private StatusProposta status; // Alterado para StatusProposta

    @NotBlank(message = "{NotBlank.proposta.condicoesPagamento}")
    @Column(columnDefinition = "TEXT")
    private String condicoesPagamento;

    @Min(value = 0, message = "{Min.proposta.contrapropostaValor}") // Adicionei validação
    @Column(columnDefinition = "DECIMAL(8,2) DEFAULT 0.0")
    private BigDecimal contrapropostaValor; // Valor da contraproposta
    
    @Column(columnDefinition = "TEXT")
    private String contrapropostaCondicoes; // Condições da contraproposta

    private LocalDateTime horarioReuniao; // Horário e data da reunião
    
    @Pattern(regexp = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "{Pattern.proposta.linkReuniao}") // Adicionei validação de URL
    private String linkReuniao; // Link da reunião

    // Getters e Setters
    // Os getters e setters para 'status' devem ser atualizados para 'StatusProposta'
    public StatusProposta getStatus() {
        return status;
    }

    public void setStatus(StatusProposta status) {
        this.status = status;
    }

    // Mantenha os demais getters e setters inalterados
    public BigDecimal getContrapropostaValor() { return contrapropostaValor; }
    public void setContrapropostaValor(BigDecimal contrapropostaValor) { this.contrapropostaValor = contrapropostaValor; }
    public String getContrapropostaCondicoes() { return contrapropostaCondicoes; }
    public void setContrapropostaCondicoes(String contrapropostaCondicoes) { this.contrapropostaCondicoes = contrapropostaCondicoes; }
    public LocalDateTime getHorarioReuniao() { return horarioReuniao; }
    public void setHorarioReuniao(LocalDateTime horarioReuniao) { this.horarioReuniao = horarioReuniao; }
    public String getLinkReuniao() { return linkReuniao; }
    public void setLinkReuniao(String linkReuniao) { this.linkReuniao = linkReuniao; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getCondicoesPagamento() { return condicoesPagamento; }
    public void setCondicoesPagamento(String condicoesPagamento) { this.condicoesPagamento = condicoesPagamento; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}