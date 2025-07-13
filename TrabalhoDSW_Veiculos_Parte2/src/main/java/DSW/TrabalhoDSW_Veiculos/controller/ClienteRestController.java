package DSW.TrabalhoDSW_Veiculos.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class ClienteRestController {

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private ILojaService lojaService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping(path = "/clientes")
	public ResponseEntity<List<Cliente>> lista() {
		List<Cliente> lista = clienteService.buscarTodos();
		if (lista.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(lista);
	}

    @GetMapping(path = "/clientes/{id}")
    public ResponseEntity<Cliente> buscaPorId(@PathVariable("id") Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @PostMapping(path = "/clientes")
    @ResponseBody
    public ResponseEntity<Cliente> adiciona(@Valid @RequestBody Cliente cliente, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        if(clienteService.buscarPorCPF(cliente.getCPF()) != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if(clienteService.buscarPorEmail(cliente.getEmail()) != null || lojaService.buscarPorEmail(cliente.getEmail()) != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        

        cliente.setRole("CLIENTE");
        cliente.setEnabled(true);
        cliente.setSenha(encoder.encode(cliente.getSenha()));
        
        clienteService.salvar(cliente);
        return ResponseEntity.ok(cliente);
        
        
    }

    @PutMapping(path = "/clientes/{id}")
    public ResponseEntity<Cliente> atualiza(@PathVariable("id") Long id, @RequestBody Cliente cliente, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        Cliente clienteExistente = clienteService.buscarPorId(id);
        if (clienteExistente == null) {
            return ResponseEntity.notFound().build();
        }
        cliente.setId(id);
        clienteService.salvar(cliente);
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping(path = "/clientes/{id}")
    public ResponseEntity<Boolean> remove(@PathVariable("id") Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        if(clienteService.existePropostasAbertas(id)){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        
        clienteService.excluir(id);
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
        
        
    }

    
}


