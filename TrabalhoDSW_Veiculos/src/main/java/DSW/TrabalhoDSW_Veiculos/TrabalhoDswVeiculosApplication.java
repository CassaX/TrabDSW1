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
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;

@SpringBootApplication
public class TrabalhoDswVeiculosApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrabalhoDswVeiculosApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(IClienteDAO clienteDAO, BCryptPasswordEncoder encoder, ILojaDAO lojaDAO, IVeiculoDAO veiculoDAO) {
		return (args) -> {
			
			Cliente u1 = new Cliente();
			u1.setEmail("nathalia@email.com");
			u1.setSenha(encoder.encode("123"));
			u1.setCPF("012.345.678-90");
			u1.setNome("Nathalia");
			u1.setTelefone("(14) 99800-8495");
			u1.setDataNascimento(LocalDate.of(2003, 2, 12));
			u1.setSexo("Feminino");
			u1.setRole("CLIENTE");
			u1.setEnabled(true);
			clienteDAO.save(u1);
			
			Cliente u2 = new Cliente();
			u2.setEmail("matheuscassatti@hotmail.com");
			u2.setSenha(encoder.encode("123"));
			u2.setCPF("032.345.678-90");
			u2.setNome("Matheus");
			u2.setTelefone("(16) 99731-8013");
			u2.setDataNascimento(LocalDate.of(2000, 1, 30));
			u2.setSexo("Masculino");
			u2.setRole("CLIENTE");
			u2.setEnabled(true);
			clienteDAO.save(u2);
			
						
			Loja l1 = new Loja();
			l1.setEmail("loja@email.com");
			l1.setSenha(encoder.encode("123"));
			l1.setCNPJ("71.150.470/0001-40");
			l1.setNome("Stefani Motors");
			l1.setDescricao("Loja especializada em veículos novos e seminovos, com garantia de qualidade.");
			l1.setRole("LOJA");
			l1.setEnabled(true);
			lojaDAO.save(l1);

			Loja l2 = new Loja();
			l2.setEmail("matheuscassatti2000@gmail.com");
			l2.setSenha(encoder.encode("123"));
			l2.setCNPJ("74.354.470/0001-40");
			l2.setNome("GP Veículos");
			l2.setDescricao("Loja especializada em veículos novos e seminovos, com garantia de qualidade.");
			l2.setRole("LOJA");
			l2.setEnabled(true);
			lojaDAO.save(l2);

			Veiculo v1= new Veiculo();
			v1.setPlaca("ABC-1234");
			v1.setModelo("Ford Fiesta");
			v1.setChassi("123B555GHSJ818567");
			v1.setAno("2018");
			v1.setQuilometragem(50000);
			v1.setDescricao("Veículo em ótimo estado, com manutenção em dia.");
			v1.setValor(new BigDecimal(45000.00));
			v1.setLoja(l1);
			veiculoDAO.save(v1);

			Veiculo v2 = new Veiculo();
			v2.setPlaca("XYZ-5678");
			v2.setModelo("Chevrolet Onix");
			v2.setChassi("123B555GHSJ818569");
			v2.setAno("2020");
			v2.setQuilometragem(30000);
			v2.setDescricao("Veículo novo, com garantia de fábrica.");
			v2.setValor(new BigDecimal(55000.00));
			v2.setLoja(l2);
			veiculoDAO.save(v2);

		};
	}

}
