package DSW.TrabalhoDSW_Veiculos.validation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import DSW.TrabalhoDSW_Veiculos.dao.ILojaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueCNPJValidator implements ConstraintValidator<UniqueCNPJ, String> {

   
    @Autowired
    private ILojaDAO dao;

    @Autowired
    private HttpServletRequest request; 

    private String fieldIdName; 

    @Override
    public void initialize(UniqueCNPJ constraintAnnotation) {
        this.fieldIdName = constraintAnnotation.fieldId();
    }

    @Override
    public boolean isValid(String CNPJ, ConstraintValidatorContext context) {
        if (dao == null || CNPJ == null || CNPJ.trim().isEmpty()) {
            return true; 
        }

        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Long currentId = null;
        if (pathVariables != null && pathVariables.containsKey("id")) {
            try {
                currentId = Long.valueOf(pathVariables.get("id"));
            } catch (NumberFormatException e) {
                // Não é um ID válido, continue sem ignorar
            }
        }
        
        Loja lojaEncontrada = dao.findByCNPJ(CNPJ);

        if (lojaEncontrada == null) {
            return true;
        }

        return lojaEncontrada.getId().equals(currentId);
    }
}