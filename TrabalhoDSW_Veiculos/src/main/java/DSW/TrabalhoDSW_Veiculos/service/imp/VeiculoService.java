package DSW.TrabalhoDSW_Veiculos.service.imp;

import DSW.TrabalhoDSW_Veiculos.dao.IVeiculoDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class VeiculoService implements IVeiculoService {

    @Autowired
    IVeiculoDAO dao;

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(Long id) {
        return dao.findById(id.longValue());
    }

    @Transactional(readOnly = true)
    public List<Veiculo> buscarTodos() {
        return dao.findAll();
    }

    public void salvar(Veiculo veiculo) {
        dao.save(veiculo);
    }

    public void excluir(Long id) {
        dao.deleteById(id);
    }
}
