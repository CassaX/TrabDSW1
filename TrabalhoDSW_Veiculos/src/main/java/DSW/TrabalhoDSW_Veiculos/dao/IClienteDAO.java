package DSW.TrabalhoDSW_Veiculos.dao;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;

@SuppressWarnings("unchecked")
public interface IClienteDAO extends CrudRepository<Cliente, Long> {
	
	Cliente findById(long id);

	Cliente findByCPF(String CPF);

    @SuppressWarnings("null")
	@Override
	List<Cliente> findAll();
	
	@SuppressWarnings("null")
	@Override
	Cliente save(Cliente cliente);

	@Override
	void deleteById(Long id);
	
    Cliente findByEmail(String email);

	@Query("SELECT COUNT(p) > 0 FROM Proposta p WHERE p.cliente.id = :clienteId AND p.status = 'ABERTO'")
	boolean existsPropostasAbertas(@Param("clienteId") Long clienteId);

}