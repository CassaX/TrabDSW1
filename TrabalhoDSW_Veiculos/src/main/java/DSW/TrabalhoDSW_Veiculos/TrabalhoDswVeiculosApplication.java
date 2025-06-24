package DSW.TrabalhoDSW_Veiculos;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.dao.ILojaDAO;
import DSW.TrabalhoDSW_Veiculos.dao.IVeiculoDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;

@SpringBootApplication
public class TrabalhoDswVeiculosApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrabalhoDswVeiculosApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(IClienteDAO clienteDAO, BCryptPasswordEncoder encoder, ILojaDAO lojaDAO, IVeiculoDAO veiculoDAO) {
		return (args) -> {
			
			Cliente u1 = new Cliente();
			u1.setEmail("nath@email.com");
			u1.setSenha(encoder.encode("123"));
			u1.setCPF("012.345.678-90");
			u1.setNome("Nathalia");
			u1.setTelefone("telefone");
			u1.setDataNascimento(LocalDate.now());
			u1.setSexo("Masculino");
			u1.setRole("CLIENTE");
			u1.setEnabled(true);
			clienteDAO.save(u1);
			
			Cliente u2 = new Cliente();
			u2.setEmail("matheuscassatti@hotmail.com");
			u2.setSenha(encoder.encode("123"));
			u2.setCPF("032.345.678-90");
			u2.setNome("Matheus");
			u2.setTelefone("telefone");
			u2.setDataNascimento(LocalDate.now());
			u2.setSexo("Masculino");
			u2.setRole("CLIENTE");
			u2.setEnabled(true);
			clienteDAO.save(u2);
			
			Cliente u3 = new Cliente();
			u3.setEmail("matheuscassatti@estudante.ufscar.br");
			u3.setSenha(encoder.encode("123"));
			u3.setCPF("112.345.678-90");
			u3.setTelefone("telefone");
			u3.setDataNascimento(LocalDate.now());
			u3.setSexo("Masculino");
			u3.setNome("Pedro");
			u3.setRole("CLIENTE");
			u3.setEnabled(true);
			clienteDAO.save(u3);
			
			Loja l1 = new Loja();
			l1.setEmail("loja@email.com");
			l1.setSenha(encoder.encode("123"));
			l1.setCNPJ("71.150.470/0001-40");
			l1.setNome("Loja");
			l1.setRole("LOJA");
			l1.setEnabled(true);
			lojaDAO.save(l1);

			Loja l2 = new Loja();
			l2.setEmail("matheuscassatti2000@gmail.com");
			l2.setSenha(encoder.encode("123"));
			l2.setCNPJ("74.354.470/0001-40");
			l2.setNome("Loja2");
			l2.setRole("LOJA");
			l2.setEnabled(true);
			lojaDAO.save(l2);
		};
	}

}
