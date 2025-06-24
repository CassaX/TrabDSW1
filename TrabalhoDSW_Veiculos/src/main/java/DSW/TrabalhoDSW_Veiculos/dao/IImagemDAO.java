package DSW.Veiculos.DAO;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import DSW.Veiculos.domain.Imagem;
import DSW.Veiculos.domain.Veiculo;

public interface IImagemDAO extends CrudRepository<Imagem, Long> {

    List<Imagem> findByVeiculo(Veiculo veiculo);
}