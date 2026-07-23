package hanamuramiyu.karakuri.scenario.persistence;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

final class ScenarioJsonCodec {
    private static final int SCHEMA_VERSION = 1;

    private final Gson gson;
    private final ScenarioStepJsonCodec stepCodec;

    private ScenarioJsonCodec(
        Gson gson,
        ScenarioStepJsonCodec stepCodec
    ) {
        this.gson = gson;
        this.stepCodec = stepCodec;
    }

    static ScenarioJsonCodec createDefault() {
        return new ScenarioJsonCodec(
            new GsonBuilder()
                .setPrettyPrinting()
                .create(),
            new ScenarioStepJsonCodec()
        );
    }

    Scenario readScenario(
        Reader reader
    ) {
        return readScenarioDocument(reader)
            .scenario();
    }

    ScenarioDocument readScenarioDocument(
        Reader reader
    ) {
        JsonObject root =
            readVersionedRoot(
                reader,
                "Scenario file root"
            );

        return readScenarioObject(root);
    }

    List<Scenario> readLegacyScenarios(
        Reader reader
    ) {
        JsonObject root =
            readVersionedRoot(
                reader,
                "Legacy scenario file root"
            );

        JsonArray scenarioArray =
            JsonObjectReader
                .from(root)
                .requiredArray("scenarios");

        List<Scenario> scenarios =
            new ArrayList<>();

        for (
            JsonElement scenarioElement :
            scenarioArray
        ) {
            scenarios.add(
                readScenarioElement(
                    scenarioElement
                ).scenario()
            );
        }

        return List.copyOf(scenarios);
    }

    void writeScenario(
        Writer writer,
        Scenario scenario
    ) {
        if (writer == null) {
            throw new IllegalArgumentException(
                "Scenario writer must not be null"
            );
        }

        if (scenario == null) {
            throw new IllegalArgumentException(
                "Scenario must not be null"
            );
        }

        gson.toJson(
            writeScenarioObject(scenario),
            writer
        );
    }

    private JsonObject readVersionedRoot(
        Reader reader,
        String rootName
    ) {
        if (reader == null) {
            throw new IllegalArgumentException(
                "Scenario reader must not be null"
            );
        }

        JsonElement rootElement =
            JsonParser.parseReader(reader);

        JsonObjectReader root =
            JsonObjectReader.from(
                rootElement,
                rootName
            );

        int schemaVersion =
            root.requiredInt(
                "schemaVersion"
            );

        if (
            schemaVersion
                != SCHEMA_VERSION
        ) {
            throw new JsonParseException(
                "Unsupported scenario schema version: "
                    + schemaVersion
            );
        }

        return root.object();
    }

    private ScenarioDocument readScenarioElement(
        JsonElement element
    ) {
        JsonObjectReader scenarioObject =
            JsonObjectReader.from(
                element,
                "Scenario"
            );

        return readScenarioObject(
            scenarioObject.object()
        );
    }

    private ScenarioDocument readScenarioObject(
        JsonObject object
    ) {
        JsonObjectReader values =
            JsonObjectReader.from(object);

        String name =
            values.requiredString("name");

        JsonArray stepArray =
            values.requiredArray("steps");

        List<ScenarioStep> steps =
            new ArrayList<>();

        for (
            JsonElement stepElement :
            stepArray
        ) {
            steps.add(
                stepCodec.read(
                    stepElement
                )
            );
        }

        boolean generatedId =
            !object.has("id")
                || object.get("id").isJsonNull();

        Scenario scenario = generatedId
            ? new Scenario(name, steps)
            : new Scenario(
                values.requiredString("id"),
                name,
                steps
            );

        return new ScenarioDocument(
            scenario,
            generatedId
        );
    }

    private JsonObject writeScenarioObject(
        Scenario scenario
    ) {
        JsonObject object =
            new JsonObject();

        object.addProperty(
            "schemaVersion",
            SCHEMA_VERSION
        );

        object.addProperty(
            "id",
            scenario.id()
        );

        object.addProperty(
            "name",
            scenario.name()
        );

        JsonArray stepArray =
            new JsonArray();

        for (
            ScenarioStep step :
            scenario.steps()
        ) {
            stepArray.add(
                stepCodec.write(step)
            );
        }

        object.add(
            "steps",
            stepArray
        );

        return object;
    }

    record ScenarioDocument(
        Scenario scenario,
        boolean generatedId
    ) {
    }
}