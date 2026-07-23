package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.KarakuriClient;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchRegistry;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchSlot;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchValidation;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class QuickLaunchScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 760;
    private static final int PANEL_MAX_HEIGHT = 520;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 58;
    private static final int FOOTER_HEIGHT = 42;
    private static final int ROW_HEIGHT = 46;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 7;
    private static final int SCROLL_STEP = 2;

    private final Screen parent;

    private int selectedSlot = 1;
    private int scrollOffset;

    private KarakuriButton clearButton;
    private KarakuriButton configureButton;

    public QuickLaunchScreen(
        Screen parent
    ) {
        super(Component.literal("Quick Launch"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickLaunchRegistry.initialize();

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int footerY =
            panelY + panelHeight - FOOTER_HEIGHT;
        int buttonWidth =
            (contentWidth - BUTTON_GAP * 2) / 3;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                footerY + 9,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Back"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            )
        );

        clearButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                footerY + 9,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Clear Slot"),
                this::clearSelectedSlot,
                KarakuriButton.Style.DANGER
            )
        );

        configureButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 2,
                footerY + 9,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Configure"),
                this::configureSelectedSlot,
                KarakuriButton.Style.PRIMARY
            )
        );

        clampScroll();
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
        int listWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int listHeight =
            panelHeight
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        graphics.fill(
            0,
            0,
            width,
            height,
            0xD0100E16
        );
        graphics.fill(
            panelX,
            panelY,
            panelRight,
            panelBottom,
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
            panelBottom,
            0xFF9B79D1
        );
        graphics.fill(
            panelX + 4,
            panelY,
            panelRight,
            panelY + HEADER_HEIGHT - 4,
            0xFF1C1824
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 12,
            0xFFF6F2FA,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                panelWidth < 430
                    ? "Assign scenarios, then bind keys in Controls"
                    : "Assign scenarios here, then bind slots in Minecraft Controls"
            ),
            panelX + CONTENT_MARGIN,
            panelY + 29,
            0xFF8F8499,
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

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );
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
        int listWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int listHeight =
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        if (
            !contains(
                event.x(),
                event.y(),
                listX,
                listY,
                listWidth,
                listHeight
            )
        ) {
            return false;
        }

        int index = scrollOffset
            + (int) (
                (event.y() - listY)
                    / ROW_HEIGHT
            );

        if (
            index < 0
                || index
                    >= QuickLaunchRegistry.SLOT_COUNT
        ) {
            return true;
        }

        selectedSlot = index + 1;
        updateButtons();

        if (doubled) {
            configureSelectedSlot();
        }

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
        int listWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int listHeight =
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        if (
            !contains(
                mouseX,
                mouseY,
                listX,
                listY,
                listWidth,
                listHeight
            )
        ) {
            return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
            );
        }

        scrollOffset -=
            (int) Math.signum(verticalAmount)
                * SCROLL_STEP;

        clampScroll();
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

    private void renderRows(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int listX,
        int listY,
        int listWidth,
        int listHeight
    ) {
        graphics.enableScissor(
            listX + 1,
            listY + 1,
            listX + listWidth - 1,
            listY + listHeight - 1
        );

        int visibleRows =
            Math.max(1, listHeight / ROW_HEIGHT);

        int endIndex = Math.min(
            QuickLaunchRegistry.SLOT_COUNT,
            scrollOffset + visibleRows + 1
        );

        for (
            int index = scrollOffset;
            index < endIndex;
            index++
        ) {
            int rowY = listY
                + (index - scrollOffset)
                    * ROW_HEIGHT;

            renderRow(
                graphics,
                QuickLaunchRegistry.slot(index + 1),
                rowY,
                listX,
                listWidth,
                mouseX,
                mouseY
            );
        }

        graphics.disableScissor();
    }

    private void renderRow(
        GuiGraphics graphics,
        QuickLaunchSlot slot,
        int rowY,
        int listX,
        int listWidth,
        int mouseX,
        int mouseY
    ) {
        boolean selected =
            selectedSlot == slot.number();

        boolean hovered = contains(
            mouseX,
            mouseY,
            listX + 2,
            rowY,
            listWidth - 4,
            ROW_HEIGHT - 1
        );

        int background = selected
            ? 0xFF2A2237
            : hovered
                ? 0xFF201C29
                : 0xFF15121B;

        graphics.fill(
            listX + 2,
            rowY,
            listX + listWidth - 2,
            rowY + ROW_HEIGHT - 1,
            background
        );

        graphics.renderOutline(
            listX + 2,
            rowY,
            listWidth - 4,
            ROW_HEIGHT - 1,
            selected
                ? 0xFF9B79D1
                : hovered
                    ? 0xFF51465D
                    : 0xFF27222F
        );

        if (selected) {
            graphics.fill(
                listX + 2,
                rowY,
                listX + 5,
                rowY + ROW_HEIGHT - 1,
                0xFFB38AE8
            );
        }

        List<Scenario> scenarios =
            QuickLaunchRegistry.resolveScenarios(slot);

        int missingCount =
            QuickLaunchRegistry
                .missingScenarioIds(slot)
                .size();

        QuickLaunchValidation validation =
            QuickLaunchValidation.analyze(
                scenarios
            );

        String status;
        int statusColor;

        if (slot.empty()) {
            status = "Empty";
            statusColor = 0xFF716879;
        } else if (missingCount > 0) {
            status = missingCount
                + " missing";
            statusColor = 0xFFE66777;
        } else if (!validation.valid()) {
            status = "Conflict";
            statusColor = 0xFFE66777;
        } else {
            status = "Ready";
            statusColor = 0xFF61D394;
        }

        Component key =
            KarakuriClient
                .quickSlotKey(slot.number())
                .getTranslatedKeyMessage();

        int keyWidth = font.width(key);
        int statusWidth = font.width(status);
        int rightWidth = Math.max(
            keyWidth,
            statusWidth
        );

        graphics.drawString(
            font,
            Component.literal(slot.label()),
            listX + 12,
            rowY + 7,
            0xFFF0EAF4,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(
                    QuickLaunchRegistry.summary(slot),
                    Math.max(
                        30,
                        listWidth - rightWidth - 42
                    )
                )
            ),
            listX + 12,
            rowY + 24,
            0xFF9A90A4,
            false
        );

        graphics.drawString(
            font,
            key,
            listX + listWidth - keyWidth - 11,
            rowY + 7,
            0xFFCDB1F2,
            false
        );

        graphics.drawString(
            font,
            Component.literal(status),
            listX + listWidth - statusWidth - 11,
            rowY + 24,
            statusColor,
            false
        );
    }

    private void configureSelectedSlot() {
        minecraft.setScreen(
            new QuickLaunchSlotScreen(
                this,
                selectedSlot
            )
        );
    }

    private void clearSelectedSlot() {
        QuickLaunchRegistry.clearSlot(
            selectedSlot
        );
        updateButtons();
    }

    private void updateButtons() {
        if (
            clearButton == null
                || configureButton == null
        ) {
            return;
        }

        clearButton.active =
            !QuickLaunchRegistry
                .slot(selectedSlot)
                .empty();

        configureButton.active = true;
    }

    private void clampScroll() {
        int listHeight =
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        int visibleRows =
            Math.max(1, listHeight / ROW_HEIGHT);

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            Math.max(
                0,
                QuickLaunchRegistry.SLOT_COUNT
                    - visibleRows
            )
        );
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
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
                    value.substring(0, end)
                        + suffix
                ) > maximumWidth
        ) {
            end--;
        }

        return value.substring(0, end)
            + suffix;
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
}