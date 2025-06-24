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

    // A constante UPLOAD_DIR não é mais necessária, pois não estamos salvando em arquivos.
    // private static String UPLOAD_DIR = "src/main/resources/static/images/uploads/";

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
                for (MultipartFile file : fotosUpload) {
                    if (!file.isEmpty()) {
                        Imagem imagem = new Imagem();
                        imagem.setNome(file.getOriginalFilename()); // Nome original do arquivo
                        imagem.setTipo(file.getContentType());    // Tipo do conteúdo (e.g., image/jpeg)
                        imagem.setDados(file.getBytes());         // Dados da imagem em byte[]
                        imagem.setVeiculo(veiculo);               // Associa a imagem ao veículo
                        imagensPersistidas.add(imagem);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing image files: " + e.getMessage());
                attr.addFlashAttribute("fail", "Erro ao processar as imagens.");
                return "veiculo/cadastro";
            }
        }

        veiculo.setFotos(imagensPersistidas); // Define a lista de imagens para o veículo
        veiculoService.salvar(veiculo); // Salva o veículo e suas imagens associadas
        attr.addFlashAttribute("success", "veiculo.create.sucess");
        return "redirect:/veiculos/meus-veiculos";
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

        // Busca o veículo existente para não perder as imagens que já estão no banco
        Veiculo existingVeiculo = veiculoService.buscarPorId(veiculo.getId());
        if (existingVeiculo == null || !existingVeiculo.getLoja().getId().equals(lojaLogada.getId())) {
            attr.addFlashAttribute("fail", "Veículo não encontrado ou você não tem permissão para editá-lo.");
            return "redirect:/veiculos/listar";
        }

        veiculo.setLoja(lojaLogada); // Garante que o veículo a ser salvo tem a loja correta

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.err.println("Validation Error (Edit): " + error.getDefaultMessage()));
            // Para edição, você pode querer recarregar o veículo original com as imagens existentes
            // se houver erros de validação, para que o usuário não perca as imagens carregadas anteriormente.
            // model.addAttribute("veiculo", existingVeiculo); // Adicione isso se você redirecionar para a mesma página
            return "veiculo/cadastro";
        }

        // Se novas fotos foram enviadas, limpamos as antigas e adicionamos as novas.
        // Se nenhuma nova foto for enviada, as fotos existentes serão mantidas pelo JPA.
        if (fotosUpload != null && !fotosUpload.isEmpty() && !fotosUpload.get(0).isEmpty()) {
            try {
                // Limpa as imagens existentes para este veículo.
                // É importante que o service/DAO gerencie a remoção dessas imagens do banco de dados
                // para evitar imagens órfãs se o Veiculo for salvo com uma nova lista de fotos.
                existingVeiculo.getFotos().clear(); // Limpa a coleção gerenciada pelo Hibernate

                for (MultipartFile file : fotosUpload) {
                    if (!file.isEmpty()) {
                        Imagem novaImagem = new Imagem();
                        novaImagem.setNome(file.getOriginalFilename());
                        novaImagem.setTipo(file.getContentType());
                        novaImagem.setDados(file.getBytes());
                        novaImagem.setVeiculo(existingVeiculo); // Associa à instância gerenciada
                        existingVeiculo.getFotos().add(novaImagem); // Adiciona à coleção gerenciada
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing new image files during edit: " + e.getMessage());
                attr.addFlashAttribute("fail", "Erro ao processar as novas imagens.");
                return "veiculo/cadastro";
            }
        }
        // Atualiza os outros campos do veículo
        existingVeiculo.setPlaca(veiculo.getPlaca());
        existingVeiculo.setModelo(veiculo.getModelo());
        existingVeiculo.setChassi(veiculo.getChassi());
        existingVeiculo.setAno(veiculo.getAno());
        existingVeiculo.setQuilometragem(veiculo.getQuilometragem());
        existingVeiculo.setDescricao(veiculo.getDescricao());
        existingVeiculo.setValor(veiculo.getValor());

        veiculoService.salvar(existingVeiculo); // Salva o veículo com as imagens atualizadas
        attr.addFlashAttribute("success", "veiculo.edit.sucess");
        return "redirect:/veiculos/meus-veiculos";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        veiculoService.excluir(id);
        attr.addFlashAttribute("success", "veiculo.delete.sucess");
        return "redirect:/veiculo/meus-veiculos";

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

        return "veiculo/lista";
    }


    @GetMapping("/imagens/{id}")
    public ResponseEntity<byte[]> getImagem(@PathVariable Long id) {
        // Implemente a busca da imagem por ID no seu serviço/repositório de Imagem
        // Supondo que você tenha um ImagemService ou possa buscar diretamente
        Imagem imagem = imagemService.buscarPorId(id);

        if (imagem != null && imagem.getDados() != null) {
            HttpHeaders headers = new HttpHeaders();
            // Define o tipo de conteúdo da imagem (ex: image/jpeg, image/png)
            headers.setContentType(MediaType.parseMediaType(imagem.getTipo()));
            headers.setContentLength(imagem.getDados().length);
            return new ResponseEntity<>(imagem.getDados(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    // NOVO MÉTODO: Listagem de veículos APENAS da loja logada
    @GetMapping("/meus-veiculos")
    public String listarMeusVeiculos(ModelMap model) {
        Loja lojaLogada = getLojaLogada();
        if (lojaLogada == null) {
            model.addAttribute("fail", "Não foi possível identificar a loja logada.");
            return "redirect:/login"; // Ou página de erro/home
        }
        model.addAttribute("veiculos", veiculoService.buscarPorLoja(lojaLogada));
        return "veiculo/meus-veiculos";
    }

}
