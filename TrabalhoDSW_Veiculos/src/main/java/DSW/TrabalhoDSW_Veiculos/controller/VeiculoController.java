package DSW.TrabalhoDSW_Veiculos.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Adicione este import

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
import org.springframework.web.bind.annotation.RequestMapping; // Inclui StatusProposta
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

    // ==== Métodos Auxiliares ====

    private Loja getLojaLogada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Garantir que não seja "anonymousUser" antes de buscar a loja
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return lojaService.buscarPorEmail(auth.getName());
        }
        return null; // Retorna null se não houver loja logada ou for anonymous
    }

    private Cliente getClienteLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Verifica se o usuário está autenticado e tem a role de CLIENTE
        if (auth.isAuthenticated() && !auth.getName().equals("anonymousUser") &&
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return clienteService.buscarPorEmail(auth.getName());
        }
        return null; // Retorna null se não houver cliente logado ou não for ROLE_CLIENTE
    }

    // ==== Cadastro ====

    @GetMapping("/cadastrar")
    public String cadastrar(Veiculo veiculo, ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "Loja não identificada. Por favor, faça login como loja.");
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
            attr.addFlashAttribute("fail", "Sessão expirada. Por favor, faça login novamente.");
            return "redirect:/login";
        }
        veiculo.setLoja(loja);

        if (result.hasErrors()) {
            return "veiculo/cadastro";
        }

        if (fotosUpload.isEmpty() || fotosUpload.stream().allMatch(MultipartFile::isEmpty)) {
            result.rejectValue("fotos", "size", "Pelo menos uma foto é obrigatória.");
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
            attr.addFlashAttribute("fail", "Erro ao processar imagens: " + e.getMessage());
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
            model.addAttribute("fail", "Permissão negada ou veículo não encontrado.");
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
            attr.addFlashAttribute("fail", "Sessão expirada. Por favor, faça login novamente.");
            return "redirect:/login";
        }

        Veiculo existente = veiculoService.buscarPorId(veiculo.getId());
        if (existente == null || !existente.getLoja().getId().equals(loja.getId())) {
            attr.addFlashAttribute("fail", "Veículo não encontrado ou não pertence à loja.");
            return "redirect:/veiculos/meus-veiculos";
        }
        
        // Mantém as fotos existentes caso nenhuma nova foto seja enviada
        if (fotosUpload == null || fotosUpload.isEmpty() || fotosUpload.get(0).isEmpty()) {
             veiculo.setFotos(existente.getFotos()); // Reatribui as fotos existentes para a validação
        }

        if (result.hasErrors()) {
            // Se houver erro de validação, reatribui a loja e as fotos existentes para a view
            veiculo.setLoja(loja);
            veiculo.setFotos(existente.getFotos()); 
            return "veiculo/cadastro";
        }

        if (fotosUpload != null && !fotosUpload.isEmpty() && !fotosUpload.get(0).isEmpty()) {
            if (fotosUpload.size() > 10) {
                attr.addFlashAttribute("fail", "Você pode enviar no máximo 10 imagens.");
                // Em caso de erro, você pode querer manter as fotos já carregadas ou resetar.
                // Para manter, você precisaria reatribuir 'existente.getFotos()' aqui e na view.
                veiculo.setFotos(existente.getFotos());
                return "veiculo/cadastro";
            }

            try {
                // Remover imagens antigas de forma segura
                // Primeiro desassociar as imagens do veículo, depois excluir do DB se necessário
                // Ou, se a relação for CascadeType.ALL no Veiculo.fotos, basta limpar a coleção
                if (existente.getFotos() != null) {
                    existente.getFotos().clear(); 
                } else {
                    existente.setFotos(new ArrayList<>());
                }

                for (MultipartFile file : fotosUpload) {
                    Imagem img = new Imagem();
                    img.setNome(file.getOriginalFilename());
                    img.setTipo(file.getContentType());
                    img.setDados(file.getBytes());
                    img.setVeiculo(existente);
                    existente.getFotos().add(img);
                }
            } catch (IOException e) {
                attr.addFlashAttribute("fail", "Erro ao processar imagens: " + e.getMessage());
                veiculo.setFotos(existente.getFotos()); // Tentar manter as fotos existentes em caso de erro
                return "veiculo/cadastro";
            }
        }
        
        // Copiar os dados do formulário para a entidade existente
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

    // ==== Meus veículos (para a loja) ====

    @GetMapping("/meus-veiculos")
    public String listarMeusVeiculos(ModelMap model) {
        Loja loja = getLojaLogada();
        if (loja == null) {
            model.addAttribute("fail", "Loja não identificada. Por favor, faça login como loja.");
            return "redirect:/login";
        }
        model.addAttribute("veiculos", veiculoService.buscarPorLoja(loja));
        return "veiculo/meus-veiculos";
    }

    // ==== Todos veículos (para o cliente ou visitante) ====

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
            // Mapeia o ID do veículo para a PROPOSTA ativa do cliente para aquele veículo
            Map<Long, Proposta> propostasAtivasDoClientePorVeiculo = new HashMap<>();
            List<Proposta> propostasDoCliente = propostaService.buscarTodosPorCliente(cliente);
            
            for (Proposta proposta : propostasDoCliente) {
                // Considera ativa se o status for ABERTO ou AGUARDANDO_RESPOSTA_CLIENTE
                if (proposta.getStatus() == StatusProposta.ABERTO || 
                    proposta.getStatus() == StatusProposta.AGUARDANDO_RESPOSTA_CLIENTE) {
                    propostasAtivasDoClientePorVeiculo.put(proposta.getVeiculo().getId(), proposta);
                }
            }
            // Nome do atributo no modelo deve corresponder ao usado no HTML
            model.addAttribute("veiculoComPropostaAberta", propostasAtivasDoClientePorVeiculo); 
        }

        return "veiculo/lista";
    }

    // ==== Excluir ====

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes attr) {
        Loja loja = getLojaLogada();
        Veiculo veiculo = veiculoService.buscarPorId(id);

        if (veiculo == null || loja == null || !veiculo.getLoja().getId().equals(loja.getId())) {
            attr.addFlashAttribute("fail", "Permissão negada ou veículo não encontrado.");
            return "redirect:/veiculos/meus-veiculos";
        }

        // TODO: Adicionar verificação se existem propostas ATIVAS para este veículo.
        // Se houver, a exclusão pode ser bloqueada ou as propostas devem ser marcadas como canceladas.
        // Isso depende da regra de negócio.

        veiculoService.excluir(id);
        attr.addFlashAttribute("success", "Veículo excluído com sucesso!");
        return "redirect:/veiculos/meus-veiculos";
    }
}