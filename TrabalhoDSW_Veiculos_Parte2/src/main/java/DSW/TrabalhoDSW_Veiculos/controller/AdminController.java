package DSW.TrabalhoDSW_Veiculos.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private IClienteService clienteService;
    
    @Autowired
    private ILojaService lojaService;


    @GetMapping("/dashboard")
public String dashboard(Model model) {
    model.addAttribute("clientes", clienteService.buscarTodos());
    model.addAttribute("lojas", lojaService.buscarTodos());
    return "admin/dashboard";
}

    @GetMapping("/clientes")
    public String gerenciarClientes() {
        return "redirect:cliente/listar";
    }

    @GetMapping("/lojas")
    public String gerenciarLojas() {
        return "redirect:loja/listar";
    }
}