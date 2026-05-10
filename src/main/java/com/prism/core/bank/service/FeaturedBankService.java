package com.prism.core.bank.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.core.bank.dto.CreateBankRequest;
import com.prism.core.bank.dto.FeaturedBankResponse;
import com.prism.core.bank.entity.FeaturedBank;
import com.prism.core.bank.repository.FeaturedBankRepository;
import com.prism.core.common.exception.PrismException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeaturedBankService {

    private final FeaturedBankRepository featuredBankRepository;
    private final ObjectMapper           objectMapper;

    // ─── Public Read ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FeaturedBankResponse> getActiveBanks() {
        return featuredBankRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeaturedBankResponse getBankById(UUID id) {
        FeaturedBank bank = featuredBankRepository.findById(id)
                .filter(FeaturedBank::isActive)
                .orElseThrow(() -> PrismException.notFound("Bank not found"));
        return toResponse(bank);
    }

    // ─── Admin CRUD ───────────────────────────────────────────────────────────

    @Transactional
    public FeaturedBankResponse createBank(CreateBankRequest request) {
        FeaturedBank bank = FeaturedBank.builder()
                .bankName(request.getBankName())
                .tagline(request.getTagline())
                .logoUrl(request.getLogoUrl())
                .loanPageUrl(request.getLoanPageUrl())
                .minLoanAmount(request.getMinLoanAmount())
                .maxLoanAmount(request.getMaxLoanAmount())
                .interestRateFrom(request.getInterestRateFrom())
                .interestRateTo(request.getInterestRateTo())
                .processingFeePct(request.getProcessingFeePct())
                .maxTenureMonths(request.getMaxTenureMonths())
                .featuresJson(request.getFeaturesJson())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(true)
                .build();
        return toResponse(featuredBankRepository.save(bank));
    }

    @Transactional
    public FeaturedBankResponse updateBank(UUID id, CreateBankRequest request) {
        FeaturedBank bank = featuredBankRepository.findById(id)
                .orElseThrow(() -> PrismException.notFound("Bank not found"));

        if (request.getBankName()        != null) bank.setBankName(request.getBankName());
        if (request.getTagline()         != null) bank.setTagline(request.getTagline());
        if (request.getLogoUrl()         != null) bank.setLogoUrl(request.getLogoUrl());
        if (request.getLoanPageUrl()     != null) bank.setLoanPageUrl(request.getLoanPageUrl());
        if (request.getMinLoanAmount()   != null) bank.setMinLoanAmount(request.getMinLoanAmount());
        if (request.getMaxLoanAmount()   != null) bank.setMaxLoanAmount(request.getMaxLoanAmount());
        if (request.getInterestRateFrom()!= null) bank.setInterestRateFrom(request.getInterestRateFrom());
        if (request.getInterestRateTo()  != null) bank.setInterestRateTo(request.getInterestRateTo());
        if (request.getProcessingFeePct()!= null) bank.setProcessingFeePct(request.getProcessingFeePct());
        if (request.getMaxTenureMonths() != null) bank.setMaxTenureMonths(request.getMaxTenureMonths());
        if (request.getFeaturesJson()    != null) bank.setFeaturesJson(request.getFeaturesJson());
        if (request.getDisplayOrder()    != null) bank.setDisplayOrder(request.getDisplayOrder());

        return toResponse(featuredBankRepository.save(bank));
    }

    @Transactional
    public void removeBank(UUID id) {
        FeaturedBank bank = featuredBankRepository.findById(id)
                .orElseThrow(() -> PrismException.notFound("Bank not found"));
        bank.setActive(false);
        featuredBankRepository.save(bank);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private FeaturedBankResponse toResponse(FeaturedBank bank) {
        List<String> features = Collections.emptyList();
        try {
            if (bank.getFeaturesJson() != null) {
                features = objectMapper.readValue(bank.getFeaturesJson(),
                        new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse featuresJson for bank={}", bank.getId());
        }

        return FeaturedBankResponse.builder()
                .id(bank.getId())
                .bankName(bank.getBankName())
                .tagline(bank.getTagline())
                .logoUrl(bank.getLogoUrl())
                .loanPageUrl(bank.getLoanPageUrl())
                .minLoanAmount(bank.getMinLoanAmount())
                .maxLoanAmount(bank.getMaxLoanAmount())
                .interestRateFrom(bank.getInterestRateFrom())
                .interestRateTo(bank.getInterestRateTo())
                .processingFeePct(bank.getProcessingFeePct())
                .maxTenureMonths(bank.getMaxTenureMonths())
                .features(features)
                .displayOrder(bank.getDisplayOrder())
                .build();
    }
}
