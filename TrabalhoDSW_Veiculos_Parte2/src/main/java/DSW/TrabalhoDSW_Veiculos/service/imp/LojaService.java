package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import DSW.TrabalhoDSW_Veiculos.dao.ILojaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;

@Service
@Transactional(readOnly = false)
public class LojaService implements ILojaService {
    @Autowired
    ILojaDAO dao;

    @Transactional(readOnly = true)
    public Loja buscarPorId(Long id) {
        return dao.findById(id.longValue());
    }

    @Transactional(readOnly = true)
    public List<Loja> buscarTodos() {
        return dao.findAll();
    }

    public void salvar(Loja loja) {
        dao.save(loja);
    }

    public void excluir(Long id) {
        dao.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean LojaTemVeiculos(Long id) {
        return !dao.findById(id.longValue()).getVeiculos().isEmpty();
    }

    @Transactional(readOnly = true)
    public Loja buscarPorEmail(String email){
        return dao.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Loja buscarPorCNPJ(String cnpj){
        return dao.findByCNPJ(cnpj);
    }
}
