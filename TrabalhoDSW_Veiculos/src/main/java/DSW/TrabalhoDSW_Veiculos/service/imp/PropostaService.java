package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar o enum

import DSW.TrabalhoDSW_Veiculos.dao.IPropostaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja; // Adicionar para findById
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;

@Service
@Transactional(readOnly = false)
public class PropostaService implements IPropostaService {

    @Autowired
    IPropostaDAO dao;

    @Transactional(readOnly = true)
    public Proposta buscarPorId(Long id) {
        return dao.findById(id).orElse(null); // Usar .orElse(null) para lidar com Optional
    }

    @Transactional(readOnly = true)
    public List<Proposta> buscarTodosPorCliente(Cliente c) {
        return dao.findAllByCliente(c);
    }

    public void salvar(Proposta proposta) {
        dao.save(proposta);
    }

    // AQUI: Modificado para incluir AGUARDANDO_RESPOSTA_CLIENTE na verificação de "proposta em aberto"
    @Transactional(readOnly = true)
    public boolean existePropostaAberta(Long idCliente, Long idVeiculo){
        return dao.existsByClienteIdAndVeiculoIdAndStatusIn( idCliente, idVeiculo,
            List.of(StatusProposta.ABERTO, StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE));
    }
    
    // Este método, se você realmente precisa dele, deve retornar Optional
    @Transactional(readOnly = true)
    public Optional<Proposta> buscarPorCliente(Cliente cliente){
        return dao.findByCliente(cliente);
    }

    @Transactional(readOnly = true)
    public List<Proposta> buscarTodosPorLoja(Loja loja) {
        return dao.findAllByVeiculo_Loja(loja); 
    }
    
}