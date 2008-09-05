package pivot.wtk.media.drawing;

public class Translation extends Transform {
    private int x = 0;
    private int y = 0;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float[][] getMatrix() {
        // TODO
        return null;
    }
}
