package DSW.TrabalhoDSW_Veiculos.security;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import DSW.TrabalhoDSW_Veiculos.dao.IUsuarioDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Usuario;

@Service
public class UsuarioDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUsuarioDAO usuarioDao;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario cliente = usuarioDao.getUserByEmail(email);
        if (cliente != null) {
            return criarUserDetails(cliente.getEmail(), cliente.getSenha(), cliente.getRole(), cliente.isEnabled());
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }

    private UserDetails criarUserDetails(String username, String password, String role, boolean enabled) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role));
        return new UsuarioDetails(username, password, authorities, enabled);
    }
}