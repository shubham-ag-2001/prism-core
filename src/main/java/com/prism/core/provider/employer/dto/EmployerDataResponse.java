package com.prism.core.provider.employer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployerDataResponse {
    private String platformKey;
    private String platformName;
    private String accountStatus;        // ACTIVE, SUSPENDED, INACTIVE
    private Integer accountTenureDays;
    private Double overallRating;        // e.g. 4.7
    private Integer totalCompletedOrders;
    private Integer activeOrdersLast30Days;
    private Double cancellationRate;     // e.g. 0.03 = 3%
    private Integer totalEarningsLast90DaysRupees;
    private String employerCategory;     // FOOD_DELIVERY, RIDE_HAILING, etc.
    private boolean isMock;
}
