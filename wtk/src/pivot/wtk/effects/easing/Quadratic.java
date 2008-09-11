package pivot.wtk.effects.easing;

public class Quadratic implements Easing {
    public float easeIn(float time, float begin, float change, float duration) {
        return change * (time /= duration) * time + begin;
    }

    public float easeOut(float time, float begin, float change, float duration) {
        return -change * (time /= duration) * (time - 2) + begin;
    }

    public float easeInOut(float time, float begin, float change, float duration) {
        if ((time /= duration / 2) < 1) {
            return change / 2 * time * time + begin;
        } else {
            return -change / 2 * ((--time) * (time - 2) - 1) + begin;
        }
    }
}
