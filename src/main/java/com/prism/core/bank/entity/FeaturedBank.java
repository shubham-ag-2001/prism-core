package com.prism.core.bank.entity;

import com.prism.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "featured_banks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedBank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bank_name", nullable = false, length = 150)
    private String bankName;

    @Column(name = "tagline", length = 255)
    private String tagline;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "loan_page_url", length = 500)
    private String loanPageUrl;

    @Column(name = "min_loan_amount")
    private Long minLoanAmount;

    @Column(name = "max_loan_amount")
    private Long maxLoanAmount;

    @Column(name = "interest_rate_from", precision = 5, scale = 2)
    private BigDecimal interestRateFrom;

    @Column(name = "interest_rate_to", precision = 5, scale = 2)
    private BigDecimal interestRateTo;

    @Column(name = "processing_fee_pct", precision = 4, scale = 2)
    private BigDecimal processingFeePct;

    @Column(name = "max_tenure_months")
    private Integer maxTenureMonths;

    @Column(name = "features_json", columnDefinition = "TEXT")
    private String featuresJson;           // JSON array of highlight strings

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
