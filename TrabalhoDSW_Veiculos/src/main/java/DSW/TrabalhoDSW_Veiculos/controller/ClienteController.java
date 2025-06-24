package DSW.TrabalhoDSW_Veiculos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller; // Importe (se for usado)
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping; // Importe
import org.springframework.web.bind.annotation.PathVariable; // Importe
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cliente")
public class ClienteController {
    
    @Autowired
    private IClienteService service;

    @Autowired
	private BCryptPasswordEncoder encoder;

    @GetMapping("/cadastrar")
    public String cadastrar(Cliente cliente, ModelMap model) {
        return "cliente/cadastro";
    }
    
    @PostMapping("/salvar")
    public String salvar(@Valid Cliente cliente, BindingResult result, RedirectAttributes attr, ModelMap model) {
        
        if (result.hasErrors()) {
            return "cliente/cadastro";
        }
        
        System.out.println("password = " + cliente.getSenha());
		cliente.setSenha(encoder.encode(cliente.getSenha()));
		service.salvar(cliente);
		attr.addFlashAttribute("sucess", "cliente.create.sucess");
		return "redirect:/cliente/listar";
    }
    
    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("cliente", service.buscarPorId(id));
        return "cliente/cadastro";
    }
    
    @PostMapping("/editar")
	public String editar(@Valid Cliente cliente, String novoPassword, BindingResult result, RedirectAttributes attr, ModelMap model) {
		
		if (result.hasErrors()) {
			return "cliente/cadastro";
		}
        if (novoPassword != null && !novoPassword.trim().isEmpty()) {
            cliente.setSenha(encoder.encode(novoPassword)); 
            System.out.println("Senha não foi alterada, mantendo a atual.");
        }

        service.salvar(cliente);
        attr.addFlashAttribute("success", "cliente.edit.sucess");
        return "redirect:/cliente/listar";
		
	}
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        service.excluir(id);
        attr.addFlashAttribute("success", "cliente.delete.sucess");
        return "redirect:/cliente/listar";
    }
    
    @GetMapping("/listar")
    public String listar(ModelMap model) {
        model.addAttribute("clientes", service.buscarTodos());
        return "cliente/lista";
    }
    /* 
    @GetMapping("/perfil")
    public String perfil(ModelMap model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Cliente cliente = service.findByEmail(email);
        model.addAttribute("cliente", cliente);
        return "cliente/perfil";
    }
    */
}