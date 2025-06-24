package DSW.TrabalhoDSW_Veiculos.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Importar BigDecimal

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError; // Para adicionar erros específicos
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
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta; // Importar o enum
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

    // Cliente faz a proposta inicial
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
        proposta.setStatus(StatusProposta.ABERTO); // Cliente envia, status é ABERTO (Aguardando Resposta da Loja)
        
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
            if (proposta.getVeiculo() != null && proposta.getVeiculo().getId() != null) {
                veiculo = veiculoService.buscarPorIdComFotos(proposta.getVeiculo().getId());
                model.addAttribute("veiculo", veiculo);
            }
            
            // Re-popula o cliente e data para não perder no caso de erro de validação
            proposta.setCliente(getClienteLogado());
            proposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            proposta.setStatus(StatusProposta.ABERTO); // Garante que o status seja ABERTO mesmo com erro de validação
            
            model.addAttribute("proposta", proposta);
            attr.addFlashAttribute("fail", "Erro no preenchimento da proposta. Verifique os campos.");
            return "proposta/cadastro";
        }

        // Verifica se já existe uma proposta ativa (ABERTA ou AGUARDANDO_RESPOSTA_CLIENTE) para o mesmo veículo pelo mesmo cliente
        if(propostaService.existePropostaAberta(
            proposta.getCliente().getId(),
            proposta.getVeiculo().getId())) {

            attr.addFlashAttribute("fail", "Você já tem uma proposta ativa para este veículo. Aguarde a resposta da loja ou verifique suas propostas.");
            return "redirect:/veiculos/listar";
        }

        proposta.setStatus(StatusProposta.ABERTO); // Status inicial ao salvar
        propostaService.salvar(proposta);
        attr.addFlashAttribute("success", "Proposta enviada com sucesso! Aguarde a resposta da loja.");
        return "redirect:/propostas/listar";
    }

    // Cliente lista suas propostas
    @GetMapping("/listar")
    public String listarPropostasCliente(ModelMap model) {
        Cliente cliente = getClienteLogado();
        model.addAttribute("propostas", propostaService.buscarTodosPorCliente(cliente));
        return "proposta/lista-cliente";
    }

    // Lojista lista suas propostas
    @GetMapping("/loja/listar")
    public String listarPropostasLoja(ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "Loja não encontrada ou não logada.");
            return "redirect:/login"; // Ou outra página de erro/login
        }
        model.addAttribute("propostas", propostaService.buscarTodosPorLoja(loja));
        return "proposta/lista-loja";
    }

    // Lojista acessa os detalhes para gerenciar (aceitar/não aceitar/contraproposta)
    @GetMapping("/loja/gerenciar/{id}")
    public String gerenciarPropostaLoja(@PathVariable("id") Long idProposta, ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null) {
            model.addAttribute("fail", "Proposta não encontrada.");
            return "redirect:/propostas/loja/listar";
        }

        Loja lojaLogada = getLojaLogada();
        // Apenas a loja proprietária do veículo pode gerenciar a proposta
        if (lojaLogada == null || proposta.getVeiculo() == null || !proposta.getVeiculo().getLoja().getId().equals(lojaLogada.getId())) {
            model.addAttribute("fail", "Você não tem permissão para gerenciar esta proposta.");
            return "redirect:/propostas/loja/listar";
        }

        model.addAttribute("proposta", proposta);
        return "proposta/editar-status"; // HTML de edição para a loja
    }

    @PostMapping("/loja/atualizar-status")
    public String atualizarStatusPropostaLoja(@Valid @ModelAttribute("proposta") Proposta propostaForm,
                                              BindingResult result,
                                              @RequestParam(value = "acaoLojista", required = false) String acaoLojista, // Para o radio button
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
        
        // Antes de validar, populamos os campos que vêm da proposta original para que o @Valid tenha todos os dados
        propostaForm.setCliente(propostaOriginal.getCliente());
        propostaForm.setVeiculo(propostaOriginal.getVeiculo());
        propostaForm.setData(propostaOriginal.getData());
        // Atenção: propostaForm.setValor() e propostaForm.setCondicoesPagamento() não devem ser do original aqui,
        // pois podem vir preenchidos com a contraproposta. O valor original é apenas para exibição.

        // Validação customizada baseada na ação do lojista
        if (propostaForm.getStatus() == null) {
            result.rejectValue("status", "NotNull", "Selecione um status para a proposta.");
        } else if (propostaForm.getStatus() == StatusProposta.ACEITO) {
            if (propostaForm.getHorarioReuniao() == null) {
                result.addError(new FieldError("proposta", "horarioReuniao", "O horário da reunião é obrigatório ao aceitar a proposta."));
            }
            if (propostaForm.getLinkReuniao() == null || propostaForm.getLinkReuniao().trim().isEmpty()) {
                result.addError(new FieldError("proposta", "linkReuniao", "O link da reunião é obrigatório ao aceitar a proposta."));
            }
            // Garante que campos de contraproposta estejam nulos se for ACEITO
            propostaForm.setContrapropostaValor(null);
            propostaForm.setContrapropostaCondicoes(null);

        } else if (propostaForm.getStatus() == StatusProposta.NAO_ACEITO) { // Este é o valor do select na loja
            if (acaoLojista == null || acaoLojista.isEmpty()) {
                 result.addError(new FieldError("proposta", "acaoLojista", "Selecione uma ação (Contraproposta ou Fechar)."));
            } else if ("contraproposta".equals(acaoLojista)) {
                if (propostaForm.getContrapropostaValor() == null || propostaForm.getContrapropostaValor().compareTo(BigDecimal.ZERO) <= 0) {
                    result.addError(new FieldError("proposta", "contrapropostaValor", "O valor da contraproposta deve ser maior que zero."));
                }
                if (propostaForm.getContrapropostaCondicoes() == null || propostaForm.getContrapropostaCondicoes().trim().isEmpty()) {
                    result.addError(new FieldError("proposta", "contrapropostaCondicoes", "As condições da contraproposta são obrigatórias."));
                }
                if (propostaForm.getHorarioReuniao() == null) { // Horário da reunião também é sugerido na contraproposta
                    result.addError(new FieldError("proposta", "horarioReuniao", "O horário sugerido para a reunião é obrigatório na contraproposta."));
                }
                if (propostaForm.getLinkReuniao() == null || propostaForm.getLinkReuniao().trim().isEmpty()) { // Link da reunião também é sugerido na contraproposta
                    result.addError(new FieldError("proposta", "linkReuniao", "O link sugerido para a reunião é obrigatório na contraproposta."));
                }
            } else if ("fechar".equals(acaoLojista)) {
                 // Garante que campos de contraproposta e reunião estejam nulos se for FECHAR
                propostaForm.setContrapropostaValor(null);
                propostaForm.setContrapropostaCondicoes(null);
                propostaForm.setHorarioReuniao(null);
                propostaForm.setLinkReuniao(null);
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute("proposta", propostaForm); // Retorna a propostaForm para a view para exibir os erros
            model.addAttribute("veiculo", propostaOriginal.getVeiculo()); // O veículo original é sempre exibido
            attr.addFlashAttribute("fail", "Erro no preenchimento do formulário. Verifique os campos.");
            return "proposta/editar-status";
        }

        // Lógica de atualização de status pela LOJA
        if (propostaForm.getStatus() == StatusProposta.ACEITO) {
            propostaOriginal.setStatus(StatusProposta.ACEITO);
            propostaOriginal.setHorarioReuniao(propostaForm.getHorarioReuniao());
            propostaOriginal.setLinkReuniao(propostaForm.getLinkReuniao());
            // Limpa campos de contraproposta
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            attr.addFlashAttribute("success", "Proposta aceita com sucesso! O cliente será notificado.");

        } else if (propostaForm.getStatus() == StatusProposta.NAO_ACEITO) { // Valor vindo do select box
            if ("contraproposta".equals(acaoLojista)) {
                propostaOriginal.setStatus(StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE); // Mudei para este status
                // A loja pode alterar o valor principal e as condições ou usar os da contraproposta
                propostaOriginal.setValor(propostaForm.getContrapropostaValor() != null ? propostaForm.getContrapropostaValor() : propostaOriginal.getValor());
                propostaOriginal.setCondicoesPagamento(propostaForm.getContrapropostaCondicoes() != null && !propostaForm.getContrapropostaCondicoes().trim().isEmpty() ? propostaForm.getContrapropostaCondicoes() : propostaOriginal.getCondicoesPagamento());
                
                // Os campos de contraproposta são preenchidos
                propostaOriginal.setContrapropostaValor(propostaForm.getContrapropostaValor());
                propostaOriginal.setContrapropostaCondicoes(propostaForm.getContrapropostaCondicoes());
                propostaOriginal.setHorarioReuniao(propostaForm.getHorarioReuniao());
                propostaOriginal.setLinkReuniao(propostaForm.getLinkReuniao());
                attr.addFlashAttribute("success", "Contraproposta enviada ao cliente com sucesso!");

            } else if ("fechar".equals(acaoLojista)) {
                propostaOriginal.setStatus(StatusProposta.RECUSADO_LOJA); // Loja recusou e fechou
                // Limpa todos os campos de contraproposta e reunião
                propostaOriginal.setContrapropostaValor(null);
                propostaOriginal.setContrapropostaCondicoes(null);
                propostaOriginal.setHorarioReuniao(null);
                propostaOriginal.setLinkReuniao(null);
                attr.addFlashAttribute("success", "Proposta recusada e fechada com sucesso!");
            }
        } else {
            // Se o status for enviado de forma inesperada (ex: ABERTO de volta), zere os campos dinâmicos
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            attr.addFlashAttribute("fail", "Status inválido ou ação inesperada. Nenhuma alteração realizada.");
        }

        propostaService.salvar(propostaOriginal);
        return "redirect:/propostas/loja/listar";
    }

    // Cliente: Visualizar e Responder Contrapropostas
    @GetMapping("/cliente/responder/{id}")
    public String responderContrapropostaCliente(@PathVariable("id") Long idProposta, ModelMap model) {
        Proposta proposta = propostaService.buscarPorId(idProposta);
        if (proposta == null || proposta.getCliente() == null || !proposta.getCliente().getId().equals(getClienteLogado().getId())) {
            model.addAttribute("fail", "Proposta não encontrada ou você não tem permissão para acessá-la.");
            return "redirect:/propostas/listar";
        }
        
        // O cliente só pode responder se o status for AGUARDANDO_RESPOSTA_CLIENTE
        if (proposta.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            model.addAttribute("fail", "Esta proposta não está aguardando sua resposta ou já foi finalizada.");
            return "redirect:/propostas/listar";
        }

        model.addAttribute("proposta", proposta);
        return "proposta/resposta-contraproposta"; // Novo HTML para resposta do cliente
    }

    @PostMapping("/cliente/processar-resposta")
    public String processarRespostaContraproposta(@Valid @ModelAttribute("proposta") Proposta propostaForm,
                                                  BindingResult result,
                                                  @RequestParam("acaoCliente") String acaoCliente, // 'aceitar', 'recusar', 'nova'
                                                  RedirectAttributes attr) {
        Proposta propostaOriginal = propostaService.buscarPorId(propostaForm.getId());
        if (propostaOriginal == null || propostaOriginal.getCliente() == null || !propostaOriginal.getCliente().getId().equals(getClienteLogado().getId())) {
            attr.addFlashAttribute("fail", "Proposta não encontrada ou você não tem permissão.");
            return "redirect:/propostas/listar";
        }
        
        // Certifica-se de que a proposta está no estado correto para ser respondida
        if (propostaOriginal.getStatus() != StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
            attr.addFlashAttribute("fail", "Esta proposta não está aguardando sua resposta ou já foi finalizada.");
            return "redirect:/propostas/listar";
        }
        
        // Se a ação for "nova" (fazer nova proposta), precisamos validar os campos da nova proposta
        if ("nova".equals(acaoCliente)) {
            // A propostaForm neste caso contém os dados da nova proposta (valor, condicoesPagamento)
            // Validamos apenas esses campos para a "nova proposta"
            if (propostaForm.getValor() == null || propostaForm.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                 result.addError(new FieldError("proposta", "valor", "O valor da nova proposta deve ser maior que zero."));
            }
            if (propostaForm.getCondicoesPagamento() == null || propostaForm.getCondicoesPagamento().trim().isEmpty()) {
                 result.addError(new FieldError("proposta", "condicoesPagamento", "As condições da nova proposta são obrigatórias."));
            }

            if (result.hasErrors()) {
                attr.addFlashAttribute("fail", "Erro na validação da nova proposta. Verifique os campos.");
                return "redirect:/propostas/cliente/responder/" + propostaForm.getId();
            }
        }

        if ("aceitar".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.ACEITO);
            // Ao aceitar, o valor da contraproposta se torna o valor principal da proposta.
            if (propostaOriginal.getContrapropostaValor() != null) {
                propostaOriginal.setValor(propostaOriginal.getContrapropostaValor());
            }
            if (propostaOriginal.getContrapropostaCondicoes() != null && !propostaOriginal.getContrapropostaCondicoes().trim().isEmpty()) {
                propostaOriginal.setCondicoesPagamento(propostaOriginal.getContrapropostaCondicoes());
            }
            // Limpa os campos de contraproposta após a aceitação
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            
            attr.addFlashAttribute("success", "Contraproposta da loja aceita com sucesso! Verifique os detalhes da reunião.");

        } else if ("recusar".equals(acaoCliente)) {
            propostaOriginal.setStatus(StatusProposta.RECUSADO_CLIENTE); // Cliente recusou e fechou
            // Limpa todos os campos de contraproposta e reunião, pois a proposta foi fechada
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            attr.addFlashAttribute("success", "Contraproposta recusada. Proposta fechada.");

        } else if ("nova".equals(acaoCliente)) {
            // Cliente recusou a contraproposta e está fazendo uma nova proposta
            // A proposta original é marcada como RECUSADO_CLIENTE
            propostaOriginal.setStatus(StatusProposta.RECUSADO_CLIENTE);
            propostaOriginal.setContrapropostaValor(null);
            propostaOriginal.setContrapropostaCondicoes(null);
            propostaOriginal.setHorarioReuniao(null);
            propostaOriginal.setLinkReuniao(null);
            propostaService.salvar(propostaOriginal); // Salva a proposta antiga como fechada

            // Cria uma NOVA proposta com os dados fornecidos pelo cliente
            Proposta novaProposta = new Proposta();
            novaProposta.setCliente(getClienteLogado());
            novaProposta.setVeiculo(propostaOriginal.getVeiculo()); // Mantém o mesmo veículo
            novaProposta.setValor(propostaForm.getValor()); // Novo valor do cliente
            novaProposta.setCondicoesPagamento(propostaForm.getCondicoesPagamento()); // Novas condições do cliente
            novaProposta.setData(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            novaProposta.setStatus(StatusProposta.ABERTO); // Nova proposta está ABERTA para a loja
            
            propostaService.salvar(novaProposta);
            attr.addFlashAttribute("success", "Sua nova proposta foi enviada com sucesso para a loja!");
            return "redirect:/propostas/listar"; // Redireciona para a lista de propostas do cliente
        } else {
            attr.addFlashAttribute("fail", "Ação inválida para a contraproposta.");
            return "redirect:/propostas/listar";
        }

        propostaService.salvar(propostaOriginal);
        return "redirect:/propostas/listar";
    }

}