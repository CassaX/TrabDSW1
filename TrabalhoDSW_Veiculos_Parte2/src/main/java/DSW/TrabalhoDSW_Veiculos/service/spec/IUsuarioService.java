package DSW.TrabalhoDSW_Veiculos.service.spec;

import java.util.List;

import DSW.TrabalhoDSW_Veiculos.domain.Usuario;

public interface IUsuarioService {

    Usuario buscarPorId(Long id);

	List<Usuario> buscarTodos();

	void salvar(Usuario editora);

	void excluir(Long id);	
}
