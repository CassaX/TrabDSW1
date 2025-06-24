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

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.dao.ILojaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;

@Service
public class ClienteDetailsServiceimpl implements UserDetailsService {

    @Autowired
    private IClienteDAO clienteDAO;

    @Autowired
    private ILojaDAO lojaDAO;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteDAO.findByEmail(email);
        if (cliente != null) {
            return criarUserDetails(cliente.getEmail(), cliente.getSenha(), cliente.getRole(), cliente.isEnabled());
        }

        Loja loja = lojaDAO.findByEmail(email);
        if (loja != null) {
            return criarUserDetails(loja.getEmail(), loja.getSenha(), loja.getRole(), loja.isEnabled());
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }

    private UserDetails criarUserDetails(String username, String password, String role, boolean enabled) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role));
        return new ClienteDetails(username, password, authorities, enabled);
    }
}