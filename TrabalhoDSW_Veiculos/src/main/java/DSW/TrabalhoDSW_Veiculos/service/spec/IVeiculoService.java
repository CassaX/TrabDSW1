package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

public interface IVeiculoService {

    Veiculo buscarPorId(Long id);

    List<Veiculo> buscarTodos();

    void salvar(Veiculo livro);

    void excluir(Long id);
}
