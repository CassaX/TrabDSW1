package DSW.TrabalhoDSW_Veiculos.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueCPFValidator implements ConstraintValidator<UniqueCPF, String> {

     @Autowired
    private IClienteDAO dao;

    @Autowired
    private HttpServletRequest request; 

    private String fieldIdName; 

    @Override
    public void initialize(UniqueCPF constraintAnnotation) {
        this.fieldIdName = constraintAnnotation.fieldId(); 
    }

    @Override
    public boolean isValid(String CPF, ConstraintValidatorContext context) {
        if (dao == null || CPF == null || CPF.trim().isEmpty()) {
            return true; 
        }

        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long currentId = null;
        if (pathVariables != null && pathVariables.containsKey("id")) { 
            try {
                currentId = Long.valueOf(pathVariables.get("id"));
            } catch (NumberFormatException e) {
    
            }
        }
        
        Cliente clienteEncontrado = dao.findByCPF(CPF);

        if (clienteEncontrado == null) {
            return true;
        }

        return clienteEncontrado.getId().equals(currentId);
    }
}