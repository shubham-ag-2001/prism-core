package com.prism.core.bank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBankRequest {

    @NotBlank(message = "Bank name is required")
    @Size(max = 150)
    private String bankName;

    @Size(max = 255)
    private String tagline;

    @Size(max = 500)
    private String logoUrl;

    @NotBlank(message = "Loan page URL is required")
    @Size(max = 500)
    private String loanPageUrl;

    @Min(0)
    private Long minLoanAmount;

    @Min(0)
    private Long maxLoanAmount;

    @DecimalMin("0.0")
    private BigDecimal interestRateFrom;

    @DecimalMin("0.0")
    private BigDecimal interestRateTo;

    @DecimalMin("0.0")
    private BigDecimal processingFeePct;

    @Min(1)
    private Integer maxTenureMonths;

    private String featuresJson;       // JSON array string e.g. ["No credit history needed"]

    @Min(0)
    private Integer displayOrder;
}
