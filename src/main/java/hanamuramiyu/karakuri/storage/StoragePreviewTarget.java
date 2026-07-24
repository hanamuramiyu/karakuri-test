package hanamuramiyu.karakuri.storage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record StoragePreviewTarget(
    List<StorageGroup> groups,
    StorageMarker marker
) {
    public StoragePreviewTarget {
        marker = Objects.requireNonNull(
            marker,
            "Storage preview marker must not be null"
        );

        if (groups == null || groups.isEmpty()) {
            throw new IllegalArgumentException(
                "Storage preview groups must not be empty"
            );
        }

        Map<String, StorageGroup> unique =
            new LinkedHashMap<>();

        for (StorageGroup group : groups) {
            StorageGroup checked = Objects.requireNonNull(
                group,
                "Storage preview groups must not contain null"
            );

            if (!marker.belongsTo(checked.id())) {
                throw new IllegalArgumentException(
                    "Storage preview marker does not belong to group "
                        + checked.name()
                );
            }

            unique.putIfAbsent(checked.id(), checked);
        }

        groups = List.copyOf(unique.values());
    }


    public StoragePreviewTarget(
        StorageGroup group,
        StorageMarker marker
    ) {
        this(List.of(group), marker);
    }

    public StorageGroup group() {
        return primaryGroup();
    }

    public StorageGroup primaryGroup() {
        return groups.getFirst();
    }

    public String groupLabel() {
        return groups.stream()
            .map(StorageGroup::name)
            .collect(Collectors.joining(" + "));
    }
}