package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import DSW.TrabalhoDSW_Veiculos.dao.IVeiculoDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;

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

    public  List<Veiculo> buscarPorLoja(Loja lojaLogada){
        return dao.findByLoja(lojaLogada);
    }

   
    @Transactional(readOnly = true)
    public Veiculo buscarPorIdComFotos(Long id) {
        return dao.findByIdWithFotos(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Veiculo> buscarTodosComFotos() {
        return dao.findAllWithFotos();
    }

    @Transactional(readOnly = true)
    public List<Veiculo> buscarPorLojaComFotos(Loja loja) {
        return dao.findByLojaWithFotos(loja);
    }
}
