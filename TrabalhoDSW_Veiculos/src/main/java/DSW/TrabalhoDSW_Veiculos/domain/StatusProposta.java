package DSW.TrabalhoDSW_Veiculos.domain;

public enum StatusProposta {
    ABERTO("Aguardando Resposta da Loja"),
    ACEITO("Aceito"),
    NAO_ACEITO("Não Aceito"), // Adicionando esta opção ao enum
    AGUARDANDO_RESPOSTA_CLIENTE("Aguardando Resposta do Cliente"),
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