package DSW.TrabalhoDSW_Veiculos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.service.spec.IImagemService;

@RestController
@RequestMapping("/imagens")
public class ImagemController {

    @Autowired
    private IImagemService imagemService;

    /* 
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImagem(@PathVariable("id") Long id) {
        Imagem imagem = IImagemService.buscarPorId(id);
        if (imagem == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, imagem.getTipo())
                .body(imagem.getDados());
    }
                */
}