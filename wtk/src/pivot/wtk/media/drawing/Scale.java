package pivot.wtk.media.drawing;

public class Scale extends Transform {
    private float x = 0;
    private float y = 0;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float[][] getMatrix() {
        // TODO
        return null;
    }
}
