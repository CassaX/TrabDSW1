package DSW.TrabalhoDSW_Veiculos.domain;

public enum StatusProposta {
    ABERTO("Aguardando Resposta da Loja"),
    AGUARDANDO_RESPOSTA_CLIENTE("Aguardando Resposta do Cliente"),
    ACEITO("Aceito"),
    RECUSADO_LOJA("Recusado pela Loja"), 
    RECUSADO_CLIENTE("Recusado pelo Cliente");

    private String descricao;

    StatusProposta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}