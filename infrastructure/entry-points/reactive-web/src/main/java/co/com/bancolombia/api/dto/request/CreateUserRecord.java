package co.com.bancolombia.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.*;

public record CreateUserRecord(

        String documentId,
        @NotBlank(message = "The identity document cannot be empty")

        @NotBlank(message = "The name cannot be empty")
        String name,

        @NotBlank(message = "The lastname cannot be empty")
        String lastname,

        LocalDate birthDate,

        String address,

        @NotBlank(message = "The email cannot be empty")
        @Email(message = "The email is not in a valid format")
        String email,

        String phone,

        @NotNull(message = "The base salary cannot be zero")
        @DecimalMin(value = "0.0", inclusive = false, message = "The base salary cannot be a negative value")
        @DecimalMax(value = "15000001", inclusive = false, message = "The base salary cannot be greater than 15,000,000")
        BigDecimal baseSalary
) {}
