package DSW.TrabalhoDSW_Veiculos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;

@RestController
@RequestMapping("/api/lojas")
public class LojaRestController {

    @Autowired
    private ILojaService service;

    @GetMapping
    public ResponseEntity<List<Loja>> listarTodas() {
        List<Loja> lojas = service.buscarTodos();
        return ResponseEntity.ok(lojas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loja> buscarPorId(@PathVariable Long id) {
        Loja loja = service.buscarPorId(id);
        if (loja == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(loja);
    }

    @PostMapping
    public ResponseEntity<Loja> criar(@RequestBody Loja loja) {
        service.salvar(loja);
        return ResponseEntity.created(URI.create("/api/lojas/" + loja.getId())).body(loja);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Loja> atualizar(@PathVariable Long id, @RequestBody Loja lojaAtualizada) {
        Loja loja = service.buscarPorId(id);
        if (loja == null) {
            return ResponseEntity.notFound().build();
        }

        loja.setNome(lojaAtualizada.getNome());
        loja.setEmail(lojaAtualizada.getEmail());
        loja.setCNPJ(lojaAtualizada.getCNPJ());
        loja.setDescricao(lojaAtualizada.getDescricao());

        service.salvar(loja);
        return ResponseEntity.ok(loja);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Loja loja = service.buscarPorId(id);
        if (loja == null) {
            return ResponseEntity.notFound().build();
        }

        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
