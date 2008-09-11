package pivot.wtk.effects.easing;

public interface Easing {
    public float easeIn(float time, float begin, float change, float duration);
    public float easeOut(float time, float begin, float change, float duration);
    public float easeInOut(float time, float begin, float change, float duration);
}
