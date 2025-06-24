package DSW.TrabalhoDSW_Veiculos.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.imp.NotificacaoPropostaService;
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

    @Autowired
    private NotificacaoPropostaService notificacaoPropostaService;

    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser") &&
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return clienteService.buscarPorEmail(auth.getName());
        }
        return null;
    }

    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser") &&
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LOJA"))) {
            return lojaService.buscarPorEmail(auth.getName());
        }
        return null;
    }

    // --- Cliente: Fazer Proposta ---
    @GetMapping("/cadastrar/{id}")
    public String cadastrar(@PathVariable("id") Long idVeiculo, Proposta proposta, ModelMap model) {
        Veiculo veiculo = veiculoService.buscarPorIdComFotos(idVeiculo);
        if (veiculo == null) {
            model.addAttribute("fail", "Veículo não encontrado.");
            return "redirect:/veiculos/listar";
        }
        proposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        proposta.setVeiculo(veiculo);
        proposta.setCliente(getClienteLogado());
        proposta.setStatus(StatusProposta.ABERTO);
        model.addAttribute("proposta", proposta);
        model.addAttribute("veiculo", veiculo);
        return "proposta/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid Proposta proposta, BindingResult result, RedirectAttributes attr, ModelMap model) {
        proposta.setCliente(getClienteLogado());
        if (proposta.getData() == null || proposta.getData().isEmpty()) {
            proposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
        proposta.setStatus(StatusProposta.ABERTO);

        if (result.hasErrors()) {
            Veiculo veiculo = null;
            if (proposta.getVeiculo() != null && proposta.getVeiculo().getId() != null) {
                veiculo = veiculoService.buscarPorIdComFotos(proposta.getVeiculo().getId());
                model.addAttribute("veiculo", veiculo);
            }
            attr.addFlashAttribute("fail", "Erro no preenchimento da proposta. Verifique os campos.");
            return "proposta/cadastro";
        }

        if(propostaService.existePropostaAberta(proposta.getCliente().getId(), proposta.getVeiculo().getId())) {
            attr.addFlashAttribute("fail", "Você já tem uma proposta ativa para este veículo. Aguarde a resposta da loja ou verifique suas propostas.");
            return "redirect:/veiculos/listar";
        }

        propostaService.salvar(proposta);

        notificacaoPropostaService.notificarProposta(proposta, false); // false para notificar a loja
        
        attr.addFlashAttribute("success", "Proposta enviada com sucesso! Aguarde a resposta da loja.");
        return "redirect:/propostas/listar";
    }

    // --- Cliente: Listar Propostas ---
    @GetMapping("/listar")
    public String listarPropostasCliente(ModelMap model) {
        Cliente cliente = getClienteLogado();
        if (cliente == null) {
            model.addAttribute("fail", "Cliente não logado.");
            return "redirect:/login";
        }
        model.addAttribute("propostas", propostaService.buscarTodosPorCliente(cliente));
        return "proposta/lista-cliente";
    }

    // --- Loja: Listar Propostas ---
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


    @GetMapping("/loja/gerenciar/{id}")
    public String gerenciarPropostaLoja(@PathVariable("id") Long idProposta, ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null) {
            model.addAttribute("fail", "Proposta não encontrada.");
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || proposta.getVeiculo() == null || !proposta.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            model.addAttribute("fail", "Você não tem permissão para gerenciar esta proposta.");
            return "redirect:/propostas/loja/listar";
        }
        
        model.addAttribute("veiculo", proposta.getVeiculo()); 
        model.addAttribute("proposta", proposta); 
        return "proposta/editar-status";
    }

    @PostMapping("/loja/atualizar-status")
    public String atualizarStatusPropostaLoja(
        @RequestParam("id") Long id,
        @RequestParam(value = "acaoLojista", required = false) String acaoLojista,
        @RequestParam(value = "contrapropostaValor", required = false) BigDecimal contrapropostaValor,
        @RequestParam(value = "contrapropostaCondicoes", required = false) String contrapropostaCondicoes,
        @RequestParam(value = "horarioReuniao", required = false) String horarioReuniao,
        @RequestParam(value = "linkReuniao", required = false) String linkReuniao,
        RedirectAttributes attr) {

        Proposta proposta = propostaService.buscarPorId(id);
        if (acaoLojista == null || acaoLojista.isEmpty()) {
            attr.addFlashAttribute("fail", "Selecione uma ação (Aceitar, Contraproposta ou Recusar).");
            return "redirect:/propostas/loja/gerenciar/" + id;
        }
        if (proposta == null) {
            attr.addFlashAttribute("fail", "Proposta não encontrada.");
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || proposta.getVeiculo() == null || 
            !proposta.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "Permissão negada.");
            return "redirect:/propostas/loja/listar";
        }

        // Verificar se a proposta está em estado válido para ação
        /*if (proposta.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_LOJA) {
            attr.addFlashAttribute("fail", "A proposta não está aguardando resposta da loja.");
            return "redirect:/propostas/loja/listar";
        }
            */
        // Verificar status correto (ABERTO)
        if (proposta.getStatus() != StatusProposta.ABERTO) {
            attr.addFlashAttribute("fail", "A proposta não está aberta para ação.");
            return "redirect:/propostas/loja/listar";
        }

        try {
            switch (acaoLojista) {
                case "aceitar":
                    // Validação para aceitar proposta
                    if (horarioReuniao == null || horarioReuniao.trim().isEmpty()) {
                        attr.addFlashAttribute("fail", "Horário da reunião é obrigatório.");
                        return "redirect:/propostas/loja/gerenciar/" + id;
                    }
                    if (linkReuniao == null || linkReuniao.trim().isEmpty() || !isValidUrl(linkReuniao)) {
                        attr.addFlashAttribute("fail", "Link de reunião inválido.");
                        return "redirect:/propostas/loja/gerenciar/" + id;
                    }
                    
                    proposta.setStatus(StatusProposta.ACEITO);
                    proposta.setHorarioReuniao(LocalDateTime.parse(horarioReuniao, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    proposta.setLinkReuniao(linkReuniao);
                    proposta.setContrapropostaValor(null);
                    proposta.setContrapropostaCondicoes(null);
                    
                    propostaService.salvar(proposta);
                    notificacaoPropostaService.notificarProposta(proposta, true);
                    attr.addFlashAttribute("success", "Proposta aceita! Cliente notificado.");
                    break;

                case "contraproposta":
                    // Validação para contraproposta
                    if (contrapropostaValor == null || contrapropostaValor.compareTo(BigDecimal.ZERO) <= 0) {
                        attr.addFlashAttribute("fail", "Valor inválido para contraproposta.");
                        return "redirect:/propostas/loja/gerenciar/" + id;
                    }
                    if (contrapropostaCondicoes == null || contrapropostaCondicoes.trim().isEmpty()) {
                        attr.addFlashAttribute("fail", "Condições de pagamento são obrigatórias.");
                        return "redirect:/propostas/loja/gerenciar/" + id;
                    }
                    
                    proposta.setStatus(StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE);
                    proposta.setContrapropostaValor(contrapropostaValor);
                    proposta.setContrapropostaCondicoes(contrapropostaCondicoes);
                    proposta.setHorarioReuniao(null);
                    proposta.setLinkReuniao(null);
                    
                    propostaService.salvar(proposta);
                    notificacaoPropostaService.notificarProposta(proposta, true);
                    attr.addFlashAttribute("success", "Contraproposta enviada! Cliente notificado.");
                    break;

                case "recusar":
                    proposta.setStatus(StatusProposta.RECUSADO_LOJA);
                    proposta.setContrapropostaValor(null);
                    proposta.setContrapropostaCondicoes(null);
                    proposta.setHorarioReuniao(null);
                    proposta.setLinkReuniao(null);
                    
                    propostaService.salvar(proposta);
                    notificacaoPropostaService.notificarProposta(proposta, true);
                    attr.addFlashAttribute("success", "Proposta recusada com sucesso.");
                    break;

                default:
                    attr.addFlashAttribute("fail", "Ação inválida.");
                    return "redirect:/propostas/loja/gerenciar/" + id;
            }
            
            return "redirect:/propostas/loja/listar";

        } catch (Exception e) {
            attr.addFlashAttribute("fail", "Erro ao processar ação: " + e.getMessage());
            return "redirect:/propostas/loja/gerenciar/" + id;
        }
    }

    @GetMapping("/cliente/responder/{id}")
    public String responderContrapropostaCliente(@PathVariable("id") Long idProposta, ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null || getClienteLogado() == null || !proposta.getCliente().getId().equals(getClienteLogado().getId())) {
            model.addAttribute("fail", "Proposta não encontrada ou você não tem permissão para acessá-la.");
            return "redirect:/propostas/listar";
        }
        if (proposta.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            model.addAttribute("fail", "Esta proposta não está aguardando sua resposta ou já foi finalizada.");
            return "redirect:/propostas/listar";
        }
        model.addAttribute("proposta", proposta);
        return "proposta/resposta-contraproposta";
    }

    @PostMapping("/cliente/processar-resposta")
    public String processarRespostaContraproposta(@Valid @ModelAttribute("proposta") Proposta propostaForm,
                                                  BindingResult result,
                                                  @RequestParam("acaoCliente") String acaoCliente,
                                                  RedirectAttributes attr) {
        Proposta propostaOriginal = propostaService.buscarPorId(propostaForm.getId());
        if (propostaOriginal == null || getClienteLogado() == null || !propostaOriginal.getCliente().getId().equals(getClienteLogado().getId())) {
            attr.addFlashAttribute("fail", "Proposta não encontrada ou você não tem permissão.");
            return "redirect:/propostas/listar";
        }
        if (propostaOriginal.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            attr.addFlashAttribute("fail", "Esta proposta não está aguardando sua resposta ou já foi finalizada.");
            return "redirect:/propostas/listar";
        }
        
        if ("nova".equals(acaoCliente)) {
            if (propostaForm.getValor() == null || propostaForm.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                 result.addError(new FieldError("proposta", "valor", "O valor da nova proposta deve ser maior que zero."));
            }
            if (propostaForm.getCondicoesPagamento() == null || propostaForm.getCondicoesPagamento().trim().isEmpty()) {
                 result.addError(new FieldError("proposta", "condicoesPagamento", "As condições da nova proposta são obrigatórias."));
            }

            if (propostaForm.getLinkReuniao() != null && !propostaForm.getLinkReuniao().trim().isEmpty()) {
                if (!isValidUrl(propostaForm.getLinkReuniao())) {
                    result.addError(new FieldError("proposta", "linkReuniao", "Formato de URL inválido para o link da reunião."));
                }
            }
            if (result.hasErrors()) {
                attr.addFlashAttribute("fail", "Erro na validação da nova proposta. Verifique os campos.");
                return "redirect:/propostas/cliente/responder/" + propostaForm.getId();
            }
        }

        if ("aceitar".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.ACEITO);
            if (propostaOriginal.getContrapropostaValor() != null) {
                propostaOriginal.setValor(propostaOriginal.getContrapropostaValor());
            }
            if (propostaOriginal.getContrapropostaCondicoes() != null && !propostaOriginal.getContrapropostaCondicoes().trim().isEmpty()) {
                propostaOriginal.setCondicoesPagamento(propostaOriginal.getContrapropostaCondicoes());
            }
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            
            propostaService.salvar(propostaOriginal);
            notificacaoPropostaService.notificarProposta(propostaOriginal, false); // Notificar a loja
            
            attr.addFlashAttribute("success", "Contraproposta da loja aceita com sucesso! Verifique os detalhes da reunião.");

        } else if ("recusar".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.RECUSADO_CLIENTE);
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            
            propostaService.salvar(propostaOriginal);
            notificacaoPropostaService.notificarProposta(propostaOriginal, false); // Notificar a loja
            
            attr.addFlashAttribute("success", "Contraproposta recusada. Proposta fechada.");

        } else if ("nova".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.RECUSADO_CLIENTE);
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            propostaService.salvar(propostaOriginal); // Salva a proposta antiga

            Proposta novaProposta = new Proposta();
            novaProposta.setCliente(getClienteLogado());
            novaProposta.setVeiculo(propostaOriginal.getVeiculo());
            novaProposta.setValor(propostaForm.getValor());
            novaProposta.setCondicoesPagamento(propostaForm.getCondicoesPagamento());
            novaProposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            novaProposta.setStatus(StatusProposta.ABERTO);
            
            propostaService.salvar(novaProposta);
            notificacaoPropostaService.notificarProposta(novaProposta, false); // Notificar a loja sobre a nova proposta
            
            attr.addFlashAttribute("success", "Sua nova proposta foi enviada com sucesso para a loja!");
            return "redirect:/propostas/listar";
        } else {
            attr.addFlashAttribute("fail", "Ação inválida para a contraproposta.");
            return "redirect:/propostas/listar";
        }
        propostaService.salvar(propostaOriginal);
        return "redirect:/propostas/listar";
    }

    // Método auxiliar para validação de URL
    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}