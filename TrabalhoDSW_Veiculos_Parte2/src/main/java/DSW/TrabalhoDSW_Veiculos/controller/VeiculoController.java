package DSW.TrabalhoDSW_Veiculos.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import DSW.TrabalhoDSW_Veiculos.domain.Imagem;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import DSW.TrabalhoDSW_Veiculos.domain.Proposta;
import DSW.TrabalhoDSW_Veiculos.domain.StatusProposta;
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IImagemService;
import DSW.TrabalhoDSW_Veiculos.service.spec.ILojaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IPropostaService;
import DSW.TrabalhoDSW_Veiculos.service.spec.IVeiculoService;
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

    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return lojaService.buscarPorEmail(auth.getName());
        }
        return null; 
    }

    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser") &&
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return clienteService.buscarPorEmail(auth.getName());
        }
        return null; 
    }



    @GetMapping("/cadastrar")
    public String cadastrar(Veiculo veiculo, ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "loja.nao.identificada");
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
            attr.addFlashAttribute("fail", "sessao.expirada");
            return "redirect:/login";
        }
        veiculo.setLoja(loja);
    
        if (result.hasErrors()) {
            return "veiculo/cadastro";
        }
        veiculoService.salvar(veiculo); 

        
        if (fotosUpload != null && !fotosUpload.isEmpty()) {
            for (MultipartFile foto : fotosUpload) {
                if (foto != null && !foto.isEmpty()) {
                    try {
                        Imagem imagem = new Imagem(
                            StringUtils.cleanPath(foto.getOriginalFilename()),
                            foto.getContentType(),
                            foto.getBytes()
                        );
                        imagem.setVeiculo(veiculo);
                        imagemService.salvar(imagem);
                    } catch (IOException e) {
                        e.printStackTrace();
                        attr.addFlashAttribute("fail", "erro.processar.imagem");
                        return "veiculo/cadastro";
                    }
                }
            }
        }
        attr.addFlashAttribute("success", "veiculo.cadastro.success");
        return "redirect:/veiculos/meus-veiculos";
    }


    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        Veiculo veiculo = veiculoService.buscarPorId(id);
        Loja loja = getLojaLogada();

        if (veiculo == null || loja == null || !veiculo.getLoja().getId().equals(loja.getId())) {
            model.addAttribute("fail", "veiculo.editar.permissao");
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
            attr.addFlashAttribute("fail", "sessao.expirada");
            return "redirect:/login";
        }

        Veiculo existente = veiculoService.buscarPorId(veiculo.getId());
        if (existente == null || !existente.getLoja().getId().equals(loja.getId())) {
            attr.addFlashAttribute("fail", "veiculo.nao.encontrado");
            return "redirect:/veiculos/meus-veiculos";
        }
        

        if (fotosUpload == null || fotosUpload.isEmpty() || fotosUpload.get(0).isEmpty()) {
             veiculo.setFotos(existente.getFotos()); 
        }

        if (result.hasErrors()) {
            veiculo.setLoja(loja);
            veiculo.setFotos(existente.getFotos()); 
            return "veiculo/cadastro";
        }

        if (fotosUpload != null && !fotosUpload.isEmpty() && !fotosUpload.get(0).isEmpty()) {
            if (fotosUpload.size() > 10) {
                attr.addFlashAttribute("fail", "veiculo.maximo.imagens");

                veiculo.setFotos(existente.getFotos());
                return "veiculo/cadastro";
            }

            if (existente.getFotos() == null) {
                existente.setFotos(new ArrayList<>());
            } else {
                existente.getFotos().clear();
            }
            

            for (MultipartFile foto : fotosUpload) {
                if (foto != null && !foto.isEmpty()) {
                    try {
                        Imagem imagem = new Imagem(
                            StringUtils.cleanPath(foto.getOriginalFilename()),
                            foto.getContentType(),
                            foto.getBytes()
                        );
                        imagem.setVeiculo(veiculo);
                        imagemService.salvar(imagem);
                    } catch (IOException e) {
                        attr.addFlashAttribute("fail", "Erro ao processar imagens: " + e.getMessage());
                        veiculo.setFotos(existente.getFotos()); 
                        return "veiculo/cadastro";
                    }
                }
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
        
        attr.addFlashAttribute("success", "veiculo.atualizado.success");
        return "redirect:/veiculos/meus-veiculos";
    }


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


    @GetMapping("/meus-veiculos")
    public String listarMeusVeiculos(ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "loja.nao.identificada");
            return "redirect:/login";
        }
        
        List<Veiculo> veiculosDaLoja = veiculoService.buscarPorLoja(loja);
        
        List<Veiculo> veiculos = veiculosDaLoja.stream()
                .filter(veiculo -> {
                    List<Proposta> propostasDoVeiculo = propostaService.buscarPorVeiculo(veiculo);
                    return propostasDoVeiculo.stream()
                            .noneMatch(p -> p.getStatus() == StatusProposta.ACEITO);
                })
                .collect(Collectors.toList());
        
        model.addAttribute("veiculos", veiculos);
        return "veiculo/meus-veiculos";
    }


    @GetMapping("/listar")
    public String listar(ModelMap model, @RequestParam(required = false) String modelo) {
        List<Veiculo> todosVeiculos = (modelo != null && !modelo.trim().isEmpty())
                ? veiculoService.buscarTodos().stream()
                .filter(v -> v.getModelo().toLowerCase().contains(modelo.toLowerCase()))
                .collect(Collectors.toList())
                : veiculoService.buscarTodos();

                List<Veiculo> veiculos = todosVeiculos.stream()
                .filter(veiculo -> {
                    List<Proposta> propostasDoVeiculo = propostaService.buscarPorVeiculo(veiculo);
                    return propostasDoVeiculo.stream()
                            .noneMatch(p -> p.getStatus() == StatusProposta.ACEITO);
                })
                .collect(Collectors.toList());
    
    

        model.addAttribute("veiculos", veiculos);

        Cliente cliente = getClienteLogado();
        if (cliente != null) {
            Map<Long, Proposta> propostasAtivasDoClientePorVeiculo = new HashMap<>();
            List<Proposta> propostasDoCliente = propostaService.buscarTodosPorCliente(cliente);
            
            for (Proposta proposta : propostasDoCliente) {
                if (proposta.getStatus() == StatusProposta.ABERTO || 
                    proposta.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
                    propostasAtivasDoClientePorVeiculo.put(proposta.getVeiculo().getId(), proposta);
                }
            }
            model.addAttribute("veiculoComPropostaAberta", propostasAtivasDoClientePorVeiculo); 
        }



        return "veiculo/lista";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        Loja loja = getLojaLogada();
        Veiculo veiculo = veiculoService.buscarPorId(id);

        if (veiculo == null || loja == null || !veiculo.getLoja().getId().equals(loja.getId())) {
            attr.addFlashAttribute("fail", "veiculo.excluir.permissao");
            return "redirect:/veiculos/meus-veiculos";
        }


        veiculoService.excluir(id);
        attr.addFlashAttribute("success", "veiculo.excluir.success");
        return "redirect:/veiculos/meus-veiculos";
    }
}
