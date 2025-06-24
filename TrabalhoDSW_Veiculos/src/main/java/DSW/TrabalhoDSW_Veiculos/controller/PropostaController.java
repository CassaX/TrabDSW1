package DSW.TrabalhoDSW_Veiculos.controller;

import java.math.BigDecimal;
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

    // Cadastro de nova proposta para um veículo específico
    @GetMapping("/cadastrar/{id}")
    public String cadastrar(@PathVariable("id") Long idVeiculo, 
                           Proposta proposta, 
                           ModelMap model) {
        
        Veiculo veiculo = veiculoService.buscarPorId(idVeiculo);
        if(veiculo == null) {
            model.addAttribute("fail", "Veículo não encontrado");
            return "redirect:/veiculos/listar";
        }
        
        // Preenche dados iniciais
        proposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        proposta.setVeiculo(veiculo);
        proposta.setCliente(getClienteLogado());
        
        model.addAttribute("veiculo", veiculo);
        return "proposta/cadastro";
    }

    // Salvar proposta com validações
    @PostMapping("/salvar")
    public String salvar(@Valid Proposta proposta, 
                        BindingResult result,
                        RedirectAttributes attr) {
        
        if(result.hasErrors()) {
            return "proposta/cadastro";
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
    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // Obtém e-mail do usuário logado
        return clienteService.buscarPorEmail(email);
    }
    
    // Lista de condições de pagamento (exemplo)
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
        Loja loja = getLojaLogada(); // Método auxiliar para obter a loja logada
        if (loja == null) {
            // Tratar caso a loja não seja encontrada ou não esteja logada
            model.addAttribute("fail", "Loja não encontrada ou não logada.");
            return "redirect:/login"; // Ou alguma página de erro/home
        }
        model.addAttribute("propostas", propostaService.buscarTodosPorLoja(loja));
        return "proposta/lista-loja";
    }

    // Aceitar Proposta (R8)
    @GetMapping("/aceitar/{id}") // Ou @PostMapping se for um formulário
    public String aceitarProposta(@PathVariable("id") Long idProposta, RedirectAttributes attr) {
        // Implementar lógica de aceitação:
        // 1. Buscar a proposta pelo ID
        // 2. Verificar se a proposta pertence à loja logada (segurança!)
        // 3. Atualizar o status para "ACEITO"
        // 4. Redirecionar para uma tela onde a loja pode inserir o link da reunião
        //    OU, se for simples, apenas marcar como aceita e enviar email
        // Por enquanto, apenas um exemplo simples:
        Proposta proposta = propostaService.buscarPorId(idProposta); //
        if (proposta != null && proposta.getVeiculo().getLoja().getId().equals(getLojaLogada().getId())) { // Supondo que você tem getLojaLogada()
            proposta.setStatus("ACEITO"); //
            propostaService.salvar(proposta); //
            attr.addFlashAttribute("success", "Proposta aceita com sucesso! Prossiga para informar os detalhes da reunião.");
            // Idealmente, redirecionar para uma tela de detalhes para adicionar o link da reunião
            return "redirect:/propostas/detalhes/{id}"; // Criar essa tela depois
        }
        attr.addFlashAttribute("fail", "Não foi possível aceitar a proposta.");
        return "redirect:/propostas/loja/listar";
    }

    // Não Aceitar Proposta (R8)
    @GetMapping("/naoaceitar/{id}") // Ou @PostMapping
    public String naoAceitarProposta(@PathVariable("id") Long idProposta, RedirectAttributes attr) {
        // Implementar lógica de não aceitação:
        // 1. Buscar a proposta pelo ID
        // 2. Verificar se a proposta pertence à loja logada (segurança!)
        // 3. Atualizar o status para "NÃO ACEITO"
        // 4. Redirecionar para uma tela onde a loja pode inserir uma contraproposta opcional
        Proposta proposta = propostaService.buscarPorId(idProposta); //
        if (proposta != null && proposta.getVeiculo().getLoja().getId().equals(getLojaLogada().getId())) {
            proposta.setStatus("NÃO ACEITO"); //
            propostaService.salvar(proposta); //
            attr.addFlashAttribute("success", "Proposta marcada como Não Aceita. Prossiga para informar uma contraproposta opcional.");
            return "redirect:/propostas/detalhes/{id}"; // Criar essa tela depois
        }
        attr.addFlashAttribute("fail", "Não foi possível recusar a proposta.");
        return "redirect:/propostas/loja/listar";
    }

    // Método auxiliar para obter a loja logada (similar ao getClienteLogado())
    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // Obtém e-mail do usuário logado
        return lojaService.buscarPorEmail(email); // Você precisará de um método findByEmail no ILojaService e sua implementação
    }
}