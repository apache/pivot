package pivot.wtk.effects.easing;

public class Linear implements Easing {
    public float easeIn(float time, float begin, float change, float duration) {
        return change * time / duration + begin;
    }

    public float easeOut(float time, float begin, float change, float duration) {
        return change * time / duration + begin;
    }

    public float easeInOut(float time, float begin, float change, float duration) {
        return change * time / duration + begin;
    }
}
