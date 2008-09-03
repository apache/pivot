package pivot.wtk;

public interface BorderListener {
    public void titleChanged(Border border, String previousTitle);
    public void contentChanged(Border border, Component previousContent);
}
