package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.persistence.ScenarioTransferService;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ImportScenariosScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 760;
    private static final int PANEL_MAX_HEIGHT = 520;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 78;
    private static final int FOOTER_HEIGHT = 44;
    private static final int ROW_HEIGHT = 38;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 6;
    private static final int SCROLL_STEP = 3;
    private static final int CHECKBOX_SIZE = 14;

    private static ScenarioTransferService.ImportSource lastSource =
        ScenarioTransferService.ImportSource.IMPORT;

    private final KarakuriScreen parent;
    private final ScenarioTransferService transferService;

    private ScenarioTransferService.ImportSource selectedSource = lastSource;
    private List<ImportRow> rows = List.of();
    private int scrollOffset;
    private String scanError;
    private String operationMessage;
    private int operationColor;

    private KarakuriButton importButton;
    private KarakuriButton folderButton;
    private KarakuriButton importSourceButton;
    private KarakuriButton exportSourceButton;
    private KarakuriButton rescanButton;
    private KarakuriButton selectAllButton;
    private KarakuriButton clearButton;

    public ImportScenariosScreen(
        KarakuriScreen parent,
        ScenarioTransferService transferService
    ) {
        super(Component.literal("Import Scenarios"));
        this.parent = parent;
        this.transferService = transferService;
    }

    @Override
    protected void init() {
        scanImports();

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int footerY = panelY + panelHeight - FOOTER_HEIGHT;
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int backWidth = Math.min(116, Math.max(68, contentWidth / 4));
        int folderWidth = Math.min(126, Math.max(76, contentWidth / 4));
        int importWidth = contentWidth - backWidth - folderWidth - BUTTON_GAP * 2;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                footerY + 10,
                backWidth,
                BUTTON_HEIGHT,
                Component.literal("Back"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            )
        );

        folderButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN + backWidth + BUTTON_GAP,
                footerY + 10,
                folderWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 430 ? "Folder" : "Open Folder"
                ),
                this::openSelectedFolder,
                KarakuriButton.Style.GHOST
            )
        );

        importButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN + backWidth + folderWidth + BUTTON_GAP * 2,
                footerY + 10,
                Math.max(70, importWidth),
                BUTTON_HEIGHT,
                Component.empty(),
                this::importSelected,
                KarakuriButton.Style.PRIMARY
            )
        );

        createHeaderButtons(panelX, panelY, panelWidth, contentWidth);
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
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + panelHeight;
        int listX = panelX + CONTENT_MARGIN;
        int listY = panelY + HEADER_HEIGHT;
        int listWidth = panelWidth - CONTENT_MARGIN * 2;
        int listHeight = panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - 6;

        graphics.fill(0, 0, width, height, 0xD0100E16);
        graphics.fill(panelX, panelY, panelRight, panelBottom, 0xFF181620);
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF6F5A91
        );
        graphics.fill(panelX, panelY, panelX + 4, panelBottom, 0xFF9B79D1);
        graphics.fill(
            panelX + 4,
            panelY,
            panelRight,
            panelY + HEADER_HEIGHT - 5,
            0xFF1C1824
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 13,
            0xFFF6F2FA,
            false
        );

        Component summary = Component.literal(
            truncate(
                operationMessage == null
                    ? summaryText()
                    : operationMessage,
                panelWidth - CONTENT_MARGIN * 2
            )
        );
        graphics.drawString(
            font,
            summary,
            panelX + CONTENT_MARGIN,
            panelY + 29,
            operationMessage == null
                ? scanError == null
                    ? 0xFF8F8499
                    : 0xFFE66777
                : operationColor,
            false
        );

        graphics.fill(
            listX,
            listY,
            listX + listWidth,
            listY + listHeight,
            0xFF100E16
        );
        graphics.renderOutline(
            listX,
            listY,
            listWidth,
            listHeight,
            0xFF393243
        );

        renderRows(
            graphics,
            mouseX,
            mouseY,
            listX,
            listY,
            listWidth,
            listHeight
        );

        graphics.fill(
            panelX + 1,
            panelBottom - FOOTER_HEIGHT,
            panelRight - 1,
            panelBottom - 1,
            0xFF1C1824
        );
        graphics.fill(
            panelX + CONTENT_MARGIN,
            panelBottom - FOOTER_HEIGHT,
            panelRight - CONTENT_MARGIN,
            panelBottom - FOOTER_HEIGHT + 1,
            0xFF393243
        );

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

        int listX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT;
        int listWidth = panelWidth() - CONTENT_MARGIN * 2;
        int listHeight = panelHeight() - HEADER_HEIGHT - FOOTER_HEIGHT - 6;

        if (!contains(event.x(), event.y(), listX, listY, listWidth, listHeight)) {
            return false;
        }

        int index = scrollOffset + (int) ((event.y() - listY) / ROW_HEIGHT);

        if (index < 0 || index >= rows.size()) {
            return true;
        }

        ImportRow row = rows.get(index);

        if (!row.candidate().valid()) {
            return true;
        }

        int rowY = listY + (index - scrollOffset) * ROW_HEIGHT;
        int rowWidth = listWidth - 4;

        if (
            row.conflict()
                && contains(
                    event.x(),
                    event.y(),
                    listX + 2 + rowWidth - conflictControlWidth(rowWidth) - 7,
                    rowY + 8,
                    conflictControlWidth(rowWidth),
                    22
                )
        ) {
            row.cycleConflictMode();
        } else {
            row.toggleSelected();
        }

        operationMessage = null;
        updateButtons();
        return true;
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        int listX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT;
        int listWidth = panelWidth() - CONTENT_MARGIN * 2;
        int listHeight = panelHeight() - HEADER_HEIGHT - FOOTER_HEIGHT - 6;

        if (!contains(mouseX, mouseY, listX, listY, listWidth, listHeight)) {
            return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
            );
        }

        if (verticalAmount > 0) {
            scrollOffset -= SCROLL_STEP;
        } else if (verticalAmount < 0) {
            scrollOffset += SCROLL_STEP;
        }

        clampScroll(listHeight);
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void createHeaderButtons(
        int panelX,
        int panelY,
        int panelWidth,
        int contentWidth
    ) {
        boolean compact = panelWidth < 430;
        int sourceY = panelY + 48;
        int rescanWidth = compact ? 50 : 72;
        int selectAllWidth = compact ? 40 : 84;
        int clearWidth = compact ? 40 : 58;
        int rightControlsWidth = rescanWidth
            + selectAllWidth
            + clearWidth
            + BUTTON_GAP * 2;
        int sourceButtonWidth = compact
            ? Math.max(
                54,
                (contentWidth - rightControlsWidth - BUTTON_GAP * 2) / 2
            )
            : 150;
        int controlsRight = panelX + panelWidth - CONTENT_MARGIN;
        int clearX = controlsRight - clearWidth;
        int selectAllX = clearX - BUTTON_GAP - selectAllWidth;
        int rescanX = selectAllX - BUTTON_GAP - rescanWidth;

        importSourceButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                sourceY,
                sourceButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    compact ? "Import" : "Import Folder"
                ),
                () -> selectSource(
                    ScenarioTransferService.ImportSource.IMPORT
                ),
                KarakuriButton.Style.SECONDARY
            )
        );

        exportSourceButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN + sourceButtonWidth + BUTTON_GAP,
                sourceY,
                sourceButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    compact ? "Export" : "Export Folder"
                ),
                () -> selectSource(
                    ScenarioTransferService.ImportSource.EXPORT
                ),
                KarakuriButton.Style.SECONDARY
            )
        );

        rescanButton = addRenderableWidget(
            new KarakuriButton(
                font,
                rescanX,
                sourceY,
                rescanWidth,
                BUTTON_HEIGHT,
                Component.literal(compact ? "Scan" : "Rescan"),
                this::rescan,
                KarakuriButton.Style.GHOST
            )
        );

        selectAllButton = addRenderableWidget(
            new KarakuriButton(
                font,
                selectAllX,
                sourceY,
                selectAllWidth,
                BUTTON_HEIGHT,
                Component.literal(compact ? "All" : "Select All"),
                this::selectAll,
                KarakuriButton.Style.SECONDARY
            )
        );

        clearButton = addRenderableWidget(
            new KarakuriButton(
                font,
                clearX,
                sourceY,
                clearWidth,
                BUTTON_HEIGHT,
                Component.literal(compact ? "None" : "Clear"),
                this::clearSelection,
                KarakuriButton.Style.GHOST
            )
        );
    }

    private void scanImports() {
        try {
            List<ScenarioTransferService.ImportCandidate> candidates =
                transferService.scanImports(selectedSource);
            List<ImportRow> updatedRows = new ArrayList<>();

            for (ScenarioTransferService.ImportCandidate candidate : candidates) {
                boolean conflict = candidate.valid()
                    && ScenarioLibrary.containsName(
                        candidate.scenario().name(),
                        -1
                    );

                updatedRows.add(new ImportRow(candidate, conflict));
            }

            rows = List.copyOf(updatedRows);
            scanError = null;
            operationMessage = null;
        } catch (IOException exception) {
            rows = List.of();
            scanError = messageFor(exception);
            operationMessage = null;
        }

        scrollOffset = 0;
    }

    private void rescan() {
        scanImports();
        updateButtons();
    }

    private void selectSource(
        ScenarioTransferService.ImportSource source
    ) {
        if (selectedSource == source) {
            return;
        }

        selectedSource = source;
        lastSource = source;
        scanImports();
        updateButtons();
    }

    private void selectAll() {
        for (ImportRow row : rows) {
            row.setSelected(row.candidate().valid());
        }

        operationMessage = null;
        updateButtons();
    }

    private void clearSelection() {
        for (ImportRow row : rows) {
            row.setSelected(false);
        }

        operationMessage = null;
        updateButtons();
    }

    private void renderRows(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int listX,
        int listY,
        int listWidth,
        int listHeight
    ) {
        if (scanError != null || rows.isEmpty()) {
            Component titleText = Component.literal(
                scanError == null
                    ? "No .karakuri files in "
                        + selectedSource.directoryName()
                        + " folder"
                    : "Could not scan "
                        + selectedSource.directoryName()
                        + " folder"
            );
            Component detailText = Component.literal(
                scanError == null
                    ? selectedSource
                        == ScenarioTransferService.ImportSource.IMPORT
                            ? "Place scenario files in config/karakuri/import"
                            : "Export a scenario from the Karakuri menu"
                    : scanError
            );
            int centerY = listY + listHeight / 2;

            graphics.drawString(
                font,
                titleText,
                listX + (listWidth - font.width(titleText)) / 2,
                centerY - 10,
                0xFFECE6F1,
                false
            );
            graphics.drawString(
                font,
                detailText,
                listX + Math.max(8, (listWidth - font.width(detailText)) / 2),
                centerY + 6,
                scanError == null ? 0xFF81778A : 0xFFE66777,
                false
            );
            return;
        }

        clampScroll(listHeight);
        int capacity = Math.max(1, listHeight / ROW_HEIGHT);
        int end = Math.min(rows.size(), scrollOffset + capacity + 1);

        graphics.enableScissor(
            listX + 1,
            listY + 1,
            listX + listWidth - 1,
            listY + listHeight - 1
        );

        for (int index = scrollOffset; index < end; index++) {
            int rowY = listY + (index - scrollOffset) * ROW_HEIGHT;
            renderRow(
                graphics,
                rows.get(index),
                listX + 2,
                rowY,
                listWidth - 4,
                mouseX,
                mouseY
            );
        }

        graphics.disableScissor();
        renderScrollIndicator(graphics, listX, listY, listWidth, listHeight);
    }

    private void renderRow(
        GuiGraphics graphics,
        ImportRow row,
        int x,
        int y,
        int rowWidth,
        int mouseX,
        int mouseY
    ) {
        boolean hovered = contains(
            mouseX,
            mouseY,
            x,
            y,
            rowWidth,
            ROW_HEIGHT - 1
        );
        int background = row.selected()
            ? hovered ? 0xFF292134 : 0xFF211B2B
            : hovered ? 0xFF211C2A : 0xFF15121B;
        int outline = !row.candidate().valid()
            ? 0xFF65404A
            : row.selected()
                ? 0xFF8F6FC0
                : hovered ? 0xFF6F5A91 : 0xFF302A39;

        graphics.fill(x, y, x + rowWidth, y + ROW_HEIGHT - 1, background);
        graphics.renderOutline(x, y, rowWidth, ROW_HEIGHT - 1, outline);
        graphics.fill(
            x,
            y,
            x + 3,
            y + ROW_HEIGHT - 1,
            row.accentColor()
        );

        renderCheckbox(graphics, row, x + 10, y + 12);

        int controlWidth = row.conflict()
            ? conflictControlWidth(rowWidth)
            : row.candidate().valid()
                ? 0
                : Math.min(72, Math.max(58, rowWidth / 5));
        int textX = x + 34;
        int textWidth = rowWidth - 46 - controlWidth;
        String primary = row.candidate().valid()
            ? row.candidate().scenario().name()
            : row.candidate().fileName();
        String secondary = row.candidate().valid()
            ? row.candidate().locationLabel()
                + "  ·  "
                + actionLabel(row.candidate().scenario())
                + (row.conflict() ? "  ·  name exists" : "")
            : row.candidate().errorMessage();

        graphics.drawString(
            font,
            Component.literal(truncate(primary, textWidth)),
            textX,
            y + 7,
            row.candidate().valid() ? 0xFFF1ECF5 : 0xFFFFD9DF,
            false
        );
        graphics.drawString(
            font,
            Component.literal(truncate(secondary, textWidth)),
            textX,
            y + 21,
            row.candidate().valid() ? 0xFF8F8499 : 0xFFE18B97,
            false
        );

        if (row.conflict()) {
            renderConflictControl(graphics, row, x, y, rowWidth);
        } else if (!row.candidate().valid()) {
            renderInvalidBadge(graphics, x, y, rowWidth, controlWidth);
        }
    }

    private void renderCheckbox(
        GuiGraphics graphics,
        ImportRow row,
        int x,
        int y
    ) {
        int outline = !row.candidate().valid()
            ? 0xFF75404C
            : row.selected()
                ? 0xFFB38AE8
                : 0xFF5A5065;
        int background = row.selected()
            ? 0xFF8F6FC0
            : 0xFF17131D;

        graphics.fill(
            x,
            y,
            x + CHECKBOX_SIZE,
            y + CHECKBOX_SIZE,
            background
        );
        graphics.renderOutline(
            x,
            y,
            CHECKBOX_SIZE,
            CHECKBOX_SIZE,
            outline
        );

        if (row.selected()) {
            graphics.fill(
                x + 4,
                y + 4,
                x + CHECKBOX_SIZE - 4,
                y + CHECKBOX_SIZE - 4,
                0xFFF7F3FC
            );
        } else if (!row.candidate().valid()) {
            Component mark = Component.literal("!");
            graphics.drawString(
                font,
                mark,
                x + (CHECKBOX_SIZE - font.width(mark)) / 2,
                y + 3,
                0xFFE66777,
                false
            );
        }
    }

    private void renderConflictControl(
        GuiGraphics graphics,
        ImportRow row,
        int x,
        int y,
        int rowWidth
    ) {
        int controlWidth = conflictControlWidth(rowWidth);
        int controlX = x + rowWidth - controlWidth - 7;
        int controlY = y + 8;
        int accent = row.conflictMode().accentColor();

        graphics.fill(
            controlX,
            controlY,
            controlX + controlWidth,
            controlY + 22,
            row.conflictMode().backgroundColor()
        );
        graphics.renderOutline(
            controlX,
            controlY,
            controlWidth,
            22,
            accent
        );

        Component text = Component.literal(
            row.conflictMode().label(controlWidth)
        );
        graphics.drawString(
            font,
            text,
            controlX + (controlWidth - font.width(text)) / 2,
            controlY + 7,
            0xFFF7F3FC,
            false
        );
    }

    private void renderInvalidBadge(
        GuiGraphics graphics,
        int x,
        int y,
        int rowWidth,
        int badgeWidth
    ) {
        int badgeX = x + rowWidth - badgeWidth - 7;
        int badgeY = y + 8;

        graphics.fill(
            badgeX,
            badgeY,
            badgeX + badgeWidth,
            badgeY + 22,
            0xFF342026
        );
        graphics.renderOutline(
            badgeX,
            badgeY,
            badgeWidth,
            22,
            0xFFE66777
        );

        Component text = Component.literal("Invalid");
        graphics.drawString(
            font,
            text,
            badgeX + (badgeWidth - font.width(text)) / 2,
            badgeY + 7,
            0xFFFFD9DF,
            false
        );
    }

    private void renderScrollIndicator(
        GuiGraphics graphics,
        int listX,
        int listY,
        int listWidth,
        int listHeight
    ) {
        int capacity = Math.max(1, listHeight / ROW_HEIGHT);

        if (rows.size() <= capacity) {
            return;
        }

        int trackHeight = listHeight - 6;
        int thumbHeight = Math.max(16, trackHeight * capacity / rows.size());
        int maximumOffset = Math.max(1, rows.size() - capacity);
        int thumbY = listY + 3
            + (trackHeight - thumbHeight) * scrollOffset / maximumOffset;

        graphics.fill(
            listX + listWidth - 3,
            listY + 3,
            listX + listWidth - 2,
            listY + listHeight - 3,
            0xFF2C2633
        );
        graphics.fill(
            listX + listWidth - 4,
            thumbY,
            listX + listWidth - 1,
            thumbY + thumbHeight,
            0xFF8F6FC0
        );
    }

    private void importSelected() {
        List<Scenario> updated = new ArrayList<>(ScenarioLibrary.getScenarios());
        int importedCount = 0;
        String selectedName = null;

        for (ImportRow row : rows) {
            if (!row.selected()) {
                continue;
            }

            Scenario source = row.candidate().scenario();
            String importedName = source.name();
            int existingIndex = findScenarioIndex(updated, importedName);

            if (row.conflict() && row.conflictMode() == ConflictMode.REPLACE) {
                if (existingIndex >= 0) {
                    Scenario existing =
                        updated.get(existingIndex);

                    updated.set(
                        existingIndex,
                        new Scenario(
                            existing.id(),
                            source.name(),
                            source.steps()
                        )
                    );
                } else {
                    updated.add(
                        new Scenario(
                            source.name(),
                            source.steps()
                        )
                    );
                }
            } else {
                if (existingIndex >= 0) {
                    importedName = createCopyName(updated, importedName);
                }

                updated.add(
                    new Scenario(importedName, source.steps())
                );
            }

            importedCount++;
            selectedName = importedName;
        }

        if (importedCount == 0) {
            return;
        }

        ScenarioLibrary.save(updated);
        parent.refreshScenarios(selectedName);
        parent.showFeedback(
            importedCount == 1
                ? "Imported 1 scenario"
                : "Imported " + importedCount + " scenarios",
            true
        );
        minecraft.setScreen(parent);
    }

    private void openSelectedFolder() {
        try {
            transferService.prepareDirectories();
            String error = KarakuriPathOpener.open(
                transferService.directoryFor(selectedSource)
            );

            if (error == null) {
                operationMessage = "Opened "
                    + selectedSource.directoryName()
                    + " folder";
                operationColor = 0xFF61D394;
            } else {
                operationMessage = error;
                operationColor = 0xFFE66777;
            }
        } catch (IOException exception) {
            operationMessage = messageFor(exception);
            operationColor = 0xFFE66777;
        }
    }

    private void updateButtons() {
        if (
            importButton == null
                || folderButton == null
                || importSourceButton == null
                || exportSourceButton == null
                || rescanButton == null
                || selectAllButton == null
                || clearButton == null
        ) {
            return;
        }

        int selectedCount = selectedCount();
        boolean compact = panelWidth() < 430;

        importButton.setMessage(
            Component.literal(
                compact
                    ? "Import (" + selectedCount + ")"
                    : "Import Selected (" + selectedCount + ")"
            )
        );
        importButton.active = selectedCount > 0;
        folderButton.active = true;
        rescanButton.active = true;
        selectAllButton.active = rows.stream().anyMatch(
            row -> row.candidate().valid() && !row.selected()
        );
        clearButton.active = selectedCount > 0;
        importSourceButton.setStyle(
            selectedSource
                == ScenarioTransferService.ImportSource.IMPORT
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.SECONDARY
        );
        exportSourceButton.setStyle(
            selectedSource
                == ScenarioTransferService.ImportSource.EXPORT
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.SECONDARY
        );
    }

    private String summaryText() {
        if (scanError != null) {
            return scanError;
        }

        long valid = rows.stream()
            .filter(row -> row.candidate().valid())
            .count();
        long invalid = rows.size() - valid;
        int selected = selectedCount();

        if (rows.isEmpty()) {
            return selectedSource.label()
                + "  ·  no .karakuri files";
        }

        if (panelWidth() < 430) {
            return selected
                + " selected  ·  "
                + valid
                + " valid"
                + (invalid == 0 ? "" : "  ·  " + invalid + " invalid");
        }

        return "Click rows to select  ·  "
            + selected
            + " selected  ·  "
            + valid
            + (valid == 1 ? " valid file" : " valid files")
            + (invalid == 0 ? "" : "  ·  " + invalid + " invalid");
    }

    private int selectedCount() {
        return (int) rows.stream()
            .filter(ImportRow::selected)
            .count();
    }

    private int conflictControlWidth(int rowWidth) {
        return Math.min(100, Math.max(74, rowWidth / 5));
    }

    private String actionLabel(Scenario scenario) {
        int count = scenario.steps().size();
        return count == 1 ? "1 action" : count + " actions";
    }

    private int findScenarioIndex(
        List<Scenario> scenarios,
        String name
    ) {
        for (int index = 0; index < scenarios.size(); index++) {
            if (scenarios.get(index).name().equalsIgnoreCase(name)) {
                return index;
            }
        }

        return -1;
    }

    private String createCopyName(
        List<Scenario> scenarios,
        String sourceName
    ) {
        int copyNumber = 1;

        while (true) {
            String suffix = copyNumber == 1
                ? " Copy"
                : " Copy " + copyNumber;
            int maximumBaseLength = 64 - suffix.length();
            String baseName = sourceName.length() <= maximumBaseLength
                ? sourceName
                : sourceName
                    .substring(0, maximumBaseLength)
                    .stripTrailing();
            String candidate = baseName + suffix;

            if (findScenarioIndex(scenarios, candidate) < 0) {
                return candidate;
            }

            copyNumber++;
        }
    }

    private void clampScroll(int listHeight) {
        int capacity = Math.max(1, listHeight / ROW_HEIGHT);
        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            Math.max(0, rows.size() - capacity)
        );
    }

    private String truncate(String value, int maximumWidth) {
        if (font.width(value) <= maximumWidth) {
            return value;
        }

        String ellipsis = "...";
        int availableWidth = Math.max(0, maximumWidth - font.width(ellipsis));
        int length = value.length();

        while (
            length > 0
                && font.width(value.substring(0, length)) > availableWidth
        ) {
            length--;
        }

        return value.substring(0, length) + ellipsis;
    }

    private String messageFor(IOException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
            ? "Failed to access import folder"
            : message;
    }

    private int panelWidth() {
        return Math.min(PANEL_MAX_WIDTH, width - PANEL_MARGIN * 2);
    }

    private int panelHeight() {
        return Math.min(PANEL_MAX_HEIGHT, height - PANEL_MARGIN * 2);
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private boolean contains(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int width,
        int height
    ) {
        return mouseX >= x
            && mouseX < x + width
            && mouseY >= y
            && mouseY < y + height;
    }

    private enum ConflictMode {
        COPY("Import as Copy", 0xFF67C7E8, 0xFF1B2A32),
        REPLACE("Replace Existing", 0xFFF1C36E, 0xFF342D20);

        private final String label;
        private final int accentColor;
        private final int backgroundColor;

        ConflictMode(
            String label,
            int accentColor,
            int backgroundColor
        ) {
            this.label = label;
            this.accentColor = accentColor;
            this.backgroundColor = backgroundColor;
        }

        private String label(int controlWidth) {
            if (controlWidth < 96) {
                return this == COPY ? "Copy" : "Replace";
            }

            return label;
        }

        private int accentColor() {
            return accentColor;
        }

        private int backgroundColor() {
            return backgroundColor;
        }
    }

    private static final class ImportRow {
        private final ScenarioTransferService.ImportCandidate candidate;
        private final boolean conflict;
        private boolean selected;
        private ConflictMode conflictMode = ConflictMode.COPY;

        private ImportRow(
            ScenarioTransferService.ImportCandidate candidate,
            boolean conflict
        ) {
            this.candidate = candidate;
            this.conflict = conflict;
        }

        private ScenarioTransferService.ImportCandidate candidate() {
            return candidate;
        }

        private boolean conflict() {
            return conflict;
        }

        private boolean selected() {
            return candidate.valid() && selected;
        }

        private void setSelected(boolean selected) {
            this.selected = candidate.valid() && selected;
        }

        private void toggleSelected() {
            setSelected(!selected());
        }

        private ConflictMode conflictMode() {
            return conflictMode;
        }

        private void cycleConflictMode() {
            if (!conflict || !candidate.valid()) {
                return;
            }

            conflictMode = conflictMode == ConflictMode.COPY
                ? ConflictMode.REPLACE
                : ConflictMode.COPY;
        }

        private int accentColor() {
            if (!candidate.valid()) {
                return 0xFFE66777;
            }

            if (selected()) {
                return 0xFFB38AE8;
            }

            return 0xFF51465D;
        }
    }
}