package DSW.TrabalhoDSW_Veiculos.controller;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/loja")
public class LojaController {

    private final MessageSource messageSource;

    private static final Logger logger = LoggerFactory.getLogger(LojaController.class);

    @Autowired
    private ILojaService service;

    @Autowired
    private IVeiculoService veiculoService;

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IPropostaService propostaService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public LojaController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/cadastrar")
    public String cadastrar(Loja loja, ModelMap model) {
        return "loja/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid Loja loja, BindingResult result, RedirectAttributes attr, ModelMap model) {

        if (service.buscarPorCNPJ(loja.getCNPJ()) != null) {
            result.addError(new FieldError("loja", "cnpj", null, false, new String[]{"loja.cnpj.cadastrado"}, null, null));
        }

        Loja lojaByEmail = service.buscarPorEmail(loja.getEmail());
        boolean emailExiste = lojaByEmail != null;
        boolean clienteEmailExiste = clienteService.buscarPorEmail(loja.getEmail()) != null;

        if (emailExiste || clienteEmailExiste) {
            result.addError(new FieldError("loja", "email", null, false, new String[]{"email.cadastrado"}, null, null));
        }

        if (result.hasErrors()) {
            logger.warn(messageSource.getMessage("log.validacao.erros.salvar", null, LocaleContextHolder.getLocale()));
            result.getAllErrors().forEach(error -> {
                logger.warn(messageSource.getMessage("log.validacao.mensagem",
                        new Object[]{error.getDefaultMessage()}, LocaleContextHolder.getLocale()));
                if (error instanceof FieldError fieldError) {
                    logger.warn(messageSource.getMessage("log.validacao.detalhes.campo",
                            new Object[]{fieldError.getField(), fieldError.getRejectedValue(), Arrays.toString(fieldError.getCodes())},
                            LocaleContextHolder.getLocale()));
                }
            });
            attr.addFlashAttribute("fail", "loja.create.fail");
            return "loja/cadastro";
        }

        loja.setRole("LOJA");
        loja.setEnabled(true);
        loja.setSenha(encoder.encode(loja.getSenha()));
        service.salvar(loja);
        attr.addFlashAttribute("success", "loja.create.success");
        return "redirect:/loja/listar";
    }

    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("loja", service.buscarPorId(id));
        return "loja/cadastro";
    }

    @PostMapping("/editar")
    public String editar(@Valid Loja loja, @RequestParam(value = "novaSenha", required = false) String novaSenha,
                         BindingResult result, RedirectAttributes attr, ModelMap model) {

        Loja lojaOriginal = service.buscarPorId(loja.getId());
        if (lojaOriginal == null) {
            attr.addFlashAttribute("fail", "loja.notfound");
            return "redirect:/loja/listar";
        }

        Loja lojaByCNPJ = service.buscarPorCNPJ(loja.getCNPJ());
        if (lojaByCNPJ != null && !lojaByCNPJ.getId().equals(loja.getId())) {
            result.addError(new FieldError("loja", "cnpj", null, false, new String[]{"loja.cnpj.cadastradoOutro"}, null, null));
        }

        Loja lojaByEmail = service.buscarPorEmail(loja.getEmail());
        boolean emailExiste = lojaByEmail != null && !lojaByEmail.getId().equals(loja.getId());
        boolean clienteEmailExiste = clienteService.buscarPorEmail(loja.getEmail()) != null
                && !clienteService.buscarPorEmail(loja.getEmail()).getId().equals(loja.getId());

        if (emailExiste || clienteEmailExiste) {
            result.addError(new FieldError("loja", "email", null, false, new String[]{"loja.email.cadastradoOutro"}, null, null));
        }

        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            loja.setSenha(encoder.encode(novaSenha));
        } else {
            loja.setSenha(lojaOriginal.getSenha());
        }

        if (result.hasErrors()) {
            loja.setRole(lojaOriginal.getRole());
            loja.setEnabled(lojaOriginal.isEnabled());
            attr.addFlashAttribute("fail", "loja.edit.fail");
            return "loja/cadastro";
        }

        loja.setRole(lojaOriginal.getRole());
        loja.setEnabled(lojaOriginal.isEnabled());

        service.salvar(loja);
        attr.addFlashAttribute("success", "loja.edit.success");
        return "redirect:/loja/listar";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        Loja loja = service.buscarPorId(id);
        if (loja == null) {
            attr.addFlashAttribute("fail", "loja.notfound");
            return "redirect:/loja/listar";
        }

        boolean temVeiculos = !veiculoService.buscarPorLoja(loja).isEmpty();
        boolean temPropostas = propostaService.buscarTodosPorLoja(loja) != null && !propostaService.buscarTodosPorLoja(loja).isEmpty();

        if (!temVeiculos && !temPropostas) {
            service.excluir(id);
            attr.addFlashAttribute("success", "loja.delete.success");
        } else {
            attr.addFlashAttribute("fail", "loja.delete.propostaOuVeiculo");
        }
        return "redirect:/loja/listar";
    }


    @GetMapping("/listar")
    public String listar(ModelMap model) {
        model.addAttribute("lojas", service.buscarTodos());
        return "loja/lista";
    }
}
