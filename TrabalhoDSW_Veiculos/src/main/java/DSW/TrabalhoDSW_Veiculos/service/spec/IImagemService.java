package DSW.Veiculos.service.spec;

import DSW.Veiculos.domain.Imagem;

public interface IImagemService {

    Imagem salvar(Imagem imagem);

    Imagem buscarPorId(Long id);

    void excluir(Long id);
}