package hanamuramiyu.karakuri.ui.editor;

import java.math.BigDecimal;

public final class ScenarioValueFormatter {
    private static final BigDecimal TICKS_PER_SECOND =
        BigDecimal.valueOf(20);

    private ScenarioValueFormatter() {
    }

    public static String durationForField(
        int durationTicks
    ) {
        return BigDecimal
            .valueOf(durationTicks)
            .divide(TICKS_PER_SECOND)
            .stripTrailingZeros()
            .toPlainString();
    }

    public static String durationValue(
        int durationTicks
    ) {
        return durationForField(durationTicks)
            + " s";
    }
}