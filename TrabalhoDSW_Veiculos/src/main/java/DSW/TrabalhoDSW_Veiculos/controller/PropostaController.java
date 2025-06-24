package DSW.TrabalhoDSW_Veiculos.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    // Método auxiliar para obter o cliente logado
    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return clienteService.buscarPorEmail(email);
    }

    // Método auxiliar para obter a loja logada
    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return lojaService.buscarPorEmail(email);
    }

    // Cadastro de nova proposta para um veículo específico
    @GetMapping("/cadastrar/{id}")
    public String cadastrar(@PathVariable("id") Long idVeiculo,
                            Proposta proposta, // Spring implicitamente adiciona isso ao modelo
                            ModelMap model) {

        // Buscar o veículo com suas fotos, se necessário para exibição no formulário
        Veiculo veiculo = veiculoService.buscarPorIdComFotos(idVeiculo);
        if(veiculo == null) {
            model.addAttribute("fail", "Veículo não encontrado.");
            return "redirect:/veiculos/listar";
        }

        // Preenche dados iniciais da proposta
        proposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        proposta.setVeiculo(veiculo); // Associa o veículo à proposta
        proposta.setCliente(getClienteLogado()); // Associa o cliente logado à proposta

        // Explicitamente adicionar 'proposta' e 'veiculo' ao model.
        // O 'proposta' é fundamental para o th:object.
        // O 'veiculo' é útil para acessar as propriedades do veículo diretamente no HTML,
        // como modelo, placa, etc., sem precisar passar por proposta.veiculo.
        model.addAttribute("proposta", proposta);
        model.addAttribute("veiculo", veiculo);

        return "proposta/cadastro";
    }

    // Salvar proposta com validações
    @PostMapping("/salvar")
    public String salvar(@Valid Proposta proposta,
                         BindingResult result,
                         RedirectAttributes attr,
                         ModelMap model) { // Adicionado ModelMap para re-renderizar o formulário em caso de erros

        // Se houver erros de validação, precisamos re-adicionar o objeto 'veiculo' ao modelo.
        // O objeto 'proposta' que vem da submissão do formulário pode não ter sua propriedade 'veiculo'
        // totalmente preenchida, especialmente após a validação.
        if(result.hasErrors()) {
            // Se o ID do veículo foi passado corretamente (via campo hidden no form)
            if (proposta.getVeiculo() != null && proposta.getVeiculo().getId() != null) {
                // Busca o veículo novamente para ter certeza de que todas as suas propriedades
                // (como modelo, etc.) estão disponíveis para o Thymeleaf.
                Veiculo veiculo = veiculoService.buscarPorIdComFotos(proposta.getVeiculo().getId());
                model.addAttribute("veiculo", veiculo); // Adiciona o veículo ao modelo novamente
            } else {
                // Caso o ID do veículo não esteja disponível, pode ser um erro mais grave
                System.err.println("Erro: ID do veículo não encontrado na proposta submetida durante erro de validação.");
                attr.addFlashAttribute("fail", "Erro ao processar a proposta. Veículo não identificado.");
                return "redirect:/veiculos/listar"; // Redireciona para evitar mais erros
            }
            return "proposta/cadastro"; // Retorna para o formulário de cadastro com os erros
        }

        // Verifica se já existe proposta aberta para este veículo+cliente (R5)
        if(propostaService.existePropostaAberta(
            proposta.getCliente().getId(),
            proposta.getVeiculo().getId())) {

            attr.addFlashAttribute("fail", "Você já tem uma proposta em aberto para este veículo");
            return "redirect:/veiculos/listar";
        }

        // Força status inicial ABERTO (R7)
        proposta.setStatus("ABERTO");

        propostaService.salvar(proposta);
        attr.addFlashAttribute("success", "Proposta enviada com sucesso!");
        return "redirect:/propostas/listar";
    }

    // Listar propostas do cliente logado
    @GetMapping("/listar")
    public String listar(ModelMap model) {
        Cliente cliente = getClienteLogado();
        model.addAttribute("propostas", propostaService.buscarPorCliente(cliente));
        return "proposta/lista";
    }

    // --- Métodos auxiliares ---
    @ModelAttribute("condicoesPagamento")
    public List<String> getCondicoesPagamento() {
        return List.of(
            "À vista",
            "Parcelado em 12x",
            "Parcelado em 24x",
            "Parcelado em 36x"
        );
    }

    // Listar propostas recebidas pela loja logada (R8)
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

    // Aceitar Proposta (R8)
    @GetMapping("/aceitar/{id}")
    public String aceitarProposta(@PathVariable("id") Long idProposta, RedirectAttributes attr) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta != null && proposta.getVeiculo().getLoja().getId().equals(getLojaLogada().getId())) {
            proposta.setStatus("ACEITO");
            propostaService.salvar(proposta);
            attr.addFlashAttribute("success", "Proposta aceita com sucesso! Prossiga para informar os detalhes da reunião.");
            // Corrigido para passar o ID da proposta, não o do veículo
            return "redirect:/propostas/detalhes/" + idProposta;
        }
        attr.addFlashAttribute("fail", "Não foi possível aceitar a proposta.");
        return "redirect:/propostas/loja/listar";
    }

    // Não Aceitar Proposta (R8)
    @GetMapping("/naoaceitar/{id}")
    public String naoAceitarProposta(@PathVariable("id") Long idProposta, RedirectAttributes attr) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta != null && proposta.getVeiculo().getLoja().getId().equals(getLojaLogada().getId())) {
            proposta.setStatus("NÃO ACEITO");
            propostaService.salvar(proposta);
            attr.addFlashAttribute("success", "Proposta marcada como Não Aceita. Prossiga para informar uma contraproposta opcional.");
            // Corrigido para passar o ID da proposta, não o do veículo
            return "redirect:/propostas/detalhes/" + idProposta;
        }
        attr.addFlashAttribute("fail", "Não foi possível recusar a proposta.");
        return "redirect:/propostas/loja/listar";
    }
}
