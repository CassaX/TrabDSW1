package DSW.TrabalhoDSW_Veiculos.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;

@Component
public class AdminConfig implements ApplicationRunner {

    @Autowired
    private IClienteDAO clienteDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (clienteDAO.findByEmail("admin@veiculos.com") == null) {
            Cliente admin = new Cliente();
            admin.setEmail("admin@veiculos.com");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setNome("Administrador");
            admin.setTelefone("00000000000");
            admin.setDataNascimento(LocalDate.now());
            admin.setEnabled(true);
            admin.setCPF("11111111111");
            admin.setSexo("sexo");
            clienteDAO.save(admin);
            System.out.println("Usuário admin criado com sucesso!");
        }
    }
}