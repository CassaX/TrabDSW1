package DSW.TrabalhoDSW_Veiculos.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/propostas")
public class PropostaController {

    @Autowired
    private IPropostaService propostaService;

    @Autowired
    private IVeiculoService veiculoService;

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private ILojaService lojaService;

    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return clienteService.buscarPorEmail(email);
    }

    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return lojaService.buscarPorEmail(email);
    }

    @GetMapping("/cadastrar/{id}")
    public String cadastrar(@PathVariable("id") Long idVeiculo,
                            Proposta proposta,
                            ModelMap model) {

        Veiculo veiculo = veiculoService.buscarPorIdComFotos(idVeiculo);
        if(veiculo == null) {
            model.addAttribute("fail", "Veículo não encontrado.");
            return "redirect:/veiculos/listar";
        }

        proposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        proposta.setVeiculo(veiculo);
        proposta.setCliente(getClienteLogado());

        model.addAttribute("proposta", proposta);
        model.addAttribute("veiculo", veiculo);

        return "proposta/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid Proposta proposta,
                         BindingResult result,
                         RedirectAttributes attr,
                         ModelMap model) {

        if(result.hasErrors()) {
            Veiculo veiculo = null;
            Cliente cliente = null;

            if (proposta.getVeiculo() != null && proposta.getVeiculo().getId() != null) {
                veiculo = veiculoService.buscarPorIdComFotos(proposta.getVeiculo().getId());
                model.addAttribute("veiculo", veiculo);
            } else {
                System.err.println("Erro: ID do veículo não encontrado na proposta submetida durante erro de validação.");
            }

            if (proposta.getCliente() != null && proposta.getCliente().getId() != null) {
                cliente = clienteService.buscarPorId(proposta.getCliente().getId());
            } else {
                 System.err.println("Erro: ID do cliente não encontrado na proposta submetida durante erro de validação.");
            }

            if (veiculo == null || cliente == null) {
                attr.addFlashAttribute("fail", "Erro ao processar a proposta. Cliente ou Veículo não identificado.");
                return "redirect:/veiculos/listar";
            }
            return "proposta/cadastro";
        }

        Cliente clienteLogado = getClienteLogado();
        Veiculo veiculoDaProposta = veiculoService.buscarPorId(proposta.getVeiculo().getId());

        proposta.setCliente(clienteLogado);
        proposta.setVeiculo(veiculoDaProposta);

        if(propostaService.existePropostaAberta(
            proposta.getCliente().getId(),
            proposta.getVeiculo().getId())) {

            attr.addFlashAttribute("fail", "Você já tem uma proposta em aberto para este veículo");
            return "redirect:/veiculos/listar";
        }

        proposta.setStatus("ABERTO");

        propostaService.salvar(proposta);
        attr.addFlashAttribute("success", "Proposta enviada com sucesso!");
        return "redirect:/propostas/listar";
    }

    @GetMapping("/listar")
    public String listarPropostasCliente(ModelMap model) {
        Cliente cliente = getClienteLogado();
        model.addAttribute("propostas", propostaService.buscarTodosPorCliente(cliente));
        return "proposta/lista-cliente";
    }

    @GetMapping("/detalhes/{id}")
    public String exibirDetalhesProposta(@PathVariable("id") Long idProposta,
                                         @RequestParam(value = "statusInicial", required = false) String statusInicial,
                                         ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null) {
            model.addAttribute("fail", "Proposta não encontrada.");
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || proposta.getVeiculo() == null || !proposta.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            model.addAttribute("fail", "Você não tem permissão para acessar os detalhes desta proposta.");
            return "redirect:/propostas/loja/listar";
        }

        if (statusInicial != null && !statusInicial.isEmpty()) {
            proposta.setStatus(statusInicial);
        }

        model.addAttribute("proposta", proposta);
        return "proposta/editar-status";
    }

    @GetMapping("/loja/listar")
    public String listarPropostasLoja(ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "Loja não encontrada ou não logada.");
            return "redirect:/login";
        }
        model.addAttribute("propostas", propostaService.buscarTodosPorLoja(loja));
        return "proposta/lista-loja";
    }

    @PostMapping("/editar-status")
    public String editarStatus(@Valid @ModelAttribute("proposta") Proposta propostaForm,
                               BindingResult result,
                               RedirectAttributes attr,
                               ModelMap model) {

        Proposta propostaOriginal = propostaService.buscarPorId(propostaForm.getId());
        if (propostaOriginal == null) {
            attr.addFlashAttribute("fail", "Proposta não encontrada para atualização de status.");
            return "redirect:/propostas/loja/listar";
        }

        propostaForm.setData(propostaOriginal.getData());
        propostaForm.setCliente(propostaOriginal.getCliente());
        propostaForm.setVeiculo(propostaOriginal.getVeiculo());
        // propostaForm.setCondicoesPagamento(propostaOriginal.getCondicoesPagamento()); // Será tratado abaixo

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || propostaOriginal.getVeiculo() == null || !propostaOriginal.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "Você não tem permissão para editar esta proposta.");
            return "redirect:/propostas/loja/listar";
        }

        if (result.hasErrors()) {
            propostaForm.setVeiculo(propostaOriginal.getVeiculo());
            propostaForm.setCliente(propostaOriginal.getCliente());
            
            model.addAttribute("proposta", propostaForm);
            if (propostaForm.getVeiculo() != null) {
                 model.addAttribute("veiculo", veiculoService.buscarPorIdComFotos(propostaForm.getVeiculo().getId()));
            }
            return "proposta/editar-status";
        }

        // --- LÓGICA PARA ATUALIZAR O VALOR PRINCIPAL E AS CONDIÇÕES DE PAGAMENTO ---
        // Se o status for "NÃO ACEITO" E um contrapropostaValor foi fornecido,
        // então o valor principal da proposta é atualizado para o valor da contraproposta.
        if ("NÃO ACEITO".equals(propostaForm.getStatus())) { // Se o status foi alterado para NÃO ACEITO
            // Atualiza o valor principal SE a contraproposta foi fornecida
            if (propostaForm.getContrapropostaValor() != null) {
                propostaOriginal.setValor(propostaForm.getContrapropostaValor());
            }
            // Atualiza as condições de pagamento principal SE a contrapropostaCondicoes foi fornecida
            if (propostaForm.getContrapropostaCondicoes() != null && !propostaForm.getContrapropostaCondicoes().trim().isEmpty()) {
                propostaOriginal.setCondicoesPagamento(propostaForm.getContrapropostaCondicoes());
            }
        }
        // --- FIM DA LÓGICA DE ATUALIZAÇÃO CONDICIONAL ---


        // COPIAR DADOS DO FORMULÁRIO PARA O OBJETO ORIGINAL GERENCIADO PELO JPA
        propostaOriginal.setStatus(propostaForm.getStatus());
        propostaOriginal.setContrapropostaCondicoes(propostaForm.getContrapropostaCondicoes());
        propostaOriginal.setContrapropostaValor(propostaForm.getContrapropostaValor());
        propostaOriginal.setHorarioReuniao(propostaForm.getHorarioReuniao());
        propostaOriginal.setLinkReuniao(propostaForm.getLinkReuniao());

        // Lógica para limpar campos que não são relevantes para o status atual
        if ("ACEITO".equals(propostaOriginal.getStatus())) {
            // Se aceito, zera os campos de contraproposta (já que o valor principal já foi decidido)
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setContrapropostaValor(null);
        } else if ("NÃO ACEITO".equals(propostaOriginal.getStatus())) {
            // Se não aceito, zera os campos de aceitação
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
        } else { // Status ABERTO ou outros
             // Se voltar para ABERTO, ou um status genérico, zera tudo que não é principal
             propostaOriginal.setContrapropostaCondicoes(null);
             propostaOriginal.setContrapropostaValor(null);
             propostaOriginal.setHorarioReuniao(null);
             propostaOriginal.setLinkReuniao(null);
        }

        propostaService.salvar(propostaOriginal);

        attr.addFlashAttribute("success", "Status da proposta atualizado com sucesso!");
        return "redirect:/propostas/loja/listar";
    }

    @GetMapping("/aceitar/{id}")
    public String aceitarProposta(@PathVariable("id") Long idProposta, RedirectAttributes attr) {
        attr.addAttribute("statusInicial", "ACEITO");
        return "redirect:/propostas/detalhes/" + idProposta;
    }

    @GetMapping("/naoaceitar/{id}")
    public String naoAceitarProposta(@PathVariable("id") Long idProposta, RedirectAttributes attr) {
        attr.addAttribute("statusInicial", "NÃO ACEITO");
        return "redirect:/propostas/detalhes/" + idProposta;
    }
}