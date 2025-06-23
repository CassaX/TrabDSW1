package DSW.TrabalhoDSW_Veiculos.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import DSW.TrabalhoDSW_Veiculos.dao.ILojaDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Loja;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueCNPJValidator implements ConstraintValidator<UniqueCNPJ, String> {

    @Autowired
    private ILojaDAO dao;

    @Override
    public boolean isValid(String CNPJ, ConstraintValidatorContext context) {
        if (dao != null) {
            Loja loja = dao.findByCNPJ(CNPJ);
            return loja == null;
        } else {
            return true;
        }

    }
}