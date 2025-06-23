package DSW.TrabalhoDSW_Veiculos.dao;


import java.util.List;

import org.springframework.data.repository.CrudRepository;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;

public interface ILojaDAO extends CrudRepository<Loja, Long>{
    @Override
    void deleteById(Long id);

    Loja findById(long id);

    Loja findByCNPJ(String CNPJ);

    @SuppressWarnings("unchecked")
    @Override
    Loja save(Loja loja);

    Loja findByEmail(String email);

    @Override
    List<Loja> findAll();
}