package pivot.wtk.effects.easing;

public class Back implements Easing {
    private float overshoot;

    public Back() {
        this(1.70158f);
    }

    public Back(float overshoot) {
        this.overshoot = overshoot;
    }

    public float easeIn(float time, float begin, float change, float duration) {
        return change * (time /= duration) * time * ((overshoot + 1) * time - overshoot) + begin;
    }

    public float easeInOut(float time, float begin, float change, float duration) {
        return change * ((time = time / duration - 1) * time * ((overshoot + 1) * time + overshoot) + 1) + begin;
    }

    public float easeOut(float time, float begin, float change, float duration) {
        if ((time /= duration / 2) < 1) {
            return change / 2 * (time * time * (((overshoot *= (1.525)) + 1) * time - overshoot)) + begin;
        } else {
            return change / 2 * ((time -= 2) * time * (((overshoot *= (1.525)) + 1) * time + overshoot) + 2) + begin;
        }
    }

}
