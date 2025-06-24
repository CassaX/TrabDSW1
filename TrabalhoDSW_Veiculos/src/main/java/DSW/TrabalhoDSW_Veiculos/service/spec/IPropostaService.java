package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;

public interface IPropostaService {

    Proposta buscarPorId(Long id);

    List<Proposta> buscarTodosPorCliente(Cliente c);

    void salvar(Proposta proposta);

    boolean existePropostaAberta(Long idCliente, Long idVeiculo);

    Object buscarPorCliente(Cliente cliente);

    List<Proposta> buscarTodosPorLoja(Loja loja);

}
