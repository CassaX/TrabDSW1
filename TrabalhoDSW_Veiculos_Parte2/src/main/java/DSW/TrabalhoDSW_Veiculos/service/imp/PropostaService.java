package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import DSW.TrabalhoDSW_Veiculos.dao.IPropostaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja; 
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

@Service
@Transactional(readOnly = false)
public class PropostaService implements IPropostaService {

    @Autowired
    IPropostaDAO dao;

    @Transactional(readOnly = true)
    public Proposta buscarPorId(Long id) {
        return dao.findById(id).orElse(null); 
    }

    @Transactional(readOnly = true)
    public List<Proposta> buscarTodosPorCliente(Cliente c) {
        return dao.findAllByCliente(c);
    }

    public void salvar(Proposta proposta) {
        dao.save(proposta);
    }

    @Transactional(readOnly = true)
    public boolean existePropostaAberta(Long idCliente, Long idVeiculo){
        return dao.existsByClienteIdAndVeiculoIdAndStatusIn( idCliente, idVeiculo,
            List.of(StatusProposta.ABERTO, StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE));
    }
    
    @Transactional(readOnly = true)
    public List<Proposta> buscarPorCliente(Cliente cliente){
        return dao.findByCliente(cliente);
    }

    @Transactional(readOnly = true)
    public List<Proposta> buscarTodosPorLoja(Loja loja) {
        return dao.findAllByVeiculo_Loja(loja); 
    }

    @Transactional(readOnly = true)
    public List<Proposta> buscarPorVeiculo(Veiculo veiculo) {
        return dao.findAllByVeiculo(veiculo);
    }
    
}