package co.com.bancolombia.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;

public record CreateUserRecord(

        String documentId,

        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "El apellido no puede estar vacío")
        String lastname,

        LocalDate birthDate,

        String address,

        @NotBlank(message = "El correo electrónico no puede estar vacío")
        @Email(message = "El correo electrónico no tiene un formato válido")
        String email,

        String phone,

        @NotNull(message = "El salario base no puede ser nulo")
        @DecimalMin(value = "0.0", inclusive = false, message = "El salario base no puede ser un valor negativo")
        @DecimalMax(value = "15000001", inclusive = false, message = "El salario base no puede ser mayor a 15,000,000")
        BigDecimal baseSalary
) {}
