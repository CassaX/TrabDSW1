package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;
import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

public interface IImagemService {
    Imagem salvar(Imagem imagem);
    Imagem buscarPorId(Long id);
    List<Imagem> buscarPorVeiculo(Veiculo veiculo);
    void excluir(Long id);
}
