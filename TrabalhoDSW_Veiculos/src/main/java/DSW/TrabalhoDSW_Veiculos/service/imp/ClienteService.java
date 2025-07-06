package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;

@Service
@Transactional(readOnly = false)
public class ClienteService implements IClienteService {
    @Autowired
    IClienteDAO dao;

    @Transactional(readOnly = true)
    public boolean existePropostasAbertas(Long id) {
        return dao.existsPropostasAbertas(id);
    }

    public void salvar(Cliente cliente) {
        dao.save(cliente);
    }

    public void excluir(Long id) {
        if(existePropostasAbertas(id)) {
            throw new RuntimeException("Cliente possui propostas abertas e não pode ser excluído.");
        }
        dao.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return dao.findById(id.longValue());
    }

    @Transactional(readOnly = true)
    public List<Cliente> buscarTodos() {
        return dao.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorEmail(String email) {
        return dao.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorCPF(String cpf){
        return dao.findByCPF(cpf);
    }

    
    
}
