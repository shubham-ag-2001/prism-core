package com.prism.core.lender.controller;

import com.prism.core.common.response.ApiResponse;
import com.prism.core.scoring.dto.response.PrismScoreResponse;
import com.prism.core.scoring.service.ScoringService;
import com.prism.core.user.repository.UserRepository;
import com.prism.core.common.exception.PrismException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lender")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LENDER')")
public class LenderController {

    private final ScoringService  scoringService;
    private final UserRepository  userRepository;

    /**
     * Lender looks up a gig worker's PRISM score by their phone number.
     */
    @GetMapping("/users/{phone}/score")
    public ResponseEntity<ApiResponse<PrismScoreResponse>> getScoreByPhone(
            @PathVariable String phone) {

        var user = userRepository.findByPhone(phone)
                .orElseThrow(() -> PrismException.notFound("No user found with this phone number"));

        return ResponseEntity.ok(ApiResponse.success(scoringService.getLatestScore(user.getId())));
    }
}
