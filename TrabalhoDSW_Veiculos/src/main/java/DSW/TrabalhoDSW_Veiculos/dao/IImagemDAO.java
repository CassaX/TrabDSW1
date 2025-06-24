package DSW.TrabalhoDSW_Veiculos.dao;

import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IImagemDAO extends JpaRepository<Imagem, Long> {
    List<Imagem> findByVeiculoId(Long idVeiculo);
}