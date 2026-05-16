package com.prism.core.scoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private UUID id;
    private String category;
    private String title;
    private String description;
    private String source;          // MOCK | LLM
    private Instant createdAt;
}
