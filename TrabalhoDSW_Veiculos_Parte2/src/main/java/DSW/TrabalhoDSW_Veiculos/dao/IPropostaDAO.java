package DSW.TrabalhoDSW_Veiculos.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

public interface IPropostaDAO extends CrudRepository<Proposta, Long> {

    boolean existsByClienteIdAndVeiculoIdAndStatusIn(Long idCliente, Long idVeiculo, List<StatusProposta> status);

    List<Proposta> findAllByCliente(Cliente cliente);

    Optional<Proposta> findByCliente(Cliente cliente);

    List<Proposta> findAllByVeiculo_Loja(Loja loja);

    List<Proposta> findAllByVeiculo(Veiculo veiculo);
}