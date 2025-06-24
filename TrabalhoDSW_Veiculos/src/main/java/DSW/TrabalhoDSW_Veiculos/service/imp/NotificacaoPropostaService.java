package DSW.TrabalhoDSW_Veiculos.service.imp;

import java.time.format.DateTimeFormatter; // Importe esta anotação

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import jakarta.mail.internet.InternetAddress;

@Service
public class NotificacaoPropostaService {

    @Autowired
    private EmailService emailService;

    public void notificarProposta(Proposta proposta, boolean notificarCliente) {
        try {
            String assunto = "Atualização da proposta para o carro " + proposta.getVeiculo().getModelo();

            String corpo = "Olá " + (notificarCliente ? proposta.getCliente().getNome() : proposta.getVeiculo().getLoja().getNome()) + ",\n\n" +
                    "Sua proposta para o carro \"" + proposta.getVeiculo().getModelo() + "\" (valor proposto: R$ " +
                    String.format("%.2f", proposta.getValor()) + ") foi atualizada para o status: " + proposta.getStatus() + ".\n\n";

            if ("NÃO ACEITO".equals(proposta.getStatus())) {
                if (proposta.getContrapropostaValor() != null) {
                    corpo += "Nossa contraproposta é de R$ " + String.format("%.2f", proposta.getContrapropostaValor()) + ".\n";
                }
                if (proposta.getContrapropostaCondicoes() != null && !proposta.getContrapropostaCondicoes().isEmpty()) {
                    corpo += "Condições da contraproposta: " + proposta.getContrapropostaCondicoes() + ".\n";
                }
            } else if ("ACEITO".equals(proposta.getStatus())) {
                if (proposta.getHorarioReuniao() != null) {
                    corpo += "Gostaríamos de agendar uma reunião para " + proposta.getHorarioReuniao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")) + ".\n";
                }
                if (proposta.getLinkReuniao() != null && !proposta.getLinkReuniao().isEmpty()) {
                    corpo += "Link da reunião: " + proposta.getLinkReuniao() + ".\n";
                }
            }

            corpo += "\nAtenciosamente,\nEquipe de Propostas";

            InternetAddress from = new InternetAddress("trabalhodswgrupo@gmail.com");
            InternetAddress to = new InternetAddress(
                notificarCliente ? proposta.getCliente().getEmail() : proposta.getVeiculo().getLoja().getEmail()
            );

            emailService.send(from, to, assunto, corpo);

        } catch (Exception e) {
            System.out.println("Erro ao enviar e-mail de notificação:");
            e.printStackTrace();
        }
    }
}