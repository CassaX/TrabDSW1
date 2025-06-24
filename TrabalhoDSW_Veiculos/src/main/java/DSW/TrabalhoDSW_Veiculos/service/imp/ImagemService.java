package DSW.TrabalhoDSW_Veiculos.service.imp;

import DSW.TrabalhoDSW_Veiculos.dao.IImagemDAO; // Você precisará criar esta interface
import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.service.spec.IImagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = false)
public class ImagemService implements IImagemService {

    @Autowired
    private IImagemDAO dao; // Injetar seu DAO/Repository para Imagem

    @Override
    public void salvar(Imagem imagem) {
        dao.save(imagem);
    }

    @Override
    public void excluir(Long id) {
        dao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Imagem buscarPorId(Long id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagem> buscarPorVeiculo(Long idVeiculo) {
        return dao.findByVeiculoId(idVeiculo); // Você precisará deste método em IImagemDAO
    }
}