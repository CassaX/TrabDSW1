package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import DSW.TrabalhoDSW_Veiculos.dao.IUsuarioDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Usuario;
import DSW.TrabalhoDSW_Veiculos.service.spec.IUsuarioService;

@Service
@Transactional(readOnly = false)
public class UsuarioService implements IUsuarioService {

	@Autowired
	IUsuarioDAO dao;

	public void salvar(Usuario usuario) {
		dao.save(usuario);
	}

	public void excluir(Long id) {
		dao.deleteById(id);
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorId(Long id) {
		return dao.findById(id.longValue());
	}

	@Transactional(readOnly = true)
	public List<Usuario> buscarTodos() {
		return dao.findAll();
	}
}