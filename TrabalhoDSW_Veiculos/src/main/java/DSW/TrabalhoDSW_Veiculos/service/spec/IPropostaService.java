package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;

public interface IPropostaService {

    Proposta buscarPorId(Long id);

    List<Proposta> buscarTodosPorUsuario(Cliente c);

    void salvar(Proposta editora);

}
