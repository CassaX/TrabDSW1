package DSW.TrabalhoDSW_Veiculos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult; // Importe
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import java.util.Arrays;


@Controller
@RequestMapping("/loja")
public class LojaController {
    
    
    private static final Logger logger = LoggerFactory.getLogger(LojaController.class); // Adicione esta linha

    @Autowired
    private ILojaService service;
    
    @GetMapping("/cadastrar")
    public String cadastrar(Loja loja, ModelMap model) {
        return "loja/cadastro";
    }
    
     @Autowired
	private BCryptPasswordEncoder encoder;


    @PostMapping("/salvar")
    public String salvar(@Valid Loja loja, BindingResult result, RedirectAttributes attr, ModelMap model) {
        if (result.hasErrors()) {
            logger.warn("Erros de validação encontrados ao tentar salvar a loja:");
            result.getAllErrors().forEach(error -> {
                logger.warn("  - Mensagem de Erro: " + error.getDefaultMessage());
                if (error instanceof FieldError) { // Use FieldError para acessar detalhes do campo
                    FieldError fieldError = (FieldError) error;
                    logger.warn("    Campo: " + fieldError.getField() + ", Valor Rejeitado: " + fieldError.getRejectedValue() + ", Códigos: " + Arrays.toString(fieldError.getCodes()));
                }
            });
        }
        loja.setRole("LOJA");
        loja.setEnabled(true);

        System.out.println("password = " + loja.getSenha());
		loja.setSenha(encoder.encode(loja.getSenha()));
        service.salvar(loja); 
        attr.addFlashAttribute("success", "Loja cadastrada com sucesso.");
        return "redirect:/loja/listar";
    }



    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("loja", service.buscarPorId(id));
        return "loja/cadastro";
    }
    
    @PostMapping("/editar")
    public String editar(@Valid Loja loja, String novaSenha, BindingResult result, RedirectAttributes attr, ModelMap model) {
        if(result.getFieldErrorCount() > 1 || result.getFieldError("CNPJ")==null) {
            return "loja/cadastro";
        }
        service.salvar(loja); 
        attr.addFlashAttribute("success", "Loja editada com sucesso.");
        return "redirect:/loja/listar";
    }
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        service.excluir(id);
        attr.addFlashAttribute("success", "Loja removida com sucesso.");
        return "redirect:/loja/listar";
    }
    
    @GetMapping("/listar")
    public String listar(ModelMap model) {
        model.addAttribute("lojas", service.buscarTodos());
        return "loja/lista";
    }
    
    /* 
    @GetMapping("/perfil")
    public String perfil(ModelMap model, RedirectAttributes attr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Loja loja = service.buscarPorEmail(email);
        
        if (loja == null || !"LOJA".equals(loja.getRole())) { // Salvaguarda para garantir que é uma loja
            System.err.println("Erro: Usuário logado como " + email + " tentou acessar perfil de loja, mas não foi encontrado ou não tem ROLE_LOJA.");
            attr.addFlashAttribute("fail", "Seu perfil de loja não foi encontrado ou você não tem permissão para acessá-lo.");
            // Redireciona para o perfil apropriado ou home
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                return "redirect:/cliente/perfil";
            } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/home";
        }

        model.addAttribute("loja", loja);
        return "loja/perfil";
    }
    */
}