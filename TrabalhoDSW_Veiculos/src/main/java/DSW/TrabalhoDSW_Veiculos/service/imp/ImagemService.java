package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import DSW.TrabalhoDSW_Veiculos.dao.IImagemDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.IImagemService;

@Service
public class ImagemService implements IImagemService {

    @Autowired
    private IImagemDAO dao;

    @Override
    public Imagem salvar(Imagem imagem) {
        return dao.save(imagem);
    }

    @Override
    public Imagem buscarPorId(Long id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public List<Imagem> buscarPorVeiculo(Veiculo veiculo) {
        return dao.findByVeiculo(veiculo);
    }

    @Override
    public void excluir(Long id) {
        dao.deleteById(id);
    }
}
