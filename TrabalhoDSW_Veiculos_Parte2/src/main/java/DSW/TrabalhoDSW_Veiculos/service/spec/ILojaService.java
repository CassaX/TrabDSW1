package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;

public interface ILojaService {

    Loja buscarPorId(Long id);

    List<Loja> buscarTodos();

    void salvar(Loja loja);

    void excluir(Long id);

    boolean LojaTemVeiculos(Long id);

    Loja buscarPorEmail(String email);

    Loja buscarPorCNPJ(String cnpj);


}
