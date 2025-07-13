package DSW.TrabalhoDSW_Veiculos.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;

@RestController
public class PropostaRestController {

    @Autowired
    private IPropostaService service;

    @Autowired
    private IVeiculoService VeiculoService;
    
    @Autowired
    private IClienteService ClienteService;

    @GetMapping(path = "/api/propostas/veiculos/{id}")
    public ResponseEntity<List<Proposta>> lista(@PathVariable("id") long id) {
        List<Proposta> lista = service.buscarPorVeiculo(VeiculoService.buscarPorId(id));
        if (lista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping(path = "/api/propostas/clientes/{id}")
    public ResponseEntity<List<Proposta>> listaPorCliente(@PathVariable("id") long id) {
        List<Proposta> lista = service.buscarPorCliente(ClienteService.buscarPorId(id));
        if (lista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lista);
    }
}