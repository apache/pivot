package pivot.wtk.media.drawing;

/**
 * TODO Allow caller to specify angle in degrees or radians.
 */
public class Rotation extends Transform {
    private float angle = 0f;

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float[][] getMatrix() {
        // TODO
        return null;
    }
}
