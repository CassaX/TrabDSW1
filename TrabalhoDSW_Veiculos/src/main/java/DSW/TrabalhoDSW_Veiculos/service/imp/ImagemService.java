package DSW.Veiculos.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import DSW.Veiculos.DAO.IImagemDAO;
import DSW.Veiculos.domain.Imagem;
import DSW.Veiculos.service.spec.IImagemService;

@Service
public class ImagemService implements IImagemService {

    @Autowired
    private IImagemDAO imagemDAO;

    @Override
    public Imagem salvar(Imagem imagem) {
        return imagemDAO.save(imagem);
    }

    @Override
    public Imagem buscarPorId(Long id) {
        return imagemDAO.findById(id).orElse(null);
    }

    @Override
    public void excluir(Long id) {
        imagemDAO.deleteById(id);
    }
}