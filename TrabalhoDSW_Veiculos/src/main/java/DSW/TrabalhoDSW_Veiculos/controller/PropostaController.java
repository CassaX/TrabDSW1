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

    @GetMapping("/cadastrar/{id}")
    public String cadastrar(@PathVariable("id") Long idVeiculo, Proposta proposta, ModelMap model) {
        Veiculo veiculo = veiculoService.buscarPorIdComFotos(idVeiculo);
        if (veiculo == null) {
            model.addAttribute("fail", "proposta.notfound"); 
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

        if (proposta.getValor() == null || proposta.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError(new FieldError("proposta", "valor", null, false, new String[]{"proposta.valor.requiredAndPositive"}, null, null));
        }
        if (proposta.getCondicoesPagamento() == null || proposta.getCondicoesPagamento().trim().isEmpty()) {
            result.addError(new FieldError("proposta", "condicoesPagamento", null, false, new String[]{"proposta.condicoesPagamento.required"}, null, null));
        }



        if (result.hasErrors()) {
            Veiculo veiculo = null;
            if (proposta.getVeiculo() != null && proposta.getVeiculo().getId() != null) {
                veiculo = veiculoService.buscarPorIdComFotos(proposta.getVeiculo().getId());
                model.addAttribute("veiculo", veiculo);
            }
            attr.addFlashAttribute("fail", "proposta.create.fail"); 
            return "proposta/cadastro";
        }

        if(propostaService.existePropostaAberta(proposta.getCliente().getId(), proposta.getVeiculo().getId())) { 
            attr.addFlashAttribute("fail", "proposta.alreadyOpen"); 
            return "redirect:/veiculos/listar";
        }

        propostaService.salvar(proposta);
        notificacaoPropostaService.notificarProposta(proposta, false); 
        
        attr.addFlashAttribute("success", "proposta.create.success"); 
        return "redirect:/propostas/listar";
    }

    @GetMapping("/listar")
    public String listarPropostasCliente(ModelMap model) {
        Cliente cliente = getClienteLogado();
        if (cliente == null) {
            model.addAttribute("fail", "cliente.notloggedin"); 
            return "redirect:/login";
        }
        model.addAttribute("propostas", propostaService.buscarTodosPorCliente(cliente));
        return "proposta/lista-cliente";
    }


    @GetMapping("/loja/listar")
    public String listarPropostasLoja(ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "loja.notfound.or.notloggedin"); 
            return "redirect:/login";
        }
        model.addAttribute("propostas", propostaService.buscarTodosPorLoja(loja));
        return "proposta/lista-loja";
    }


    @GetMapping("/loja/gerenciar/{id}")
    public String gerenciarPropostaLoja(@PathVariable("id") Long idProposta, ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null) {
            model.addAttribute("fail", "proposta.notfound"); 
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || proposta.getVeiculo() == null || !proposta.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            model.addAttribute("fail", "proposta.permission.denied"); 
            return "redirect:/propostas/loja/listar";
        }
        
        if (proposta.getStatus() == StatusProposta.AGUARDANDO_FINALIZACAO_LOJA) {
            model.addAttribute("preSelectedStatus", StatusProposta.ACEITO.name()); 
        } else {
            model.addAttribute("preSelectedStatus", proposta.getStatus() != null ? proposta.getStatus().name() : "");
        }

        model.addAttribute("veiculo", proposta.getVeiculo()); 
        model.addAttribute("proposta", proposta); 
        return "proposta/editar-status";
    }

    @PostMapping("/loja/atualizar-status")
    public String atualizarStatusPropostaLoja(
        @Valid @ModelAttribute("proposta") Proposta propostaForm,
        BindingResult result,
        @RequestParam(value = "acaoLojista", required = false) String acaoLojista,
        RedirectAttributes attr) {
        
        Proposta propostaOriginal = propostaService.buscarPorId(propostaForm.getId());
        if (propostaOriginal == null) {
            attr.addFlashAttribute("fail", "proposta.notfound.update"); 
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || propostaOriginal.getVeiculo() == null || 
            !propostaOriginal.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "proposta.permission.denied"); 
            return "redirect:/propostas/loja/listar";
        }

        propostaForm.setCliente(propostaOriginal.getCliente());
        propostaForm.setVeiculo(propostaOriginal.getVeiculo());
        propostaForm.setData(propostaOriginal.getData());
        propostaForm.setValor(propostaOriginal.getValor()); 
        propostaForm.setCondicoesPagamento(propostaOriginal.getCondicoesPagamento());

        if (propostaForm.getStatus() != StatusProposta.ACEITO) { 
            propostaForm.setHorarioReuniao(null); 
            propostaForm.setLinkReuniao(null);   
        }

        if (propostaForm.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            propostaForm.setContrapropostaValor(null);
            propostaForm.setContrapropostaCondicoes(null);
        }
        
        if (propostaForm.getStatus() == null) {
            result.addError(new FieldError("proposta", "status", null, false, new String[]{"proposta.status.selectAction"}, null, null));
        } else if (propostaForm.getStatus() == StatusProposta.ACEITO) {
            if (propostaForm.getHorarioReuniao() == null) {
                result.addError(new FieldError("proposta", "horarioReuniao", null, false, new String[]{"proposta.horarioReuniao.required"}, null, null));
            }
            if (propostaForm.getLinkReuniao() == null || propostaForm.getLinkReuniao().trim().isEmpty()) {
                result.addError(new FieldError("proposta", "linkReuniao", null, false, new String[]{"proposta.linkReuniao.required"}, null, null));
            } else if (!isValidUrl(propostaForm.getLinkReuniao())) {
                result.addError(new FieldError("proposta", "linkReuniao", null, false, new String[]{"proposta.linkReuniao.invalidFormat"}, null, null));
            }
        } else if (propostaForm.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            if (acaoLojista == null || (!"contraproposta".equals(acaoLojista) && !"recusar".equals(acaoLojista))) {
                result.addError(new FieldError("proposta", "acaoLojista", null, false, new String[]{"proposta.acaoLojista.selectAction"}, null, null));
            } else if ("contraproposta".equals(acaoLojista)) {
                if (propostaForm.getContrapropostaValor() == null || propostaForm.getContrapropostaValor().compareTo(BigDecimal.ZERO) <= 0) {
                    result.addError(new FieldError("proposta", "contrapropostaValor", null, false, new String[]{"proposta.contrapropostaValor.requiredAndPositive"}, null, null));
                }
                if (propostaForm.getContrapropostaCondicoes() == null || propostaForm.getContrapropostaCondicoes().trim().isEmpty()) {
                    result.addError(new FieldError("proposta", "contrapropostaCondicoes", null, false, new String[]{"proposta.contrapropostaCondicoes.required"}, null, null));
                }
            }
        } else {
            result.addError(new FieldError("proposta", "status", null, false, new String[]{"proposta.status.invalidOrUnexpected"}, null, null));
        }

        if (result.hasErrors()) {
            ModelMap model = new ModelMap();
            model.addAttribute("proposta", propostaForm);
            model.addAttribute("veiculo", propostaOriginal.getVeiculo());
            if (propostaOriginal.getStatus() == StatusProposta.AGUARDANDO_FINALIZACAO_LOJA) {
                model.addAttribute("preSelectedStatus", StatusProposta.ACEITO.name());
            } else {
                model.addAttribute("preSelectedStatus", propostaForm.getStatus() != null ? propostaForm.getStatus().name() : "");
            }
            attr.addFlashAttribute("fail", "proposta.update.fail");
            return "proposta/editar-status";
        } else { 
            result.addError(new FieldError("proposta", "status", "proposta.status.invalidOrUnexpected")); 
        }
        
        if (result.hasErrors()) {
            ModelMap model = new ModelMap(); 
            model.addAttribute("proposta", propostaForm); 
            model.addAttribute("veiculo", propostaOriginal.getVeiculo()); 
            if (propostaOriginal.getStatus() == StatusProposta.AGUARDANDO_FINALIZACAO_LOJA) {
                model.addAttribute("preSelectedStatus", StatusProposta.ACEITO.name()); 
            } else {
                model.addAttribute("preSelectedStatus", propostaForm.getStatus() != null ? propostaForm.getStatus().name() : "");
            }
            attr.addFlashAttribute("fail", "proposta.update.fail"); 
            return "proposta/editar-status"; 
        }


        if (propostaForm.getStatus() == StatusProposta.ACEITO) { 
            propostaOriginal.setStatus(StatusProposta.ACEITO);
            propostaOriginal.setHorarioReuniao(propostaForm.getHorarioReuniao());
            propostaOriginal.setLinkReuniao(propostaForm.getLinkReuniao());
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            
            propostaService.salvar(propostaOriginal);
            notificacaoPropostaService.notificarProposta(propostaOriginal, true); 

            List<Proposta> outrasPropostas = propostaService.buscarPorVeiculo(propostaOriginal.getVeiculo());
            for (Proposta outra : outrasPropostas) {
                if (!outra.getId().equals(propostaOriginal.getId()) && 
                    (outra.getStatus() == StatusProposta.ABERTO ||
                    outra.getStatus() == StatusProposta.AGUARDANDO_FINALIZACAO_LOJA ||
                    outra.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE)) {

                    outra.setStatus(StatusProposta.RECUSADO_LOJA);
                    outra.setContrapropostaValor(null);
                    outra.setContrapropostaCondicoes(null);
                    outra.setHorarioReuniao(null);
                    outra.setLinkReuniao(null);
                    propostaService.salvar(outra);

                    notificacaoPropostaService.notificarProposta(outra, false);
                }
            }
            
            attr.addFlashAttribute("success", "proposta.accept.success");

        } else if (propostaForm.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            if ("contraproposta".equals(acaoLojista)) {
                propostaOriginal.setStatus(StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE);
                propostaOriginal.setValor(propostaForm.getContrapropostaValor());
                propostaOriginal.setCondicoesPagamento(propostaForm.getContrapropostaCondicoes());
                propostaOriginal.setContrapropostaValor(propostaForm.getContrapropostaValor());
                propostaOriginal.setContrapropostaCondicoes(propostaOriginal.getContrapropostaCondicoes());
                propostaOriginal.setHorarioReuniao(null);
                propostaOriginal.setLinkReuniao(null);
                
                propostaService.salvar(propostaOriginal);
                notificacaoPropostaService.notificarProposta(propostaOriginal, true);
                
                attr.addFlashAttribute("success", "proposta.counterproposal.sent");

            } else if ("recusar".equals(acaoLojista)) {
                propostaOriginal.setStatus(StatusProposta.RECUSADO_LOJA);
                propostaOriginal.setContrapropostaValor(null);
                propostaOriginal.setContrapropostaCondicoes(null);
                propostaOriginal.setHorarioReuniao(null);
                propostaOriginal.setLinkReuniao(null);
                
                propostaService.salvar(propostaOriginal);
                notificacaoPropostaService.notificarProposta(propostaOriginal, true);
                
                attr.addFlashAttribute("success", "proposta.decline.success");
            } else { 
                attr.addFlashAttribute("fail", "proposta.acaoLojista.invalidActionValue"); 
            }
        } else if (propostaForm.getStatus() == StatusProposta.AGUARDANDO_FINALIZACAO_LOJA) { 
             propostaOriginal.setStatus(StatusProposta.ACEITO);
             propostaOriginal.setHorarioReuniao(propostaForm.getHorarioReuniao());
             propostaOriginal.setLinkReuniao(propostaForm.getLinkReuniao());
             propostaOriginal.setContrapropostaValor(null);
             propostaOriginal.setContrapropostaCondicoes(null);

             propostaService.salvar(propostaOriginal);
             notificacaoPropostaService.notificarProposta(propostaOriginal, true); 
             attr.addFlashAttribute("success", "proposta.finalize.success"); 
        } else {
            attr.addFlashAttribute("fail", "proposta.status.invalidOrUnexpected"); 
        }
        
        return "redirect:/propostas/loja/listar"; 
    }

    @GetMapping("/cliente/responder/{id}")
    public String responderContrapropostaCliente(@PathVariable("id") Long idProposta, ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null || getClienteLogado() == null || !proposta.getCliente().getId().equals(getClienteLogado().getId())) {
            model.addAttribute("fail", "proposta.notfound.or.permission.denied"); 
            return "redirect:/propostas/listar";
        }
        if (proposta.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            model.addAttribute("fail", "proposta.notAwaitingResponse"); 
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
            attr.addFlashAttribute("fail", "proposta.notfound.or.permission.denied"); 
            return "redirect:/propostas/listar";
        }
        if (propostaOriginal.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            attr.addFlashAttribute("fail", "proposta.notAwaitingResponse"); 
            return "redirect:/propostas/listar";
        }
        
        if ("nova".equals(acaoCliente)) {
            if (propostaForm.getValor() == null || propostaForm.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                result.addError(new FieldError("proposta", "valor", null, false, new String[]{"proposta.newValue.invalid"}, null, null));
            }
            if (propostaForm.getCondicoesPagamento() == null || propostaForm.getCondicoesPagamento().trim().isEmpty()) {
                result.addError(new FieldError("proposta", "condicoesPagamento", null, false, new String[]{"proposta.newConditions.required"}, null, null));
            }
            if (propostaForm.getLinkReuniao() != null && !propostaForm.getLinkReuniao().trim().isEmpty()) {
                if (!isValidUrl(propostaForm.getLinkReuniao())) {
                    result.addError(new FieldError("proposta", "linkReuniao", null, false, new String[]{"proposta.linkReuniao.invalidFormat"}, null, null));
                }
            }
            if (result.hasErrors()) {
                attr.addFlashAttribute("fail", "proposta.newProposal.fail");
                return "redirect:/propostas/cliente/responder/" + propostaForm.getId();
            }
        }

        if ("aceitar".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.AGUARDANDO_FINALIZACAO_LOJA); 
            
            if (propostaOriginal.getContrapropostaValor() != null) {
                propostaOriginal.setValor(propostaOriginal.getContrapropostaValor());
            }
            if (propostaOriginal.getContrapropostaCondicoes() != null && !propostaOriginal.getContrapropostaCondicoes().trim().isEmpty()) {
                propostaOriginal.setCondicoesPagamento(propostaOriginal.getContrapropostaCondicoes());
            }
            
            propostaService.salvar(propostaOriginal);
            notificacaoPropostaService.notificarProposta(propostaOriginal, false); 
            
            attr.addFlashAttribute("success", "proposta.clientAccept.success"); 

        } else if ("recusar".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.RECUSADO_CLIENTE);
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            
            propostaService.salvar(propostaOriginal);
            notificacaoPropostaService.notificarProposta(propostaOriginal, false); 
            
            attr.addFlashAttribute("success", "proposta.clientDecline.success"); 

        } else if ("nova".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.RECUSADO_CLIENTE);
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            propostaService.salvar(propostaOriginal); 

            Proposta novaProposta = new Proposta();
            novaProposta.setCliente(getClienteLogado());
            novaProposta.setVeiculo(propostaOriginal.getVeiculo());
            novaProposta.setValor(propostaForm.getValor());
            novaProposta.setCondicoesPagamento(propostaForm.getCondicoesPagamento());
            novaProposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            novaProposta.setStatus(StatusProposta.ABERTO);
            
            propostaService.salvar(novaProposta);
            notificacaoPropostaService.notificarProposta(novaProposta, false); 
            
            attr.addFlashAttribute("success", "proposta.newProposal.sent"); 
            return "redirect:/propostas/listar";
        } else {
            attr.addFlashAttribute("fail", "proposta.action.invalid"); 
            return "redirect:/propostas/listar";
        }
        return "redirect:/propostas/listar";
    }


    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}