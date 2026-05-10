package com.prism.core.scoring.engine;

import com.prism.core.scoring.engine.model.ScoringInput;
import com.prism.core.scoring.engine.model.ScoringResult;

/**
 * Contract for every versioned scoring engine.
 * To add a new engine version:
 *   1. Create a new class implementing this interface
 *   2. Annotate with @Component
 *   3. Change prism.scoring.current-rule-set-version in application.yml
 */
public interface ScoringEngineVersion {
    /** e.g. "v1.0" — must match prism.scoring.current-rule-set-version */
    String getVersion();

    /** Compute a full PRISM score from the given flag values. */
    ScoringResult compute(ScoringInput input);
}
