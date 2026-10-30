package seedu.address.ui;

import java.awt.Point;
import java.util.List;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

/**
 * Checks saved window settings against the screens that are currently available.
 */
final class WindowBounds {

    private static final double MIN_WIDTH = 450;
    private static final double MIN_HEIGHT = 600;
    private static final double DEFAULT_WIDTH = 740;
    private static final double DEFAULT_HEIGHT = 600;
    private static final double MIN_VISIBLE_FRACTION = 0.5;
    private static final double TITLE_BAR_HEIGHT = 40;

    private WindowBounds() {}

    /**
     * Returns {@code saved} if it is usable on {@code screens} (visual bounds, primary screen first). Otherwise
     * returns settings with a sensible size, centred on the screen that overlaps the saved window most (or the
     * primary screen if none does). Nothing is changed when {@code screens} is empty.
     */
    static GuiSettings fit(GuiSettings saved, List<Rectangle2D> screens) {
        if (screens.isEmpty()) {
            return saved;
        }

        double width = isUsable(saved.getWindowWidth(), MIN_WIDTH) ? saved.getWindowWidth() : DEFAULT_WIDTH;
        double height = isUsable(saved.getWindowHeight(), MIN_HEIGHT) ? saved.getWindowHeight() : DEFAULT_HEIGHT;
        Point position = saved.getWindowCoordinates();

        Rectangle2D all = boundingBox(screens);
        width = Math.min(width, all.getWidth());
        height = Math.min(height, all.getHeight());

        if (position != null) {
            Rectangle2D window = new Rectangle2D(position.x, position.y, width, height);
            if (isMostlyVisible(window, screens) && isMostlyVisible(titleBar(window), screens)) {
                return new GuiSettings(width, height, position.x, position.y);
            }
        }

        Rectangle2D screen = position == null ? screens.get(0) : mostOverlapped(position, width, height, screens);
        width = Math.min(width, screen.getWidth());
        height = Math.min(height, screen.getHeight());
        return new GuiSettings(width, height,
                (int) (screen.getMinX() + (screen.getWidth() - width) / 2),
                (int) (screen.getMinY() + (screen.getHeight() - height) / 2));
    }

    private static boolean isUsable(double size, double minimum) {
        return Double.isFinite(size) && size >= minimum;
    }

    private static Rectangle2D boundingBox(List<Rectangle2D> screens) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Rectangle2D screen : screens) {
            minX = Math.min(minX, screen.getMinX());
            minY = Math.min(minY, screen.getMinY());
            maxX = Math.max(maxX, screen.getMaxX());
            maxY = Math.max(maxY, screen.getMaxY());
        }
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    private static Rectangle2D titleBar(Rectangle2D window) {
        return new Rectangle2D(window.getMinX(), window.getMinY(), window.getWidth(),
                Math.min(TITLE_BAR_HEIGHT, window.getHeight()));
    }

    private static boolean isMostlyVisible(Rectangle2D area, List<Rectangle2D> screens) {
        return visibleArea(area, screens) >= MIN_VISIBLE_FRACTION * area.getWidth() * area.getHeight();
    }

    private static double visibleArea(Rectangle2D window, List<Rectangle2D> screens) {
        double area = 0;
        for (Rectangle2D screen : screens) {
            area += overlap(window, screen);
        }
        return area;
    }

    private static Rectangle2D mostOverlapped(Point position, double width, double height,
            List<Rectangle2D> screens) {
        Rectangle2D window = new Rectangle2D(position.x, position.y, width, height);
        Rectangle2D best = screens.get(0);
        for (Rectangle2D screen : screens) {
            if (overlap(window, screen) > overlap(window, best)) {
                best = screen;
            }
        }
        return best;
    }

    private static double overlap(Rectangle2D a, Rectangle2D b) {
        double overlapWidth = Math.min(a.getMaxX(), b.getMaxX()) - Math.max(a.getMinX(), b.getMinX());
        double overlapHeight = Math.min(a.getMaxY(), b.getMaxY()) - Math.max(a.getMinY(), b.getMinY());
        return overlapWidth > 0 && overlapHeight > 0 ? overlapWidth * overlapHeight : 0;
    }
}
