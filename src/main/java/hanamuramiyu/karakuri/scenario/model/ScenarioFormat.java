package hanamuramiyu.karakuri.scenario.model;

import java.math.BigDecimal;
import java.util.Locale;

public final class ScenarioFormat {
    private ScenarioFormat() {
    }

    public static String formatDuration(int durationTicks) {
        return BigDecimal
            .valueOf(durationTicks)
            .divide(BigDecimal.valueOf(20))
            .stripTrailingZeros()
            .toPlainString()
            + " s";
    }

    public static String formatClicksPerSecond(int halfSteps) {
        if (halfSteps % 2 == 0) {
            return Integer.toString(halfSteps / 2);
        }

        return String.format(
            Locale.ROOT,
            "%.1f",
            halfSteps / 2.0
        );
    }

    public static String formatClicksPerSecondLabel(int halfSteps) {
        return formatClicksPerSecond(halfSteps) + " CPS";
    }
}
