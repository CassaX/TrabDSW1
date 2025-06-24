package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

public interface IVeiculoService {

    Veiculo buscarPorId(Long id);

    List<Veiculo> buscarTodos();

    void salvar(Veiculo veiculo);

    void excluir(Long id);

    List<Veiculo> buscarPorLoja(Loja lojaLogada);

    Veiculo buscarPorIdComFotos(Long id);

    List<Veiculo> buscarTodosComFotos();

    Object buscarPorLojaComFotos(Loja lojaLogada);
}
