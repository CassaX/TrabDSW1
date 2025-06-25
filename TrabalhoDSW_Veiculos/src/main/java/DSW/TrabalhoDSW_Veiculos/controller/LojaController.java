package DSW.TrabalhoDSW_Veiculos.controller;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/loja")
public class LojaController {
    
    private static final Logger logger = LoggerFactory.getLogger(LojaController.class);

    @Autowired
    private ILojaService service;
    
    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping("/cadastrar")
    public String cadastrar(Loja loja, ModelMap model) {
        return "loja/cadastro";
    }
    
    @PostMapping("/salvar")
    public String salvar(@Valid Loja loja, BindingResult result, RedirectAttributes attr, ModelMap model) {
        if (loja.getSenha() == null || loja.getSenha().trim().isEmpty()) {
            result.addError(new FieldError("loja", "senha", "loja.senha.notblank")); // Mensagem de erro via chave
        }
        
        if (service.buscarPorCNPJ(loja.getCNPJ()) != null) { 
            result.addError(new FieldError("loja", "CNPJ", "loja.cnpj.cadastrado")); // Mensagem de erro via chave
        }

        if (service.buscarPorEmail(loja.getEmail()) != null) {
            result.addError(new FieldError("loja", "email", "loja.email.cadastrado")); // Mensagem de erro via chave
        }

        if (result.hasErrors()) {
            logger.warn("Erros de validação encontrados ao tentar salvar a loja:");
            result.getAllErrors().forEach(error -> {
                logger.warn("   - Mensagem de Erro: " + error.getDefaultMessage());
                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error;
                    logger.warn("     Campo: " + fieldError.getField() + ", Valor Rejeitado: " + fieldError.getRejectedValue() + ", Códigos: " + Arrays.toString(fieldError.getCodes()));
                }
            });
            attr.addFlashAttribute("fail", "loja.create.fail"); // Mensagem geral de falha no cadastro
            return "loja/cadastro"; 
        }
        
        loja.setRole("LOJA");
        loja.setEnabled(true);
        loja.setSenha(encoder.encode(loja.getSenha()));
        service.salvar(loja); 
        attr.addFlashAttribute("success", "loja.create.success"); // Chave para mensagem de sucesso
        return "redirect:/loja/listar";
    }


    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("loja", service.buscarPorId(id));
        return "loja/cadastro";
    }
    
    @PostMapping("/editar")
    public String editar(@Valid Loja loja, @RequestParam(value = "novaSenha", required = false) String novaSenha, BindingResult result, RedirectAttributes attr, ModelMap model) {
        
        Loja lojaOriginal = service.buscarPorId(loja.getId());
        if (lojaOriginal == null) {
            attr.addFlashAttribute("fail", "loja.notfound"); // Mensagem de loja não encontrada
            return "redirect:/loja/listar";
        }

        // VALIDAÇÃO DE UNICIDADE DE CNPJ PARA EDIÇÃO
        Loja lojaByCNPJ = service.buscarPorCNPJ(loja.getCNPJ());
        if (lojaByCNPJ != null && !lojaByCNPJ.getId().equals(loja.getId())) {
            result.addError(new FieldError("loja", "CNPJ", "loja.cnpj.cadastradoOutro")); // Mensagem de erro via chave
        }

        // Validação de unicidade de email para edição
        Loja lojaByEmail = service.buscarPorEmail(loja.getEmail());
        if (lojaByEmail != null && !lojaByEmail.getId().equals(loja.getId())) {
            result.addError(new FieldError("loja", "email", "loja.email.cadastradoOutro")); // Mensagem de erro via chave
        }

        // Lógica da Senha
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            loja.setSenha(encoder.encode(novaSenha));
        } else {
            loja.setSenha(lojaOriginal.getSenha()); 
        }

        if (result.hasErrors()) {
            loja.setRole(lojaOriginal.getRole());
            loja.setEnabled(lojaOriginal.isEnabled());
            attr.addFlashAttribute("fail", "loja.edit.fail"); // Mensagem geral de falha na edição
            return "loja/cadastro"; 
        }
        
        // Copiar outros dados que não vêm do formulário diretamente (role, enabled)
        loja.setRole(lojaOriginal.getRole());
        loja.setEnabled(lojaOriginal.isEnabled());

        service.salvar(loja); 
        attr.addFlashAttribute("success", "loja.edit.success"); // Chave para mensagem de sucesso
        return "redirect:/loja/listar";
    }
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        service.excluir(id);
        attr.addFlashAttribute("success", "loja.delete.success"); // Chave para mensagem de sucesso
        return "redirect:/loja/listar";
    }
    
    @GetMapping("/listar")
    public String listar(ModelMap model) {
        model.addAttribute("lojas", service.buscarTodos());
        return "loja/lista";
    }
}