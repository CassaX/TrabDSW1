package DSW.TrabalhoDSW_Veiculos.service.spec;

import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import java.util.List;

public interface IImagemService {
    void salvar(Imagem imagem);
    void excluir(Long id);
    Imagem buscarPorId(Long id);
    List<Imagem> buscarPorVeiculo(Long idVeiculo); // Método útil para carregar todas as imagens de um veículo
}