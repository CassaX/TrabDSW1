package DSW.TrabalhoDSW_Veiculos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Imagem")
public class Imagem extends AbstractEntity<Long> {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipo;

    @Lob // Usado para grandes objetos binários
    @Column(nullable = false, columnDefinition = "LONGBLOB") // Adicionado columnDefinition para garantir tipo adequado no DB
    private byte[] dados;

    @ManyToOne(fetch = FetchType.LAZY) // Usar LAZY para evitar carregar todas as imagens com o Veículo
    @JoinColumn(name = "veiculo_id", nullable = false)
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

}
