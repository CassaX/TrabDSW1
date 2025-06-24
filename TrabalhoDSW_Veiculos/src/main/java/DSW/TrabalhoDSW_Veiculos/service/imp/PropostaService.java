package DSW.TrabalhoDSW_Veiculos.service.imp;

import DSW.TrabalhoDSW_Veiculos.dao.IPropostaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class PropostaService implements IPropostaService {

    @Autowired
    IPropostaDAO dao;

    @Transactional(readOnly = true)
    public Proposta buscarPorId(Long id) {
        return dao.findById(id.longValue());
    }

    @Transactional(readOnly = true)
    public List<Proposta> buscarTodosPorCliente(Cliente c) {
        return dao.findAllByCliente(c);
    }

    public void salvar(Proposta proposta) {
        dao.save(proposta);
    }

    public boolean existePropostaAberta(Long idCliente, Long idVeiculo){
        return dao.existsByClienteIdAndVeiculoIdAndStatus( idCliente, idVeiculo, "ABERTO");
    }
    

   public Object buscarPorCliente(Cliente cliente){
        return dao.findByCliente(cliente);
    }

    @Transactional(readOnly = true)
public List<Proposta> buscarTodosPorLoja(Loja loja) {
    return dao.findAllByVeiculo_Loja(loja); 
}
}
