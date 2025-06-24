package DSW.TrabalhoDSW_Veiculos.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
import DSW.TrabalhoDSW_Veiculos.domain.Veiculo;
import DSW.TrabalhoDSW_Veiculos.service.spec.IClienteService;
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

    private static String UPLOAD_DIR = "src/main/resources/static/images/uploads/";

    // Método auxiliar para obter a loja logada
    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return lojaService.buscarPorEmail(email);
    }

    // Método auxiliar para obter o cliente logado
    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Verifica se o usuário está autenticado e tem a ROLE_CLIENTE
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser") &&
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            String email = auth.getName();
            return clienteService.buscarPorEmail(email);
        }
        return null;
    }

    @GetMapping("/cadastrar")
    public String cadastrar(Veiculo veiculo, ModelMap model) {
        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null) {
            model.addAttribute("fail", "Não foi possível identificar a loja logada.");
            return "redirect:/login";
        }
        veiculo.setLoja(lojaLogada);
        return "veiculo/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid Veiculo veiculo, BindingResult result,
                         @RequestParam("fotosUpload") List<MultipartFile> fotosUpload,
                         RedirectAttributes attr) {

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null) {
            attr.addFlashAttribute("fail", "Sessão da loja expirada ou loja não identificada. Faça login novamente.");
            return "redirect:/login";
        }
        veiculo.setLoja(lojaLogada);

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.err.println("Validation Error: " + error.getDefaultMessage()));
            return "veiculo/cadastro";
        }

        List<Imagem> imagensPersistidas = new ArrayList<>();
        if (fotosUpload != null && !fotosUpload.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (MultipartFile file : fotosUpload) {
                    if (!file.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath);

                        Imagem imagem = new Imagem();
                        imagem.setCaminho("/images/uploads/" + fileName);
                        imagem.setVeiculo(veiculo);
                        imagensPersistidas.add(imagem);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error uploading files: " + e.getMessage());
                attr.addFlashAttribute("fail", "Erro ao fazer upload das imagens.");
                return "veiculo/cadastro";
            }
        }

        veiculo.setFotos(imagensPersistidas);

        veiculoService.salvar(veiculo);
        attr.addFlashAttribute("success", "veiculo.create.sucess");
        return "redirect:/veiculos/meus-veiculos"; // REDIRECIONAR PARA A NOVA LISTAGEM DE LOJA
    }

    @GetMapping("/editar/{id}")
    public String preEditar(@PathVariable("id") Long id, ModelMap model) {
        Veiculo veiculo = veiculoService.buscarPorId(id);
        if (veiculo == null) {
            model.addAttribute("fail", "Veículo não encontrado para edição.");
            return "redirect:/veiculos/listar";
        }

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null || !veiculo.getLoja().getId().equals(lojaLogada.getId())) {
            model.addAttribute("fail", "Você não tem permissão para editar este veículo.");
            return "redirect:/veiculos/listar";
        }

        model.addAttribute("veiculo", veiculo);
        return "veiculo/cadastro";
    }

    @PostMapping("/editar")
    public String editar(@Valid Veiculo veiculo, BindingResult result,
                         @RequestParam(value = "fotosUpload", required = false) List<MultipartFile> fotosUpload,
                         RedirectAttributes attr) {

        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null) {
            attr.addFlashAttribute("fail", "Sessão da loja expirada ou loja não identificada. Faça login novamente.");
            return "redirect:/login";
        }

        Veiculo existingVeiculo = veiculoService.buscarPorId(veiculo.getId());
        if (existingVeiculo == null || !existingVeiculo.getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "Veículo não encontrado ou você não tem permissão para editá-lo.");
            return "redirect:/veiculos/listar";
        }

        veiculo.setLoja(lojaLogada);

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.err.println("Validation Error (Edit): " + error.getDefaultMessage()));
            return "veiculo/cadastro";
        }

        List<Imagem> imagensAtuais = existingVeiculo.getFotos();
        if (imagensAtuais == null) {
            imagensAtuais = new ArrayList<>();
        }

        if (fotosUpload != null && !fotosUpload.isEmpty() && !fotosUpload.get(0).isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                imagensAtuais.clear();

                for (MultipartFile file : fotosUpload) {
                    if (!file.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath);

                        Imagem imagem = new Imagem();
                        imagem.setCaminho("/images/uploads/" + fileName);
                        imagem.setVeiculo(existingVeiculo);
                        imagensAtuais.add(imagem);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error uploading files during edit: " + e.getMessage());
                attr.addFlashAttribute("fail", "Erro ao fazer upload das novas imagens.");
                return "veiculo/cadastro";
            }
        }
        existingVeiculo.setFotos(imagensAtuais);

        existingVeiculo.setPlaca(veiculo.getPlaca());
        existingVeiculo.setModelo(veiculo.getModelo());
        existingVeiculo.setChassi(veiculo.getChassi());
        existingVeiculo.setAno(veiculo.getAno());
        existingVeiculo.setQuilometragem(veiculo.getQuilometragem());
        existingVeiculo.setDescricao(veiculo.getDescricao());
        existingVeiculo.setValor(veiculo.getValor());

        veiculoService.salvar(existingVeiculo);
        attr.addFlashAttribute("success", "veiculo.edit.sucess");
        return "redirect:/veiculos/meus-veiculos"; // REDIRECIONAR PARA A NOVA LISTAGEM DE LOJA
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        Veiculo veiculo = veiculoService.buscarPorId(id);
        Loja lojaLogada = getLojaLogada();
        if (veiculo == null || lojaLogada == null || !veiculo.getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "Você não tem permissão para excluir este veículo.");
            return "redirect:/veiculos/listar";
        }

        veiculoService.excluir(id);
        attr.addFlashAttribute("success", "veiculo.delete.sucess");
        return "redirect:/veiculos/meus-veiculos"; // REDIRECIONAR PARA A NOVA LISTAGEM DE LOJA
    }


    // MÉTODO EXISTENTE: Listagem de TODOS os veículos (pública, com filtro para clientes/visitantes)
    @GetMapping("/listar")
    public String listar(ModelMap model, @RequestParam(required = false) String modelo) {
        List<Veiculo> veiculos;

        if (modelo != null && !modelo.trim().isEmpty()) {
            veiculos = veiculoService.buscarTodos().stream()
                                  .filter(v -> v.getModelo().toLowerCase().contains(modelo.toLowerCase()))
                                  .collect(Collectors.toList());
        } else {
            veiculos = veiculoService.buscarTodos();
        }

        model.addAttribute("veiculos", veiculos);

        Cliente clienteLogado = getClienteLogado();
        if (clienteLogado != null) {
            List<Proposta> propostasDoCliente = propostaService.buscarTodosPorCliente(clienteLogado);

            Map<Long, Boolean> veiculoComPropostaAberta = propostasDoCliente.stream()
                .filter(p -> "ABERTO".equals(p.getStatus()))
                .collect(Collectors.toMap(
                    p -> p.getVeiculo().getId(),
                    p -> true,
                    (existing, replacement) -> existing
                ));
            model.addAttribute("veiculoComPropostaAberta", veiculoComPropostaAberta);
        }

        return "veiculo/lista"; // Continua apontando para veiculo/lista
    }

    // NOVO MÉTODO: Listagem de veículos APENAS da loja logada
    @GetMapping("/meus-veiculos")
    public String listarMeusVeiculos(ModelMap model) {
        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null) {
            model.addAttribute("fail", "Não foi possível identificar a loja logada.");
            return "redirect:/login"; // Ou página de erro/home
        }
        // Assumindo que você tem um método para buscar veículos por loja no VeiculoService ou DAO
        // Se não tiver, precisará criar: List<Veiculo> findByLoja(Loja loja);
        model.addAttribute("veiculos", veiculoService.buscarPorLoja(lojaLogada));
        return "veiculo/meus-veiculos"; // Aponta para o novo arquivo HTML
    }
}