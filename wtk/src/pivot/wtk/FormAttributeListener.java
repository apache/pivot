package pivot.wtk;

public interface FormAttributeListener {
    public void labelChanged(Form form, Component component, String previousLabel);
    public void flagChanged(Form form, Component component, Form.Flag previousFlag);
}
