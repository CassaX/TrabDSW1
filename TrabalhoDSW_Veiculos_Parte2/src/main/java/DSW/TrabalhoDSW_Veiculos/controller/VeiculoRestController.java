package DSW.TrabalhoDSW_Veiculos.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;
import jakarta.validation.Valid;

@RestController
public class VeiculoRestController {

    @Autowired
    private IVeiculoService service;

    @Autowired
    private ILojaService lojaService;

    @PostMapping(path = "/api/veiculos/lojas/{id}")
    @ResponseBody
    public ResponseEntity<Veiculo> cria(@PathVariable("id") long lojaId, 
                                  @Valid @RequestBody Veiculo veiculo,
                                  BindingResult result) {
        
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(null);
        }
        
        veiculo.setLoja(lojaService.buscarPorId(lojaId));
        service.salvar(veiculo);
        return ResponseEntity.ok(veiculo);
    }

    @GetMapping(path = "/api/veiculos/lojas/{id}")
    public ResponseEntity<List<Veiculo>> veiculoPorLoja(@PathVariable("id") long id) {
        List<Veiculo> lista = service.buscarPorLoja(lojaService.buscarPorId(id));
        if (lista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lista);
    }

    @GetMapping(path = "/api/veiculos/modelos/{nome}")
    public ResponseEntity<List<Veiculo>> listaPorModelo(@PathVariable("nome") String modelo) {
        List<Veiculo> lista = service.buscarPorModeloCompleto(modelo);
        if (lista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lista);
    }
}