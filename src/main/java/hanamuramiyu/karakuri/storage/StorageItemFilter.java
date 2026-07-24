package hanamuramiyu.karakuri.storage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public record StorageItemFilter(
    List<String> itemIds
) {
    public StorageItemFilter {
        if (itemIds == null) {
            throw new IllegalArgumentException(
                "Storage item filter IDs must not be null"
            );
        }

        LinkedHashSet<String> normalized =
            new LinkedHashSet<>();

        for (String itemId : itemIds) {
            if (itemId == null || itemId.isBlank()) {
                continue;
            }

            normalized.add(
                itemId.trim().toLowerCase(Locale.ROOT)
            );
        }

        itemIds = List.copyOf(normalized);
    }

    public static StorageItemFilter empty() {
        return new StorageItemFilter(List.of());
    }

    public boolean emptyFilter() {
        return itemIds.isEmpty();
    }

    public boolean accepts(
        String itemId
    ) {
        return itemId != null
            && itemIds.contains(
                itemId.trim().toLowerCase(Locale.ROOT)
            );
    }

    public boolean overlaps(
        StorageItemFilter other
    ) {
        if (
            other == null
                || emptyFilter()
                || other.emptyFilter()
        ) {
            return false;
        }

        for (String itemId : itemIds) {
            if (other.itemIds.contains(itemId)) {
                return true;
            }
        }

        return false;
    }

    public int overlapCount(
        StorageItemFilter other
    ) {
        if (other == null) {
            return 0;
        }

        int count = 0;

        for (String itemId : itemIds) {
            if (other.itemIds.contains(itemId)) {
                count++;
            }
        }

        return count;
    }
}