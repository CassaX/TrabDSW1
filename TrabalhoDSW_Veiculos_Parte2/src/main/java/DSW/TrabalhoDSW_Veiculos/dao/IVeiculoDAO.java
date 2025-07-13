package DSW.TrabalhoDSW_Veiculos.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

@SuppressWarnings("unchecked")
public interface IVeiculoDAO extends CrudRepository<Veiculo, Long> {
	
	@Query("SELECT DISTINCT v FROM Veiculo v " +
		"LEFT JOIN FETCH v.loja " +
		"LEFT JOIN FETCH v.fotos")
	List<Veiculo> findAllWithLojaAndFotos();

	@Query("SELECT DISTINCT v FROM Veiculo v " +
		"LEFT JOIN FETCH v.loja " +
		"LEFT JOIN FETCH v.fotos " +
		"WHERE LOWER(v.modelo) LIKE LOWER(CONCAT('%', :modelo, '%'))")
	List<Veiculo> findByModeloContainingIgnoreCaseWithLojaAndFotos(@Param("modelo") String modelo);

	@Query("SELECT v FROM Veiculo v LEFT JOIN FETCH v.fotos WHERE v.id = :id")
    Optional<Veiculo> findByIdWithFotos(@Param("id") Long id);

    @Query("SELECT DISTINCT v FROM Veiculo v LEFT JOIN FETCH v.fotos")
    List<Veiculo> findAllWithFotos();

    @Query("SELECT DISTINCT v FROM Veiculo v LEFT JOIN FETCH v.fotos WHERE v.loja = :loja")
    List<Veiculo> findByLojaWithFotos(@Param("loja") Loja loja);

	Veiculo findById(long id);

	@Override
	List<Veiculo> findAll();

	@Override
	Veiculo save(Veiculo veiculo);

	@Override
	void deleteById(Long id);

	List<Veiculo> findByLoja(Loja loja);

	List<Veiculo> findByModelo(String modelo);


}