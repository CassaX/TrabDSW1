package DSW.TrabalhoDSW_Veiculos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.service.spec.IImagemService;

@Controller
public class ImagemController {

    @Autowired
    private IImagemService imagemService;

    @GetMapping("/imagens/{id}")
    public ResponseEntity<byte[]> exibirImagem(@PathVariable("id") Long id) {
        Imagem imagem = imagemService.buscarPorId(id);
        if (imagem == null || imagem.getDados() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, imagem.getTipo())
                .contentLength(imagem.getDados().length)
                .body(imagem.getDados());
    }
}
