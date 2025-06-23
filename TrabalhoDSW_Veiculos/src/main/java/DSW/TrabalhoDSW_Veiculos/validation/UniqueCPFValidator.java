package DSW.TrabalhoDSW_Veiculos.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import DSW.TrabalhoDSW_Veiculos.dao.IClienteDAO;
import DSW.TrabalhoDSW_Veiculos.domain.Cliente;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueCPFValidator implements ConstraintValidator<UniqueCPF, String> {

    @Autowired
    private IClienteDAO dao;

    @Override
    public boolean isValid(String CPF, ConstraintValidatorContext context) {
        if (dao != null && CPF != null && !CPF.trim().isEmpty()) {
            Cliente cliente = dao.findByCPF(CPF);
            return cliente == null;
        } else {
            return true;
        }
    }
}