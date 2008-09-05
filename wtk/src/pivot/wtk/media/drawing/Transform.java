package pivot.wtk.media.drawing;

import pivot.wtk.Point;

public abstract class Transform {
    public Point transform(int x, int y) {
        // TODO
        return null;
    }

    public Point transform(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("point is null.");
        }

        return transform(point.x, point.y);
    }

    public abstract float[][] getMatrix();
}
