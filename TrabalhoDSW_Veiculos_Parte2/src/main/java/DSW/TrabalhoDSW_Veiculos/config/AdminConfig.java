package DSW.TrabalhoDSW_Veiculos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import DSW.TrabalhoDSW_Veiculos.dao.IUsuarioDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Usuario;


@Component
public class AdminConfig implements ApplicationRunner {

    @Autowired
    private IUsuarioDAO usuarioDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (usuarioDAO.getUserByEmail("admin@veiculos.com") == null) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@veiculos.com");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setNome("Administrador");
            admin.setEnabled(true);
            usuarioDAO.save(admin);
            System.out.println("Usuário admin criado com sucesso!");
        }
    }
}