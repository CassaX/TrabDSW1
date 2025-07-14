package DSW.TrabalhoDSW_Veiculos;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.dao.IImagemDAO;
import DSW.TrabalhoDSW_Veiculos.dao.ILojaDAO;
import DSW.TrabalhoDSW_Veiculos.dao.IPropostaDAO;
import DSW.TrabalhoDSW_Veiculos.dao.IVeiculoDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@SpringBootApplication
public class TrabalhoDswVeiculosApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrabalhoDswVeiculosApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(IClienteDAO clienteDAO, BCryptPasswordEncoder encoder, ILojaDAO lojaDAO, IVeiculoDAO veiculoDAO, IPropostaDAO propostaDAO, IImagemDAO imagemDAO) {
		return (args) -> {
			
			Cliente u1 = new Cliente();
			u1.setEmail("nathalia@email.com");
			u1.setSenha(encoder.encode("123"));
			u1.setCPF("012.345.678-90");
			u1.setNome("Nathalia");
			u1.setTelefone("(14) 99800-8495");
			u1.setDataNascimento(LocalDate.of(2003, 2, 12));
			u1.setSexo("FEMININO");
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
			u2.setSexo("MASCULINO");
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
			v1.setFotos(null);
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

			Proposta p1 = new Proposta();
			p1.setData("LocalDateTime.now()");		
			p1.setValor(new BigDecimal(45000.00));
			p1.setVeiculo(v1);
			p1.setCliente(u1);
			p1.setStatus(StatusProposta.ABERTO);
			p1.setCondicoesPagamento("10x no cartão");
			propostaDAO.save(p1);

			Proposta p2 = new Proposta();
			p2.setData("LocalDateTime.now()");		
			p2.setValor(new BigDecimal(47000.00));
			p2.setVeiculo(v2);
			p2.setCliente(u2);
			p2.setStatus(StatusProposta.ABERTO);
			p2.setCondicoesPagamento("12x no cartão");
			propostaDAO.save(p2);

			// Adicionando imagens aos veículos
            try {
                // Caminho da imagem do Fiesta
                Path pathFiesta = Paths.get("src/main/resources/static/image/fiesta.jpg");
                Imagem imgFiesta = new Imagem(
                    "fiesta.jpg",
                    Files.probeContentType(pathFiesta),
                    Files.readAllBytes(pathFiesta)
                );
                imgFiesta.setVeiculo(v1);
                imagemDAO.save(imgFiesta);

                // Caminho da imagem do Onix
                Path pathOnix = Paths.get("src/main/resources/static/image/onix.JPG");
                Imagem imgOnix = new Imagem(
                    "onix.JPG",
                    Files.probeContentType(pathOnix),
                    Files.readAllBytes(pathOnix)
                );
                imgOnix.setVeiculo(v2);
                imagemDAO.save(imgOnix);

            } catch (IOException e) {
                System.err.println("Erro ao carregar imagens: " + e.getMessage());
                e.printStackTrace();
            }

		};
	}

}
