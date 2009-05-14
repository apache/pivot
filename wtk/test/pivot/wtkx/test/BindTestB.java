package pivot.wtkx.test;

import pivot.wtk.PushButton;

public class BindTestB extends BindTestA {
    @Bind(fieldName="window")
    private static PushButton pushButton;

    public static void main(String[] args) throws Exception{
        BindTestB bindTestB = new BindTestB();
        bindTestB.bind();
        System.out.println(pushButton.getButtonData());
    }
}
