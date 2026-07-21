package hanamuramiyu.karakuri.scenario;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.persistence.ScenarioFileStore;

import java.util.List;

public final class ScenarioRepository {
    private static final ScenarioFileStore FILE_STORE =
        ScenarioFileStore.createDefault();

    private ScenarioRepository() {
    }

    public static List<Scenario> load() {
        return FILE_STORE.load();
    }

    public static void save(
        List<Scenario> scenarios
    ) {
        if (scenarios == null) {
            throw new IllegalArgumentException(
                "Scenario list must not be null"
            );
        }

        FILE_STORE.save(scenarios);
    }
}
