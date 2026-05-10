package com.prism.core.scoring.engine;

import com.prism.core.common.exception.PrismException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry of all available scoring engine versions.
 * Spring auto-discovers all ScoringEngineVersion beans at startup.
 *
 * To add a new engine version:
 *   1. Create ScoringEngineVX implementing ScoringEngineVersion, annotate @Component
 *   2. Update prism.scoring.current-rule-set-version in application.yml
 */
@Slf4j
@Component
public class ScoringEngineRegistry {

    private final Map<String, ScoringEngineVersion> engines;
    private final String currentVersion;

    public ScoringEngineRegistry(List<ScoringEngineVersion> engineList,
                                  @Value("${prism.scoring.current-rule-set-version:v1.0}") String currentVersion) {
        this.engines = engineList.stream()
                .collect(Collectors.toMap(ScoringEngineVersion::getVersion, Function.identity()));
        this.currentVersion = currentVersion;
        log.info("ScoringEngineRegistry loaded {} engine(s): {}", engines.size(), engines.keySet());
        log.info("Active scoring engine version: {}", currentVersion);
    }

    /** Get the currently active scoring engine (from config). */
    public ScoringEngineVersion getCurrent() {
        return get(currentVersion);
    }

    /** Get a specific engine version by key (e.g. "v1.0"). */
    public ScoringEngineVersion get(String version) {
        ScoringEngineVersion engine = engines.get(version);
        if (engine == null) {
            throw PrismException.notFound(
                    "Scoring engine version '" + version + "' not found. " +
                    "Available: " + engines.keySet());
        }
        return engine;
    }

    /** Returns all registered version keys. */
    public java.util.Set<String> getAvailableVersions() {
        return engines.keySet();
    }
}
