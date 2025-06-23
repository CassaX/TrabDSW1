package DSW.TrabalhoDSW_Veiculos.controller;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; 
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

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.impl.NotificacaoPropostaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/propostas")
public class PropostaController {
    
    @Autowired
    private IPropostaService service;
    
    @Autowired
    private IVeiculoService veiculoService;

    @GetMapping("/cadastrar/{id}")
    public String cadastrar(Proposta proposta) {
        String data = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        proposta.setData(data);
        proposta.setValor(new BigDecimal(0));
        proposta.setCondicoesPagamento(this.getCondicoesPagamento());
        proposta.setStatus(this.getStatus());

        return "proposta/cadastro";
    }

    private Cliente getCliente(){
        ClienteDetails clienteDetails=(ClienteDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return clienteDetails.getCliente();
    }

    @GetMapping("/listar")
    public String listar(ModelMap model){
        model.addAttribute("propostas", service.buscarTodosPorCliente(getCliente()));
        return "proposta/listar";
    }

    @GetMapping("/salvar/{idVeiculo}")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
    
    @ModelAttribute("veiculos")
	public List<Veiculo> listaVeiculo() {
		return veiculoService.buscarTodos();
	}
    
}