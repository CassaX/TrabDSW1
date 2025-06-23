package DSW.TrabalhoDSW_Veiculos.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class HomeController {

     @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }
     
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    

    
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }
}