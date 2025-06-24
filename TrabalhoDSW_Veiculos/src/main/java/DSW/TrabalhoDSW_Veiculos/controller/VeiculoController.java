package DSW.TrabalhoDSW_Veiculos.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.*;
import DSW.TrabalhoDSW_Veiculos.service.spec.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private IVeiculoService veiculoService;

    @Autowired
    private ILojaService lojaService;

    @Autowired
    private IPropostaService propostaService;

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IImagemService imagemService;

    // ==== Métodos Auxiliares ====

    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return lojaService.buscarPorEmail(auth.getName());
    }

    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser") &&
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return clienteService.buscarPorEmail(auth.getName());
        }
        return null;
    }

    // ==== Cadastro ====

    @GetMapping("/cadastrar")
    public String cadastrar(Veiculo veiculo, ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "Loja não identificada.");
            return "redirect:/login";
        }
        veiculo.setLoja(loja);
        return "veiculo/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid Veiculo veiculo, BindingResult result,
                         @RequestParam("fotosUpload") List<MultipartFile> fotosUpload,
                         RedirectAttributes attr) {

        Loja loja = getLojaLogada();
        if (loja == null) {
            attr.addFlashAttribute("fail", "Sessão expirada.");
            return "redirect:/login";
        }
        veiculo.setLoja(loja);

        if (result.hasErrors()) {
            return "veiculo/cadastro";
        }

        if (fotosUpload.size() > 10) {
            attr.addFlashAttribute("fail", "Você pode enviar no máximo 10 imagens.");
            return "veiculo/cadastro";
        }

        List<Imagem> imagens = new ArrayList<>();
        try {
            for (MultipartFile file : fotosUpload) {
                if (!file.isEmpty()) {
                    Imagem imagem = new Imagem();
                    imagem.setNome(file.getOriginalFilename());
                    imagem.setTipo(file.getContentType());
                    imagem.setDados(file.getBytes());
                    imagem.setVeiculo(veiculo);
                    imagens.add(imagem);
                }
            }
        } catch (IOException e) {
            attr.addFlashAttribute("fail", "Erro ao processar imagens.");
            return "veiculo/cadastro";
        }

        veiculo.setFotos(imagens);
        veiculoService.salvar(veiculo);
        attr.addFlashAttribute("success", "Veículo cadastrado com sucesso!");
        return "redirect:/veiculos/meus-veiculos";
    }

    // ==== Edição ====

    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        Veiculo veiculo = veiculoService.buscarPorId(id);
        Loja loja = getLojaLogada();

        if (veiculo == null || loja == null || !veiculo.getLoja().getId().equals(loja.getId())) {
            model.addAttribute("fail", "Permissão negada.");
            return "redirect:/veiculos/meus-veiculos";
        }

        model.addAttribute("veiculo", veiculo);
        return "veiculo/cadastro";
    }

    @PostMapping("/editar")
    public String editar(@Valid Veiculo veiculo, BindingResult result,
                         @RequestParam(value = "fotosUpload", required = false) List<MultipartFile> fotosUpload,
                         RedirectAttributes attr) {

        Loja loja = getLojaLogada();
        if (loja == null) {
            attr.addFlashAttribute("fail", "Sessão expirada.");
            return "redirect:/login";
        }

        Veiculo existente = veiculoService.buscarPorId(veiculo.getId());
        if (existente == null || !existente.getLoja().getId().equals(loja.getId())) {
            attr.addFlashAttribute("fail", "Veículo não encontrado ou não pertence à loja.");
            return "redirect:/veiculos/meus-veiculos";
        }

        if (result.hasErrors()) {
            return "veiculo/cadastro";
        }

        if (fotosUpload != null && !fotosUpload.isEmpty() && !fotosUpload.get(0).isEmpty()) {
            if (fotosUpload.size() > 10) {
                attr.addFlashAttribute("fail", "Você pode enviar no máximo 10 imagens.");
                return "veiculo/cadastro";
            }

            try {
                existente.getFotos().clear(); // Limpa imagens antigas
                for (MultipartFile file : fotosUpload) {
                    Imagem img = new Imagem();
                    img.setNome(file.getOriginalFilename());
                    img.setTipo(file.getContentType());
                    img.setDados(file.getBytes());
                    img.setVeiculo(existente);
                    existente.getFotos().add(img);
                }
            } catch (IOException e) {
                attr.addFlashAttribute("fail", "Erro ao processar imagens.");
                return "veiculo/cadastro";
            }
        }

        existente.setPlaca(veiculo.getPlaca());
        existente.setModelo(veiculo.getModelo());
        existente.setChassi(veiculo.getChassi());
        existente.setAno(veiculo.getAno());
        existente.setQuilometragem(veiculo.getQuilometragem());
        existente.setDescricao(veiculo.getDescricao());
        existente.setValor(veiculo.getValor());

        veiculoService.salvar(existente);
        attr.addFlashAttribute("success", "Veículo atualizado com sucesso!");
        return "redirect:/veiculos/meus-veiculos";
    }

    // ==== Imagem individual ====

    @GetMapping("/imagens/{id}")
    public ResponseEntity<byte[]> getImagem(@PathVariable Long id) {
        Imagem imagem = imagemService.buscarPorId(id);
        if (imagem != null && imagem.getDados() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(imagem.getTipo()));
            headers.setContentLength(imagem.getDados().length);
            return new ResponseEntity<>(imagem.getDados(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ==== Meus veículos ====

    @GetMapping("/meus-veiculos")
    public String listarMeusVeiculos(ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "Loja não identificada.");
            return "redirect:/login";
        }
        model.addAttribute("veiculos", veiculoService.buscarPorLoja(loja));
        return "veiculo/meus-veiculos";
    }

    // ==== Todos veículos ====

    @GetMapping("/listar")
    public String listar(ModelMap model, @RequestParam(required = false) String modelo) {
        List<Veiculo> veiculos = (modelo != null && !modelo.trim().isEmpty())
                ? veiculoService.buscarTodos().stream()
                .filter(v -> v.getModelo().toLowerCase().contains(modelo.toLowerCase()))
                .collect(Collectors.toList())
                : veiculoService.buscarTodos();

        model.addAttribute("veiculos", veiculos);

        Cliente cliente = getClienteLogado();
        if (cliente != null) {
            List<Proposta> propostas = propostaService.buscarTodosPorCliente(cliente);
            Map<Long, Boolean> veiculoComPropostaAberta = propostas.stream()
                    .filter(p -> "ABERTO".equals(p.getStatus()))
                    .collect(Collectors.toMap(
                            p -> p.getVeiculo().getId(),
                            p -> true,
                            (a, b) -> a));
            model.addAttribute("veiculoComPropostaAberta", veiculoComPropostaAberta);
        }

        return "veiculo/lista";
    }

    // ==== Excluir ====

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        veiculoService.excluir(id);
        attr.addFlashAttribute("success", "Veículo excluído com sucesso!");
        return "redirect:/veiculos/meus-veiculos";
    }
}
