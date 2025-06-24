package DSW.TrabalhoDSW_Veiculos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/veiculos")
public class VeiculoController {
    
    @Autowired
    private IVeiculoService veiculoService;
    
    @Autowired
    private ILojaService lojaService;

    
    @GetMapping("/cadastrar")
    public String cadastrar(Veiculo veiculo) {
        return "veiculo/cadastro";
    }
    
    @PostMapping("/salvar")
    public String salvar(@Valid Veiculo veiculo, BindingResult result, RedirectAttributes attr) {
        
        

        if (result.hasErrors()) {
            return "veiculo/cadastro";
        }
        
        veiculoService.salvar(veiculo);
        attr.addFlashAttribute("success", "veiculo.create.sucess");
        return "redirect:/veiculos/listar";
    }
    
    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute(model.addAttribute("veiculo", veiculoService.buscarPorId(id)));
        return "veiculo/cadastro";
    }
    
    @PostMapping("/editar")
    public String editar(@Valid Veiculo veiculo, BindingResult result, RedirectAttributes attr) {
        
        if (result.hasErrors()) {
            return "veiculo/cadastro";
        }

        veiculoService.salvar(veiculo);
        attr.addFlashAttribute("success", "veiculo.edit.sucess");
        return "redirect:/veiculos/listar";
    }
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        veiculoService.excluir(id);
        attr.addFlashAttribute("success", "livro.delete.sucess");
        return "redirect:/veiculos/meus-veiculos";
    }
    
    @ModelAttribute("lojas")
    public List<Loja> getLojas() {
        return lojaService.buscarTodos();
    }
    
    @GetMapping("/listar")
    public String listar(ModelMap model) {
        model.addAttribute("veiculos", veiculoService.buscarTodos());
        return "veiculo/lista";
    }
}