package com.prism.core.scoring.engine.v1;

/**
 * V1 scoring constants: dimension min/max and vector max_pts.
 * Sourced directly from PRISM_Agent_Prompt.md.
 */
public final class V1DimensionConfig {

    private V1DimensionConfig() {}

    // ─── D1 — INCOME ──────────────────────────────────────────────────────────
    public static final double D1_MIN = 60,  D1_MAX = 180;
    public static final double INC01_MAX = 28;
    public static final double INC02_MAX = 22;
    public static final double INC03_MAX = 16;
    public static final double INC04_MAX = 18;
    public static final double INC05_MAX = 8;
    public static final double INC06_MAX = 14;
    public static final double INC07_MAX = 14;

    // ─── D2 — ACTIVITY ────────────────────────────────────────────────────────
    public static final double D2_MIN = 45,  D2_MAX = 150;
    public static final double ACT01_MAX = 14;
    public static final double ACT02_MAX = 24;
    public static final double ACT03_MAX = 16;
    public static final double ACT04_MAX = 18;
    public static final double ACT05_MAX = 12;
    public static final double ACT06_MAX = 10;
    public static final double ACT07_MAX = 7;
    public static final double ACT08_MAX = 4;

    // ─── D3 — SPENDING ────────────────────────────────────────────────────────
    public static final double D3_MIN = 50,  D3_MAX = 150;
    public static final double SPD01_MAX = 6;
    public static final double SPD02_MAX = 18;
    public static final double SPD03_MAX = 16;
    public static final double SPD04_MAX = 10;
    public static final double SPD05_MAX = 12;
    public static final double SPD06_MAX = 8;
    public static final double SPD07_MAX = 14;
    public static final double SPD08_MAX = 8;
    public static final double SPD09_MAX = 6;
    public static final double SPD10_MAX = 2;

    // ─── D4 — SOCIAL ──────────────────────────────────────────────────────────
    public static final double D4_MIN = 20,  D4_MAX = 80;
    public static final double SOC01_MAX = 22;
    public static final double SOC02_MAX = 14;
    public static final double SOC03_MAX = 12;
    public static final double SOC04_MAX = 8;
    public static final double SOC05_MAX = 4;

    // ─── D5 — RISK ────────────────────────────────────────────────────────────
    public static final double D5_MIN = 55,  D5_MAX = 160;
    public static final double RSK01_MAX = 35;
    public static final double RSK02_MAX = 20;
    public static final double RSK04_MAX = 30;
    public static final double RSK05_MAX = 20;

    // ─── D6 — IDENTITY ────────────────────────────────────────────────────────
    public static final double D6_MIN = 55,  D6_MAX = 100;
    public static final double IDN01_MAX = 30;
    public static final double IDN02_MAX = 15;

    // ─── D7 — TEMPORAL CONSISTENCY ────────────────────────────────────────────
    public static final double D7_MIN = 15,  D7_MAX = 80;
    public static final double TMP01_MAX = 18;
    public static final double TMP02_MAX = 16;
    public static final double TMP03_MAX = 12;
    public static final double TMP04_MAX = 10;
    public static final double TMP05_MAX = 5;
    public static final double TMP06_MAX = 4;
}
