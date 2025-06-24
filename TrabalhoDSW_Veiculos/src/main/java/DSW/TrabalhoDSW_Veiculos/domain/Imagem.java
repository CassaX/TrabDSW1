package DSW.TrabalhoDSW_Veiculos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "Imagem")
public class Imagem extends AbstractEntity<Long> {

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private byte[] dados;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    // Getters e Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public byte[] getDados() {
        return dados;
    }

    public void setDados(byte[] dados) {
        this.dados = dados;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public boolean isImagem() {
        return tipo != null && tipo.startsWith("image");
    }
}
