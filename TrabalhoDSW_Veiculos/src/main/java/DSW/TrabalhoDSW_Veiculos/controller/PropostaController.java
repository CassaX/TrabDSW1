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
    public String atualizarStatusPropostaLoja(@Valid @ModelAttribute("proposta") Proposta propostaForm,
                                              BindingResult result,
                                              @RequestParam(value = "acaoLojista", required = false) String acaoLojista,
                                              RedirectAttributes attr,
                                              ModelMap model) {

        Proposta propostaOriginal = propostaService.buscarPorId(propostaForm.getId());
        if (propostaOriginal == null) {
            attr.addFlashAttribute("fail", "Proposta não encontrada para atualização de status.");
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || propostaOriginal.getVeiculo() == null || !propostaOriginal.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "Você não tem permissão para editar esta proposta.");
            return "redirect:/propostas/loja/listar";
        }
        
        propostaForm.setCliente(propostaOriginal.getCliente());
        propostaForm.setVeiculo(propostaOriginal.getVeiculo());
        propostaForm.setData(propostaOriginal.getData());
        
        if (propostaForm.getValor() == null) {
            propostaForm.setValor(propostaOriginal.getValor());
        }
        if (propostaForm.getCondicoesPagamento() == null || propostaForm.getCondicoesPagamento().trim().isEmpty()) {
            propostaForm.setCondicoesPagamento(propostaOriginal.getCondicoesPagamento());
        }

        if (propostaForm.getStatus() != StatusProposta.ACEITO) {
            propostaForm.setHorarioReuniao(null); 
            propostaForm.setLinkReuniao(null);   
        }

        if (propostaForm.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            propostaForm.setContrapropostaValor(null);
            propostaForm.setContrapropostaCondicoes(null);
        }
        
        if (propostaForm.getStatus() == null) {
            result.addError(new FieldError("proposta", "status", "Selecione uma ação para a proposta."));
        } else if (propostaForm.getStatus() == StatusProposta.ACEITO) {
            if (propostaForm.getHorarioReuniao() == null) {
                result.addError(new FieldError("proposta", "horarioReuniao", "O horário da reunião é obrigatório ao aceitar a proposta."));
            }
            if (propostaForm.getLinkReuniao() == null || propostaForm.getLinkReuniao().trim().isEmpty()) {
                result.addError(new FieldError("proposta", "linkReuniao", "O link da reunião é obrigatório ao aceitar a proposta."));
            }
            // Validação de formato de URL para o link da reunião (agora manual)
            if (propostaForm.getLinkReuniao() != null && !propostaForm.getLinkReuniao().trim().isEmpty()) {
                if (!isValidUrl(propostaForm.getLinkReuniao())) {
                    result.addError(new FieldError("proposta", "linkReuniao", "Formato de URL inválido para o link da reunião."));
                }
            }

        } else if (propostaForm.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            if (acaoLojista == null || acaoLojista.isEmpty()) {
                result.addError(new FieldError("proposta", "acaoLojista", "Selecione uma ação (Contraproposta ou Recusar)."));
            } else if ("contraproposta".equals(acaoLojista)) {
                if (propostaForm.getContrapropostaValor() == null || propostaForm.getContrapropostaValor().compareTo(BigDecimal.ZERO) <= 0) {
                    result.addError(new FieldError("proposta", "contrapropostaValor", "O valor da contraproposta deve ser maior que zero."));
                }
                if (propostaForm.getContrapropostaCondicoes() == null || propostaForm.getContrapropostaCondicoes().trim().isEmpty()) {
                    result.addError(new FieldError("proposta", "contrapropostaCondicoes", "As condições da contraproposta são obrigatórias."));
                }

            } else if ("recusar".equals(acaoLojista)) {
                // Não há validações extras para recusar. Campos já zerados acima.
            }
        } else {
            result.addError(new FieldError("proposta", "status", "Status inválido ou ação inesperada."));
        }
        
        if (result.hasErrors()) {
            model.addAttribute("proposta", propostaForm); 
            model.addAttribute("veiculo", propostaOriginal.getVeiculo()); 
            attr.addFlashAttribute("fail", "Erro no preenchimento do formulário. Verifique os campos.");
            return "proposta/editar-status"; 
        }

        // --- Lógica de Atualização da Proposta ---
        if (propostaForm.getStatus() == StatusProposta.ACEITO) {
            propostaOriginal.setStatus(StatusProposta.ACEITO);
            propostaOriginal.setHorarioReuniao(propostaForm.getHorarioReuniao());
            propostaOriginal.setLinkReuniao(propostaForm.getLinkReuniao());
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            
            propostaService.salvar(propostaOriginal);
            notificacaoPropostaService.notificarProposta(propostaOriginal, true); // Notificar o cliente
            
            attr.addFlashAttribute("success", "Proposta aceita com sucesso! O cliente será notificado.");

        } else if (propostaForm.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            if ("contraproposta".equals(acaoLojista)) {
                propostaOriginal.setStatus(StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE);
                propostaOriginal.setValor(propostaForm.getContrapropostaValor());
                propostaOriginal.setCondicoesPagamento(propostaForm.getContrapropostaCondicoes());
                propostaOriginal.setContrapropostaValor(propostaForm.getContrapropostaValor());
                propostaOriginal.setContrapropostaCondicoes(propostaForm.getContrapropostaCondicoes());
                propostaOriginal.setHorarioReuniao(null);
                propostaOriginal.setLinkReuniao(null);
                
                propostaService.salvar(propostaOriginal);
                notificacaoPropostaService.notificarProposta(propostaOriginal, true); // Notificar o cliente
                
                attr.addFlashAttribute("success", "Contraproposta enviada ao cliente com sucesso!");

            } else if ("recusar".equals(acaoLojista)) {
                propostaOriginal.setStatus(StatusProposta.RECUSADO_LOJA);
                propostaOriginal.setContrapropostaValor(null);
                propostaOriginal.setContrapropostaCondicoes(null);
                propostaOriginal.setHorarioReuniao(null);
                propostaOriginal.setLinkReuniao(null);
                
                propostaService.salvar(propostaOriginal);
                notificacaoPropostaService.notificarProposta(propostaOriginal, true); // Notificar o cliente
                
                attr.addFlashAttribute("success", "Proposta recusada e fechada com sucesso!");
            } else {
                attr.addFlashAttribute("fail", "Ação 'Recusar' ou 'Contraproposta' não selecionada. Nenhuma alteração realizada.");
            }
        } else {
            attr.addFlashAttribute("fail", "Status inválido ou ação inesperada. Nenhuma alteração realizada.");
        }

        return "redirect:/propostas/loja/listar"; 
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