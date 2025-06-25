package DSW.TrabalhoDSW_Veiculos.controller;

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
        // Validação da senha no cadastro 
        if (cliente.getSenha() == null || cliente.getSenha().trim().isEmpty()) {
            result.addError(new FieldError("cliente", "senha", "cliente.senha.notblank"));
        }
        
        // VALIDAÇÃO DE UNICIDADE DE CPF PARA CADASTRO
        if (service.buscarPorCPF(cliente.getCPF()) != null) { 
            result.addError(new FieldError("cliente", "CPF", "cliente.cpf.cadastrado")); 
        }

        // Validação de unicidade de email para cadastro (se email já existe)
        if (service.buscarPorEmail(cliente.getEmail()) != null) {
            result.addError(new FieldError("cliente", "email", "cliente.email.cadastrado"));
        }

        if (result.hasErrors()) {
            attr.addFlashAttribute("fail", "cliente.create.fail");
            return "cliente/cadastro"; 
        }
        
        cliente.setRole("CLIENTE");
        cliente.setEnabled(true);
        cliente.setSenha(encoder.encode(cliente.getSenha()));
        service.salvar(cliente);
        attr.addFlashAttribute("success", "cliente.create.success");
        return "redirect:/cliente/listar";
    }
    
    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("cliente", service.buscarPorId(id));
        return "cliente/cadastro";
    }
    
    @PostMapping("/editar")
    public String editar(@Valid Cliente cliente, @RequestParam(value = "novaSenha", required = false) String novaSenha, BindingResult result, RedirectAttributes attr, ModelMap model) {
        
        Cliente clienteOriginal = service.buscarPorId(cliente.getId());
        if (clienteOriginal == null) {
            attr.addFlashAttribute("fail", "cliente.notfound"); 
        }

        // VALIDAÇÃO DE UNICIDADE DE CPF PARA EDIÇÃO
        Cliente clienteByCPF = service.buscarPorCPF(cliente.getCPF());
        if (clienteByCPF != null && !clienteByCPF.getId().equals(cliente.getId())) { 
            result.addError(new FieldError("cliente", "CPF", "cliente.cpf.cadastradoOutro")); 
        }

        // Validação de unicidade de email para edição
        Cliente clienteByEmail = service.buscarPorEmail(cliente.getEmail());
        if (clienteByEmail != null && !clienteByEmail.getId().equals(cliente.getId())) {
            result.addError(new FieldError("cliente", "email", "cliente.email.cadastradoOutro"));
        }

        // Lógica da Senha
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            cliente.setSenha(encoder.encode(novaSenha)); 
        } else {
            cliente.setSenha(clienteOriginal.getSenha()); 
        }

        if (result.hasErrors()) {
            cliente.setRole(clienteOriginal.getRole());
            cliente.setEnabled(clienteOriginal.isEnabled());
            attr.addFlashAttribute("fail", "cliente.edit.fail"); // Mensagem geral de falha na edição
            return "cliente/cadastro"; 
        }
        
        cliente.setRole(clienteOriginal.getRole());
        cliente.setEnabled(clienteOriginal.isEnabled());

        service.salvar(cliente);
        attr.addFlashAttribute("success", "cliente.edit.success"); // Chave para mensagem de sucesso
        return "redirect:/cliente/listar";
    }
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {

        service.excluir(id);
        attr.addFlashAttribute("success", "cliente.delete.success"); // Chave para mensagem de sucesso
        return "redirect:/cliente/listar";
    }
    
    @GetMapping("/listar")
    public String listar(ModelMap model) {
        model.addAttribute("clientes", service.buscarTodos());
        return "cliente/lista";
    }
}