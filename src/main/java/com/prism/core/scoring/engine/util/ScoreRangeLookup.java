package com.prism.core.scoring.engine.util;

/**
 * Range-lookup helper for converting a continuous value to a score [0–100].
 *
 * Usage: provide an ordered array of {upperBound, score} pairs from lowest to highest.
 * The last entry's upperBound is ignored (acts as catch-all for values above all thresholds).
 *
 * Example:
 *   // avg_weekly_credit: <1000→10, 1000-2000→25, ...
 *   double[][] table = {{1000,10},{2000,25},{3000,45},{4000,65},{5000,80},{6000,90},{8000,95},{Double.MAX_VALUE,100}};
 *   ScoreRangeLookup.lookup(4850, table); // returns 80
 */
public final class ScoreRangeLookup {

    private ScoreRangeLookup() {}

    /**
     * Standard ascending lookup: value < upperBound → returns score.
     * Last row is the "everything above" catch-all.
     *
     * @param value  the raw flag value
     * @param table  2D array: [[upperBound, score], ...]  ordered ascending by upperBound
     * @return       the score for this value
     */
    public static double lookup(double value, double[][] table) {
        for (double[] row : table) {
            if (value < row[0]) return row[1];
        }
        return table[table.length - 1][1]; // catch-all
    }

    /**
     * Descending lookup: value > upperBound → returns score.
     * Used for spend-to-earn ratio where higher is worse.
     *
     * @param table  2D array: [[lowerBound, score], ...] ordered descending by lowerBound
     */
    public static double lookupDescending(double value, double[][] table) {
        for (double[] row : table) {
            if (value > row[0]) return row[1];
        }
        return table[table.length - 1][1];
    }

    /**
     * Boolean flag scoring: false→falseScore, true→trueScore.
     */
    public static double lookupBoolean(Boolean value, double trueScore, double falseScore) {
        if (value == null) return falseScore; // treat null as false (safe default)
        return value ? trueScore : falseScore;
    }

    /**
     * Linear interpolation between two breakpoints.
     * Used for TMP01 consecutive active months streak.
     */
    public static double interpolate(double value, double x1, double y1, double x2, double y2) {
        if (x2 <= x1) return y1;
        double t = (value - x1) / (x2 - x1);
        return y1 + t * (y2 - y1);
    }
}
