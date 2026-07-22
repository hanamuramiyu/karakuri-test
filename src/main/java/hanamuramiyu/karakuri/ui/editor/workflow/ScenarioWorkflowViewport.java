package hanamuramiyu.karakuri.ui.editor.workflow;

final class ScenarioWorkflowViewport {
    static final int CARD_HEIGHT = 58;

    private static final int CARD_MIN_WIDTH = 104;
    private static final int CARD_MAX_WIDTH = 132;
    private static final int CARD_GAP = 30;
    private static final int CONTENT_PADDING = 12;
    private static final int VISIBILITY_PADDING = 8;
    private static final int FOOTER_HEIGHT = 16;
    private static final int MIN_CARD_TOP_PADDING = 60;
    private static final int AUTO_SCROLL_EDGE = 24;
    private static final int AUTO_SCROLL_STEP = 8;
    private static final int SCROLL_STEP = 36;

    private int x;
    private int y;
    private int width;
    private int height;
    private int scrollOffset;

    void setBounds(
        int x,
        int y,
        int width,
        int height,
        int stepCount
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        clampScrollOffset(stepCount);
    }


    void resetScroll() {
        scrollOffset = 0;
    }

    int x() {
        return x;
    }

    int y() {
        return y;
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    int cardWidth() {
        return Math.clamp(
            width / 3,
            CARD_MIN_WIDTH,
            CARD_MAX_WIDTH
        );
    }

    int cardY() {
        int usableHeight =
            height - FOOTER_HEIGHT;

        return y
            + Math.max(
                MIN_CARD_TOP_PADDING,
                (
                    usableHeight
                        - CARD_HEIGHT
                ) / 2
            );
    }

    int cardX(int index) {
        return x
            + CONTENT_PADDING
            - scrollOffset
            + index * cardStride();
    }

    boolean contains(
        double mouseX,
        double mouseY
    ) {
        return mouseX >= x
            && mouseX < x + width
            && mouseY >= y
            && mouseY < y + height;
    }

    int findCardIndex(
        double mouseX,
        double mouseY,
        int stepCount
    ) {
        int cardY = cardY();

        for (
            int index = 0;
            index < stepCount;
            index++
        ) {
            if (
                isInsideCard(
                    mouseX,
                    mouseY,
                    cardX(index),
                    cardY
                )
            ) {
                return index;
            }
        }

        return -1;
    }

    int dragTargetIndex(
        double mouseX,
        int stepCount
    ) {
        double localX =
            mouseX
                - x
                - CONTENT_PADDING
                + scrollOffset
                + cardStride() / 2.0;

        return Math.clamp(
            (int) Math.floor(
                localX / cardStride()
            ),
            0,
            stepCount - 1
        );
    }

    void scrollBy(
        double amount,
        int stepCount
    ) {
        scrollOffset = Math.clamp(
            scrollOffset
                - (int) Math.round(
                    amount * SCROLL_STEP
                ),
            0,
            maxScrollOffset(stepCount)
        );
    }

    void autoScroll(
        double mouseX,
        int stepCount
    ) {
        if (mouseX < x + AUTO_SCROLL_EDGE) {
            scrollOffset -= AUTO_SCROLL_STEP;
        } else if (
            mouseX > x + width - AUTO_SCROLL_EDGE
        ) {
            scrollOffset += AUTO_SCROLL_STEP;
        }

        clampScrollOffset(stepCount);
    }

    void ensureIndexVisible(
        int index,
        int stepCount
    ) {
        if (
            width <= 0
                || stepCount == 0
        ) {
            return;
        }

        int localLeft =
            CONTENT_PADDING
                + index * cardStride();

        int localRight =
            localLeft + cardWidth();

        if (
            localLeft - scrollOffset
                < VISIBILITY_PADDING
        ) {
            scrollOffset =
                localLeft
                    - VISIBILITY_PADDING;
        } else if (
            localRight - scrollOffset
                > width - VISIBILITY_PADDING
        ) {
            scrollOffset =
                localRight
                    - width
                    + VISIBILITY_PADDING;
        }

        clampScrollOffset(stepCount);
    }

    private boolean isInsideCard(
        double mouseX,
        double mouseY,
        int cardX,
        int cardY
    ) {
        return mouseX >= cardX
            && mouseX < cardX + cardWidth()
            && mouseY >= cardY
            && mouseY < cardY + CARD_HEIGHT;
    }

    private int cardStride() {
        return cardWidth() + CARD_GAP;
    }

    private int maxScrollOffset(
        int stepCount
    ) {
        int totalWidth =
            CONTENT_PADDING * 2
                + stepCount * cardWidth()
                + Math.max(
                    0,
                    stepCount - 1
                ) * CARD_GAP;

        return Math.max(
            0,
            totalWidth - width
        );
    }

    private void clampScrollOffset(
        int stepCount
    ) {
        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            maxScrollOffset(stepCount)
        );
    }
}