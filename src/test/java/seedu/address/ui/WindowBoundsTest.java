package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

public class WindowBoundsTest {

    private static final Rectangle2D PRIMARY = new Rectangle2D(0, 0, 1707, 912);
    private static final Rectangle2D LEFT = new Rectangle2D(-1920, 0, 1920, 1040);
    private static final Rectangle2D ABOVE = new Rectangle2D(0, -1080, 1920, 1080);
    private static final List<Rectangle2D> ONE_SCREEN = List.of(PRIMARY);

    @Test
    public void fit_validBounds_unchanged() {
        GuiSettings saved = new GuiSettings(800, 650, 100, 50);
        assertEquals(saved, WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_validBoundsOnSecondaryScreenWithNegativeCoordinates_unchanged() {
        GuiSettings saved = new GuiSettings(800, 650, -1500, 100);
        assertEquals(saved, WindowBounds.fit(saved, List.of(PRIMARY, LEFT)));
    }

    @Test
    public void fit_windowSpanningTwoScreens_unchanged() {
        GuiSettings saved = new GuiSettings(1000, 700, -500, 100);
        assertEquals(saved, WindowBounds.fit(saved, List.of(PRIMARY, LEFT)));
    }

    @Test
    public void fit_windowOnRemovedScreen_centredOnPrimary() {
        GuiSettings saved = new GuiSettings(740, 600, -1500, 100);
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_positionOffAllScreens_centredOnPrimary() {
        GuiSettings saved = new GuiSettings(740, 600, 5000, 5000);
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_mostlyOffScreen_centred() {
        GuiSettings saved = new GuiSettings(740, 600, 1600, 800);
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_partiallyOffScreenButMostlyVisible_unchanged() {
        GuiSettings saved = new GuiSettings(740, 600, 1200, 400);
        assertEquals(saved, WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_mostlyOffScreen_centredOnMostOverlappedScreen() {
        GuiSettings saved = new GuiSettings(740, 600, -1800, 900);
        // window mostly below the left screen, which is the one it overlaps most
        assertEquals(new GuiSettings(740, 600, -1330, 220), WindowBounds.fit(saved, List.of(PRIMARY, LEFT)));
    }

    @Test
    public void fit_noOverlapWithAnyOfSeveralScreens_centredOnPrimary() {
        GuiSettings saved = new GuiSettings(740, 600, 5000, 5000);
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(saved, List.of(PRIMARY, LEFT)));
    }

    @Test
    public void fit_exactlyHalfVisible_unchanged() {
        GuiSettings saved = new GuiSettings(740, 600, 1337, 100);
        assertEquals(saved, WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_validBoundsOnScreenAbovePrimary_unchanged() {
        GuiSettings saved = new GuiSettings(740, 600, 100, -900);
        assertEquals(saved, WindowBounds.fit(saved, List.of(PRIMARY, ABOVE)));
    }

    @Test
    public void fit_titleBarAboveScreen_centred() {
        GuiSettings saved = new GuiSettings(740, 600, 100, -100);
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_titleBarHalfVisible_unchanged() {
        GuiSettings saved = new GuiSettings(740, 600, 100, -20);
        assertEquals(saved, WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_bodyUnderHalfVisibleAtBottomEdge_centred() {
        GuiSettings saved = new GuiSettings(740, 600, 100, 880);
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_windowAcrossScreenAbovePrimary_unchanged() {
        GuiSettings saved = new GuiSettings(740, 600, 100, -100);
        assertEquals(saved, WindowBounds.fit(saved, List.of(PRIMARY, ABOVE)));
    }

    @Test
    public void fit_absentPosition_centredOnPrimary() {
        assertEquals(new GuiSettings(740, 600, 483, 156), WindowBounds.fit(new GuiSettings(), ONE_SCREEN));
    }

    @Test
    public void fit_unusableSizes_defaultSize() {
        double[] bad = {0, -5, 10, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double size : bad) {
            assertEquals(new GuiSettings(740, 600, 100, 100),
                    WindowBounds.fit(new GuiSettings(size, size, 100, 100), ONE_SCREEN), "size " + size);
        }
    }

    @Test
    public void fit_hugeSize_limitedToScreen() {
        GuiSettings saved = new GuiSettings(10000, 10000, 0, 0);
        assertEquals(new GuiSettings(1707, 912, 0, 0), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_hugeSizeOffScreen_limitedToScreenAndCentred() {
        GuiSettings saved = new GuiSettings(10000, 10000, 5000, 5000);
        assertEquals(new GuiSettings(1707, 912, 0, 0), WindowBounds.fit(saved, ONE_SCREEN));
    }

    @Test
    public void fit_screenSmallerThanDefault_limitedToScreen() {
        Rectangle2D small = new Rectangle2D(0, 0, 1280, 560);
        assertEquals(new GuiSettings(740, 560, 270, 0),
                WindowBounds.fit(new GuiSettings(740, 600, 5000, 5000), List.of(small)));
    }

    @Test
    public void fit_noScreens_unchanged() {
        GuiSettings saved = new GuiSettings(740, 600, 5000, 5000);
        assertSame(saved, WindowBounds.fit(saved, List.of()));
    }
}
