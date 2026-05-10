package com.prism.core.bank.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FeaturedBankResponse {
    private UUID id;
    private String bankName;
    private String tagline;
    private String logoUrl;
    private String loanPageUrl;
    private Long minLoanAmount;
    private Long maxLoanAmount;
    private BigDecimal interestRateFrom;
    private BigDecimal interestRateTo;
    private BigDecimal processingFeePct;
    private Integer maxTenureMonths;
    private List<String> features;      // deserialized from featuresJson
    private int displayOrder;
}
