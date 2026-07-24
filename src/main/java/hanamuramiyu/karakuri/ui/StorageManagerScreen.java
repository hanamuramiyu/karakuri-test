package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.storage.StorageColor;
import hanamuramiyu.karakuri.storage.StorageGroup;
import hanamuramiyu.karakuri.storage.StorageMarker;
import hanamuramiyu.karakuri.storage.StoragePreviewController;
import hanamuramiyu.karakuri.storage.StoragePreviewTarget;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.storage.StorageTargeting;
import hanamuramiyu.karakuri.storage.StorageWorldIdentity;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StorageManagerScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 920;
    private static final int PANEL_MAX_HEIGHT = 570;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 62;
    private static final int FOOTER_HEIGHT = 78;
    private static final int COLUMN_GAP = 10;
    private static final int GROUP_WIDTH = 270;
    private static final int ROW_HEIGHT = 44;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 6;
    private static final int CHECKBOX_SIZE = 14;
    private static final int SCROLL_STEP = 2;

    private final Screen parent;
    private final Set<String> selectedGroupIds =
        new LinkedHashSet<>();
    private final Set<String> selectedMarkerIds =
        new LinkedHashSet<>();

    private String activeGroupId;
    private String activeMarkerId;
    private int groupScroll;
    private int markerScroll;
    private String feedbackMessage;
    private int feedbackColor;
    private int feedbackTicks;

    private KarakuriButton renameGroupButton;
    private KarakuriButton colorButton;
    private KarakuriButton deleteGroupButton;
    private KarakuriButton addTargetButton;
    private KarakuriButton renameMarkerButton;
    private KarakuriButton deleteMarkerButton;
    private KarakuriButton previewButton;

    public StorageManagerScreen(
        Screen parent
    ) {
        super(Component.literal("Storage Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        StorageRegistry.initialize();
        normalizeSelection();

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int footerY =
            panelY + panelHeight() - FOOTER_HEIGHT;
        int groupButtonWidth =
            (contentWidth - BUTTON_GAP * 3) / 4;
        int actionButtonWidth =
            (contentWidth - BUTTON_GAP * 4) / 5;
        int firstRowY = footerY + 6;
        int secondRowY =
            firstRowY + BUTTON_HEIGHT + BUTTON_GAP;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                firstRowY,
                groupButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("New Group"),
                this::createGroup,
                KarakuriButton.Style.PRIMARY
            )
        );

        renameGroupButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + groupButtonWidth
                    + BUTTON_GAP,
                firstRowY,
                groupButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Rename Group"),
                this::renameGroup,
                KarakuriButton.Style.SECONDARY
            )
        );

        colorButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (groupButtonWidth
                        + BUTTON_GAP) * 2,
                firstRowY,
                groupButtonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::cycleGroupColor,
                KarakuriButton.Style.SECONDARY
            )
        );

        deleteGroupButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (groupButtonWidth
                        + BUTTON_GAP) * 3,
                firstRowY,
                groupButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Delete Group"),
                this::deleteGroup,
                KarakuriButton.Style.DANGER
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                secondRowY,
                actionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Back"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            )
        );

        addTargetButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + actionButtonWidth
                    + BUTTON_GAP,
                secondRowY,
                actionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Add Targeted"),
                this::addTargetedStorage,
                KarakuriButton.Style.PRIMARY
            )
        );

        renameMarkerButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (actionButtonWidth
                        + BUTTON_GAP) * 2,
                secondRowY,
                actionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Rename Storage"),
                this::renameMarker,
                KarakuriButton.Style.SECONDARY
            )
        );

        deleteMarkerButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (actionButtonWidth
                        + BUTTON_GAP) * 3,
                secondRowY,
                actionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Delete Storage"),
                this::deleteMarker,
                KarakuriButton.Style.DANGER
            )
        );

        previewButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (actionButtonWidth
                        + BUTTON_GAP) * 4,
                secondRowY,
                actionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Preview Selected"),
                this::previewSelected,
                KarakuriButton.Style.SUCCESS
            )
        );

        clampScrolls();
        updateButtons();
    }

    @Override
    public void tick() {
        if (feedbackTicks > 0) {
            feedbackTicks--;

            if (feedbackTicks == 0) {
                feedbackMessage = null;
            }
        }

        updateButtons();
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentX = panelX + CONTENT_MARGIN;
        int listY = panelY + HEADER_HEIGHT;
        int listHeight = listHeight();
        int groupWidth = groupWidth();
        int markerX =
            contentX + groupWidth + COLUMN_GAP;
        int markerWidth =
            panelWidth
                - CONTENT_MARGIN * 2
                - groupWidth
                - COLUMN_GAP;

        graphics.fill(0, 0, width, height, 0xD0100E16);
        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            0xFF181620
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF6F5A91
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + panelHeight,
            0xFF9B79D1
        );
        graphics.fill(
            panelX + 4,
            panelY,
            panelX + panelWidth,
            panelY + HEADER_HEIGHT - 4,
            0xFF1C1824
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 15,
            0xFFF6F2FA,
            false
        );
        graphics.drawString(
            font,
            Component.literal(
                "Assign containers to one or several storage groups"
            ),
            panelX + CONTENT_MARGIN,
            panelY + 34,
            0xFF9E94A7,
            false
        );

        renderColumnFrame(
            graphics,
            contentX,
            listY,
            groupWidth,
            listHeight,
            "Storage Groups"
        );
        renderColumnFrame(
            graphics,
            markerX,
            listY,
            markerWidth,
            listHeight,
            activeGroup() == null
                ? "Storages"
                : activeGroup().name()
                    + " · "
                    + visibleMarkers().size()
        );

        renderGroups(
            graphics,
            contentX,
            listY + 26,
            groupWidth,
            listHeight - 26,
            mouseX,
            mouseY
        );
        renderMarkers(
            graphics,
            markerX,
            listY + 26,
            markerWidth,
            listHeight - 26,
            mouseX,
            mouseY
        );

        graphics.fill(
            panelX + 1,
            panelY + panelHeight - FOOTER_HEIGHT,
            panelX + panelWidth - 1,
            panelY + panelHeight - FOOTER_HEIGHT + 1,
            0xFF393243
        );

        if (feedbackMessage != null) {
            graphics.drawString(
                font,
                Component.literal(feedbackMessage),
                panelX + CONTENT_MARGIN,
                panelY + panelHeight - FOOTER_HEIGHT - 14,
                feedbackColor,
                false
            );
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        if (event.button() != 0) {
            return false;
        }

        int contentX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT + 26;
        int listHeight = listHeight() - 26;
        int groupWidth = groupWidth();
        int markerX =
            contentX + groupWidth + COLUMN_GAP;
        int markerWidth =
            panelWidth()
                - CONTENT_MARGIN * 2
                - groupWidth
                - COLUMN_GAP;

        if (
            contains(
                event.x(),
                event.y(),
                contentX,
                listY,
                groupWidth,
                listHeight
            )
        ) {
            clickGroup(
                event.x(),
                event.y(),
                contentX,
                listY,
                doubled
            );
            return true;
        }

        if (
            contains(
                event.x(),
                event.y(),
                markerX,
                listY,
                markerWidth,
                listHeight
            )
        ) {
            clickMarker(
                event.y(),
                listY,
                doubled
            );
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        int contentX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT + 26;
        int listHeight = listHeight() - 26;
        int groupWidth = groupWidth();
        int markerX =
            contentX + groupWidth + COLUMN_GAP;
        int markerWidth =
            panelWidth()
                - CONTENT_MARGIN * 2
                - groupWidth
                - COLUMN_GAP;
        int amount =
            (int) Math.signum(verticalAmount)
                * SCROLL_STEP;

        if (
            contains(
                mouseX,
                mouseY,
                contentX,
                listY,
                groupWidth,
                listHeight
            )
        ) {
            groupScroll -= amount;
            clampScrolls();
            return true;
        }

        if (
            contains(
                mouseX,
                mouseY,
                markerX,
                listY,
                markerWidth,
                listHeight
            )
        ) {
            markerScroll -= amount;
            clampScrolls();
            return true;
        }

        return super.mouseScrolled(
            mouseX,
            mouseY,
            horizontalAmount,
            verticalAmount
        );
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderColumnFrame(
        GuiGraphics graphics,
        int x,
        int y,
        int columnWidth,
        int columnHeight,
        String label
    ) {
        graphics.fill(
            x,
            y,
            x + columnWidth,
            y + columnHeight,
            0xFF100E16
        );
        graphics.renderOutline(
            x,
            y,
            columnWidth,
            columnHeight,
            0xFF393243
        );
        graphics.fill(
            x + 1,
            y + 1,
            x + columnWidth - 1,
            y + 25,
            0xFF17141F
        );
        graphics.drawString(
            font,
            Component.literal(
                truncate(label, columnWidth - 20)
            ),
            x + 9,
            y + 9,
            0xFFECE6F1,
            false
        );
    }

    private void renderGroups(
        GuiGraphics graphics,
        int x,
        int y,
        int listWidth,
        int listHeight,
        int mouseX,
        int mouseY
    ) {
        List<StorageGroup> groups =
            StorageRegistry.groups();

        if (groups.isEmpty()) {
            renderEmpty(
                graphics,
                x,
                y,
                listWidth,
                listHeight,
                "No storage groups",
                "Create a group to begin"
            );
            return;
        }

        int capacity = Math.max(1, listHeight / ROW_HEIGHT);
        int end = Math.min(
            groups.size(),
            groupScroll + capacity + 1
        );

        graphics.enableScissor(
            x + 1,
            y,
            x + listWidth - 1,
            y + listHeight
        );

        for (int index = groupScroll; index < end; index++) {
            renderGroupRow(
                graphics,
                groups.get(index),
                x + 2,
                y + (index - groupScroll) * ROW_HEIGHT,
                listWidth - 4,
                mouseX,
                mouseY
            );
        }

        graphics.disableScissor();
    }

    private void renderGroupRow(
        GuiGraphics graphics,
        StorageGroup group,
        int x,
        int y,
        int rowWidth,
        int mouseX,
        int mouseY
    ) {
        boolean active = group.id().equals(activeGroupId);
        boolean selected =
            selectedGroupIds.contains(group.id());
        boolean hovered = contains(
            mouseX,
            mouseY,
            x,
            y,
            rowWidth,
            ROW_HEIGHT - 1
        );
        int background = active
            ? 0xFF2A2237
            : hovered ? 0xFF211C2A : 0xFF15121B;

        graphics.fill(
            x,
            y,
            x + rowWidth,
            y + ROW_HEIGHT - 1,
            background
        );
        graphics.renderOutline(
            x,
            y,
            rowWidth,
            ROW_HEIGHT - 1,
            active
                ? group.color().color()
                : hovered ? 0xFF51465D : 0xFF27222F
        );
        graphics.fill(
            x,
            y,
            x + 3,
            y + ROW_HEIGHT - 1,
            group.color().color()
        );

        renderCheckbox(
            graphics,
            x + 9,
            y + (ROW_HEIGHT - CHECKBOX_SIZE) / 2,
            selected,
            group.color().color()
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(group.name(), rowWidth - 86)
            ),
            x + 32,
            y + 8,
            0xFFF0EAF4,
            false
        );

        int markerCount =
            StorageRegistry.markersForWorld(
                group,
                currentWorldId()
            ).size();
        String detail = markerCount
            + (markerCount == 1
                ? " storage"
                : " storages")
            + "  ·  "
            + group.color().label();
        graphics.drawString(
            font,
            Component.literal(
                truncate(detail, rowWidth - 46)
            ),
            x + 32,
            y + 24,
            0xFF92889B,
            false
        );
    }

    private void renderMarkers(
        GuiGraphics graphics,
        int x,
        int y,
        int listWidth,
        int listHeight,
        int mouseX,
        int mouseY
    ) {
        List<StorageMarker> markers = visibleMarkers();

        if (activeGroup() == null) {
            renderEmpty(
                graphics,
                x,
                y,
                listWidth,
                listHeight,
                "No group selected",
                "Select or create a storage group"
            );
            return;
        }

        if (markers.isEmpty()) {
            renderEmpty(
                graphics,
                x,
                y,
                listWidth,
                listHeight,
                "No storage markers in this world",
                "Look at a container and use Add Targeted"
            );
            return;
        }

        int capacity = Math.max(1, listHeight / ROW_HEIGHT);
        int end = Math.min(
            markers.size(),
            markerScroll + capacity + 1
        );

        graphics.enableScissor(
            x + 1,
            y,
            x + listWidth - 1,
            y + listHeight
        );

        for (int index = markerScroll; index < end; index++) {
            renderMarkerRow(
                graphics,
                markers.get(index),
                x + 2,
                y + (index - markerScroll) * ROW_HEIGHT,
                listWidth - 4,
                mouseX,
                mouseY
            );
        }

        graphics.disableScissor();
    }

    private void renderMarkerRow(
        GuiGraphics graphics,
        StorageMarker marker,
        int x,
        int y,
        int rowWidth,
        int mouseX,
        int mouseY
    ) {
        StorageGroup group = activeGroup();
        boolean active = marker.id().equals(activeMarkerId);
        boolean selected =
            selectedMarkerIds.contains(marker.id());
        boolean hovered = contains(
            mouseX,
            mouseY,
            x,
            y,
            rowWidth,
            ROW_HEIGHT - 1
        );
        int background = active
            ? 0xFF252030
            : hovered ? 0xFF211C2A : 0xFF15121B;

        graphics.fill(
            x,
            y,
            x + rowWidth,
            y + ROW_HEIGHT - 1,
            background
        );
        graphics.renderOutline(
            x,
            y,
            rowWidth,
            ROW_HEIGHT - 1,
            active
                ? group.color().color()
                : hovered ? 0xFF51465D : 0xFF27222F
        );

        renderCheckbox(
            graphics,
            x + 9,
            y + (ROW_HEIGHT - CHECKBOX_SIZE) / 2,
            selected,
            group.color().color()
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(marker.name(), rowWidth - 182)
            ),
            x + 32,
            y + 8,
            0xFFF0EAF4,
            false
        );

        String coordinates =
            marker.x()
                + ", "
                + marker.y()
                + ", "
                + marker.z()
                + "  ·  "
                + shortDimension(marker.dimensionId());
        graphics.drawString(
            font,
            Component.literal(
                truncate(coordinates, rowWidth - 48)
            ),
            x + 32,
            y + 24,
            0xFF92889B,
            false
        );

        String blockLabel = blockLabel(marker.blockId());
        String assignmentLabel = marker.groupIds().size() == 1
            ? blockLabel
            : blockLabel
                + " · "
                + marker.groupIds().size()
                + " groups";
        graphics.drawString(
            font,
            Component.literal(assignmentLabel),
            x
                + rowWidth
                - 10
                - font.width(assignmentLabel),
            y + 8,
            0xFFC8BECF,
            false
        );
    }

    private void renderCheckbox(
        GuiGraphics graphics,
        int x,
        int y,
        boolean selected,
        int color
    ) {
        graphics.fill(
            x,
            y,
            x + CHECKBOX_SIZE,
            y + CHECKBOX_SIZE,
            selected ? color : 0xFF100E16
        );
        graphics.renderOutline(
            x,
            y,
            CHECKBOX_SIZE,
            CHECKBOX_SIZE,
            selected ? 0xFFF3EAFB : 0xFF5A5063
        );

        if (selected) {
            graphics.drawString(
                font,
                Component.literal("✓"),
                x + 3,
                y + 3,
                0xFFF8F4FB,
                false
            );
        }
    }

    private void renderEmpty(
        GuiGraphics graphics,
        int x,
        int y,
        int emptyWidth,
        int emptyHeight,
        String heading,
        String detail
    ) {
        int centerY = y + emptyHeight / 2;
        Component headingText =
            Component.literal(heading);
        Component detailText =
            Component.literal(detail);

        graphics.drawString(
            font,
            headingText,
            x + (emptyWidth - font.width(headingText)) / 2,
            centerY - 10,
            0xFFECE6F1,
            false
        );
        graphics.drawString(
            font,
            detailText,
            x + Math.max(
                8,
                (emptyWidth - font.width(detailText)) / 2
            ),
            centerY + 7,
            0xFF81778A,
            false
        );
    }

    private void clickGroup(
        double mouseX,
        double mouseY,
        int x,
        int y,
        boolean doubled
    ) {
        List<StorageGroup> groups =
            StorageRegistry.groups();
        int index = groupScroll
            + (int) ((mouseY - y) / ROW_HEIGHT);

        if (index < 0 || index >= groups.size()) {
            return;
        }

        StorageGroup group = groups.get(index);

        if (mouseX < x + 30) {
            toggle(selectedGroupIds, group.id());
        } else {
            if (!group.id().equals(activeGroupId)) {
                activeGroupId = group.id();
                activeMarkerId = null;
                markerScroll = 0;
                normalizeMarkerSelection();
            }

            if (doubled) {
                renameGroup();
            }
        }

        updateButtons();
    }

    private void clickMarker(
        double mouseY,
        int y,
        boolean doubled
    ) {
        List<StorageMarker> markers = visibleMarkers();
        int index = markerScroll
            + (int) ((mouseY - y) / ROW_HEIGHT);

        if (index < 0 || index >= markers.size()) {
            return;
        }

        StorageMarker marker = markers.get(index);
        activeMarkerId = marker.id();
        toggle(selectedMarkerIds, marker.id());

        if (doubled) {
            renameMarker();
        }

        updateButtons();
    }

    private void createGroup() {
        minecraft.setScreen(
            new StorageNameScreen(
                this,
                "Create Storage Group",
                "Group name",
                "",
                "Create",
                name -> {
                    StorageGroup group =
                        StorageRegistry.addGroup(
                            name,
                            StorageColor.PURPLE
                        );
                    activeGroupId = group.id();
                    activeMarkerId = null;
                    minecraft.setScreen(this);
                }
            )
        );
    }

    private void renameGroup() {
        StorageGroup group = activeGroup();

        if (group == null) {
            return;
        }

        minecraft.setScreen(
            new StorageNameScreen(
                this,
                "Rename Storage Group",
                "Group name",
                group.name(),
                "Rename",
                name -> {
                    StorageRegistry.replaceGroup(
                        group.withName(name)
                    );
                    minecraft.setScreen(this);
                }
            )
        );
    }

    private void cycleGroupColor() {
        StorageGroup group = activeGroup();

        if (group == null) {
            return;
        }

        StorageRegistry.replaceGroup(
            group.withColor(group.color().next())
        );
        updateButtons();
    }

    private void deleteGroup() {
        StorageGroup group = activeGroup();

        if (group == null) {
            return;
        }

        List<StorageMarker> groupMarkers =
            StorageRegistry.markersForGroup(group);
        long exclusiveCount = groupMarkers.stream()
            .filter(marker -> marker.groupIds().size() == 1)
            .count();
        long sharedCount = groupMarkers.size() - exclusiveCount;
        String detail;

        if (groupMarkers.isEmpty()) {
            detail = "The empty group will be removed.";
        } else if (sharedCount == 0) {
            detail = exclusiveCount
                + (exclusiveCount == 1
                    ? " storage marker will also be removed."
                    : " storage markers will also be removed.");
        } else {
            detail = exclusiveCount
                + " exclusive removed · "
                + sharedCount
                + " shared kept in other groups";
        }

        minecraft.setScreen(
            new StorageDeleteConfirmationScreen(
                this,
                "Delete Storage Group",
                group.name(),
                detail,
                () -> {
                    StorageRegistry.deleteGroup(group.id());
                    selectedGroupIds.remove(group.id());
                    activeGroupId = null;
                    activeMarkerId = null;
                    normalizeSelection();
                }
            )
        );
    }

    private void addTargetedStorage() {
        StorageGroup group = activeGroup();

        if (group == null) {
            showFeedback(
                "Select a storage group first",
                false
            );
            return;
        }

        StorageTargeting.TargetedStorage target =
            StorageTargeting.targetedStorage(minecraft);

        if (target == null) {
            showFeedback(
                "Look at a chest, barrel, or shulker box",
                false
            );
            return;
        }

        String worldId = currentWorldId();
        String dimensionId =
            StorageWorldIdentity.dimensionId(minecraft);

        for (BlockPos targetPosition : target.positions()) {
            StorageMarker existing =
                StorageRegistry.findMarkerAt(
                    worldId,
                    dimensionId,
                    targetPosition
                );

            if (existing == null) {
                continue;
            }

            if (existing.belongsTo(group.id())) {
                showFeedback(
                    "Already assigned to \""
                        + group.name()
                        + "\" as \""
                        + existing.name()
                        + "\"",
                    false
                );
                return;
            }

            String currentGroups =
                StorageRegistry.groupsForMarker(existing.id())
                    .stream()
                    .map(StorageGroup::name)
                    .collect(
                        java.util.stream.Collectors.joining(", ")
                    );

            minecraft.setScreen(
                new StorageGroupAssignmentConfirmationScreen(
                    this,
                    existing.name(),
                    currentGroups,
                    group.name(),
                    () -> {
                        StorageRegistry.assignMarkerToGroup(
                            existing.id(),
                            group.id()
                        );
                        activeMarkerId = existing.id();
                        selectedMarkerIds.add(existing.id());
                        showFeedback(
                            "Assigned \""
                                + existing.name()
                                + "\" to \""
                                + group.name()
                                + "\"",
                            true
                        );
                    }
                )
            );
            return;
        }

        BlockPos position = target.positions()
            .stream()
            .min(
                Comparator.comparingInt(
                    (BlockPos blockPosition) ->
                        blockPosition.getX()
                ).thenComparingInt(
                    blockPosition ->
                        blockPosition.getY()
                ).thenComparingInt(
                    blockPosition ->
                        blockPosition.getZ()
                )
            )
            .orElse(target.position());

        minecraft.setScreen(
            new StorageNameScreen(
                this,
                "Add Targeted Storage",
                "Storage name",
                target.defaultName(),
                "Add Storage",
                name -> {
                    StorageMarker marker =
                        new StorageMarker(
                            group.id(),
                            name,
                            worldId,
                            dimensionId,
                            position,
                            target.blockId()
                        );
                    StorageRegistry.addMarker(
                        group.id(),
                        marker
                    );
                    activeMarkerId = marker.id();
                    selectedMarkerIds.add(marker.id());
                    minecraft.setScreen(this);
                }
            )
        );
    }

    private void renameMarker() {
        StorageMarker marker = activeMarker();

        if (marker == null) {
            return;
        }

        minecraft.setScreen(
            new StorageNameScreen(
                this,
                "Rename Storage",
                "Storage name",
                marker.name(),
                "Rename",
                name -> {
                    StorageRegistry.replaceMarker(
                        marker.withName(name)
                    );
                    minecraft.setScreen(this);
                }
            )
        );
    }

    private void deleteMarker() {
        StorageMarker marker = activeMarker();
        StorageGroup group = activeGroup();

        if (marker == null || group == null) {
            return;
        }

        if (marker.groupIds().size() > 1) {
            int remainingGroups = marker.groupIds().size() - 1;
            minecraft.setScreen(
                new StorageDeleteConfirmationScreen(
                    this,
                    "Remove Storage from Group",
                    marker.name(),
                    "The marker stays in "
                        + remainingGroups
                        + (remainingGroups == 1
                            ? " other group."
                            : " other groups."),
                    "Remove",
                    "Remove \""
                        + marker.name()
                        + "\" from \""
                        + group.name()
                        + "\"?",
                    () -> {
                        StorageRegistry.removeMarkerFromGroup(
                            marker.id(),
                            group.id()
                        );
                        activeMarkerId = null;
                        normalizeMarkerSelection();
                    }
                )
            );
            return;
        }

        minecraft.setScreen(
            new StorageDeleteConfirmationScreen(
                this,
                "Delete Storage Marker",
                marker.name(),
                "The container will not be changed in the world.",
                () -> {
                    StorageRegistry.deleteMarker(marker.id());
                    selectedMarkerIds.remove(marker.id());
                    activeMarkerId = null;
                    normalizeMarkerSelection();
                }
            )
        );
    }

    private void previewSelected() {
        List<StoragePreviewTarget> targets =
            selectedPreviewTargets();

        if (targets.isEmpty()) {
            showFeedback(
                "Select at least one storage or non-empty group",
                false
            );
            return;
        }

        try {
            StoragePreviewController.start(targets);
            minecraft.setScreen(null);
        } catch (RuntimeException exception) {
            showFeedback(
                exception.getMessage() == null
                    ? "Could not start storage preview"
                    : exception.getMessage(),
                false
            );
        }
    }

    private List<StoragePreviewTarget>
    selectedPreviewTargets() {
        Map<String, LinkedHashSet<String>> groupIdsByMarker =
            new LinkedHashMap<>();
        String worldId = currentWorldId();

        for (String groupId : selectedGroupIds) {
            StorageGroup group =
                StorageRegistry.findGroup(groupId);

            if (group == null) {
                continue;
            }

            for (
                StorageMarker marker :
                StorageRegistry.markersForWorld(
                    group,
                    worldId
                )
            ) {
                groupIdsByMarker
                    .computeIfAbsent(
                        marker.id(),
                        ignored -> new LinkedHashSet<>()
                    )
                    .add(group.id());
            }
        }

        for (String markerId : selectedMarkerIds) {
            StorageMarker marker =
                StorageRegistry.findMarker(markerId);

            if (
                marker == null
                    || !marker.worldId().equals(worldId)
            ) {
                continue;
            }

            groupIdsByMarker
                .computeIfAbsent(
                    marker.id(),
                    ignored -> new LinkedHashSet<>()
                )
                .addAll(marker.groupIds());
        }

        if (groupIdsByMarker.isEmpty()) {
            StorageMarker marker = activeMarker();
            StorageGroup group = activeGroup();

            if (
                marker != null
                    && marker.worldId().equals(worldId)
            ) {
                groupIdsByMarker
                    .computeIfAbsent(
                        marker.id(),
                        ignored -> new LinkedHashSet<>()
                    )
                    .addAll(marker.groupIds());
            } else if (group != null) {
                for (
                    StorageMarker groupMarker :
                    StorageRegistry.markersForWorld(
                        group,
                        worldId
                    )
                ) {
                    groupIdsByMarker
                        .computeIfAbsent(
                            groupMarker.id(),
                            ignored -> new LinkedHashSet<>()
                        )
                        .add(group.id());
                }
            }
        }

        List<StoragePreviewTarget> targets =
            new ArrayList<>();

        for (
            Map.Entry<String, LinkedHashSet<String>> entry :
            groupIdsByMarker.entrySet()
        ) {
            StorageMarker marker =
                StorageRegistry.findMarker(entry.getKey());

            if (marker == null) {
                continue;
            }

            List<StorageGroup> markerGroups =
                entry.getValue()
                    .stream()
                    .map(StorageRegistry::findGroup)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (!markerGroups.isEmpty()) {
                targets.add(
                    new StoragePreviewTarget(
                        markerGroups,
                        marker
                    )
                );
            }
        }

        return List.copyOf(targets);
    }

    private StorageGroup activeGroup() {
        return activeGroupId == null
            ? null
            : StorageRegistry.findGroup(activeGroupId);
    }

    private StorageMarker activeMarker() {
        return activeMarkerId == null
            ? null
            : StorageRegistry.findMarker(activeMarkerId);
    }

    private List<StorageMarker> visibleMarkers() {
        StorageGroup group = activeGroup();

        if (group == null) {
            return List.of();
        }

        return StorageRegistry.markersForWorld(
            group,
            currentWorldId()
        ).stream()
            .sorted(
                Comparator.comparingInt(
                    StorageMarker::priority
                ).thenComparing(
                    StorageMarker::name,
                    String.CASE_INSENSITIVE_ORDER
                )
            )
            .toList();
    }

    private void normalizeSelection() {
        List<StorageGroup> groups =
            StorageRegistry.groups();
        Set<String> validGroupIds = groups.stream()
            .map(StorageGroup::id)
            .collect(
                java.util.stream.Collectors.toSet()
            );
        selectedGroupIds.retainAll(validGroupIds);

        if (
            activeGroupId == null
                || !validGroupIds.contains(activeGroupId)
        ) {
            activeGroupId = groups.isEmpty()
                ? null
                : groups.getFirst().id();
            activeMarkerId = null;
        }

        normalizeMarkerSelection();
    }

    private void normalizeMarkerSelection() {
        Set<String> validMarkerIds =
            StorageRegistry.markers()
                .stream()
                .map(StorageMarker::id)
                .collect(
                    java.util.stream.Collectors.toSet()
                );
        selectedMarkerIds.retainAll(validMarkerIds);

        List<StorageMarker> visible = visibleMarkers();
        boolean activeVisible = visible.stream()
            .anyMatch(
                marker -> marker.id().equals(activeMarkerId)
            );

        if (!activeVisible) {
            activeMarkerId = visible.isEmpty()
                ? null
                : visible.getFirst().id();
        }

        clampScrolls();
    }

    private void updateButtons() {
        if (
            renameGroupButton == null
                || colorButton == null
                || deleteGroupButton == null
                || addTargetButton == null
                || renameMarkerButton == null
                || deleteMarkerButton == null
                || previewButton == null
        ) {
            return;
        }

        StorageGroup group = activeGroup();
        StorageMarker marker = activeMarker();

        renameGroupButton.active = group != null;
        colorButton.active = group != null;
        colorButton.setMessage(
            Component.literal(
                group == null
                    ? "Color"
                    : "Color: " + group.color().label()
            )
        );
        deleteGroupButton.active = group != null;
        addTargetButton.active =
            group != null
                && minecraft.player != null
                && minecraft.level != null;
        renameMarkerButton.active = marker != null;
        deleteMarkerButton.active = marker != null;
        deleteMarkerButton.setMessage(
            Component.literal(
                marker != null && marker.groupIds().size() > 1
                    ? "Remove from Group"
                    : "Delete Storage"
            )
        );
        previewButton.active =
            !selectedPreviewTargets().isEmpty();
    }

    private void clampScrolls() {
        int capacity = Math.max(
            1,
            (listHeight() - 26) / ROW_HEIGHT
        );
        groupScroll = Math.clamp(
            groupScroll,
            0,
            Math.max(
                0,
                StorageRegistry.groups().size() - capacity
            )
        );
        markerScroll = Math.clamp(
            markerScroll,
            0,
            Math.max(
                0,
                visibleMarkers().size() - capacity
            )
        );
    }

    private void showFeedback(
        String message,
        boolean success
    ) {
        feedbackMessage = message;
        feedbackColor = success
            ? 0xFF61D394
            : 0xFFE66777;
        feedbackTicks = 120;
    }

    private String currentWorldId() {
        return StorageWorldIdentity.worldId(minecraft);
    }

    private static void toggle(
        Set<String> values,
        String value
    ) {
        if (!values.remove(value)) {
            values.add(value);
        }
    }

    private String shortDimension(
        String dimensionId
    ) {
        int separator = dimensionId.indexOf(':');
        return separator >= 0
            ? dimensionId.substring(separator + 1)
            : dimensionId;
    }

    private String blockLabel(
        String blockId
    ) {
        int separator = blockId.indexOf(':');
        String path = separator >= 0
            ? blockId.substring(separator + 1)
            : blockId;
        String[] words = path.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                Character.toUpperCase(word.charAt(0))
            ).append(word.substring(1));
        }

        return result.toString();
    }

    private String truncate(
        String value,
        int maximumWidth
    ) {
        if (font.width(value) <= maximumWidth) {
            return value;
        }

        String suffix = "...";
        int end = value.length();

        while (
            end > 0
                && font.width(
                    value.substring(0, end) + suffix
                ) > maximumWidth
        ) {
            end--;
        }

        return value.substring(0, end) + suffix;
    }

    private int panelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int panelHeight() {
        return Math.min(
            PANEL_MAX_HEIGHT,
            height - PANEL_MARGIN * 2
        );
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int groupWidth() {
        return Math.min(
            GROUP_WIDTH,
            Math.max(
                190,
                (panelWidth() - CONTENT_MARGIN * 2) / 3
            )
        );
    }

    private int listHeight() {
        return panelHeight()
            - HEADER_HEIGHT
            - FOOTER_HEIGHT
            - 5;
    }

    private boolean contains(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int areaWidth,
        int areaHeight
    ) {
        return mouseX >= x
            && mouseX < x + areaWidth
            && mouseY >= y
            && mouseY < y + areaHeight;
    }
}