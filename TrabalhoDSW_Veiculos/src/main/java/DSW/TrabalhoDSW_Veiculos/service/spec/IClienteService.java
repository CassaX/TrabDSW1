package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;

public interface IClienteService {

    Cliente buscarPorId(Long id);

    List<Cliente> buscarTodos();

    void salvar(Cliente editora);

    void excluir(Long id);
}
