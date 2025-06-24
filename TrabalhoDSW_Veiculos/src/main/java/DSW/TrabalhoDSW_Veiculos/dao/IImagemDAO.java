package DSW.TrabalhoDSW_Veiculos.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

public interface IImagemDAO extends CrudRepository<Imagem, Long> {
    List<Imagem> findByVeiculo(Veiculo veiculo);
    Imagem findById(long id);
}
