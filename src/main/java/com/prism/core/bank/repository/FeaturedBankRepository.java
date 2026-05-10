package com.prism.core.bank.repository;

import com.prism.core.bank.entity.FeaturedBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeaturedBankRepository extends JpaRepository<FeaturedBank, UUID> {
    List<FeaturedBank> findAllByActiveTrueOrderByDisplayOrderAsc();
}
