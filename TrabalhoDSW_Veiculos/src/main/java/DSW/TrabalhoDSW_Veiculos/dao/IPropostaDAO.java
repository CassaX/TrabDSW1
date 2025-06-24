package DSW.TrabalhoDSW_Veiculos.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente; // Importar o enum
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta; // Para findByCliente
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;

public interface IPropostaDAO extends JpaRepository<Proposta, Long> {

    // AQUI: Usando "StatusIn" para buscar por múltiplos status do enum
    boolean existsByClienteIdAndVeiculoIdAndStatusIn(Long idCliente, Long idVeiculo, List<StatusProposta> status);

    List<Proposta> findAllByCliente(Cliente cliente);

    Optional<Proposta> findByCliente(Cliente cliente); // Retorna Optional

    List<Proposta> findAllByVeiculo_Loja(Loja loja);
}