package pivot.core.test;

import pivot.util.MIMEType;

public class MIMETypeTest {
    public static void main(String[] args) {
        MIMEType mimeType = MIMEType.decode("foo; a=123; b=456; c=789");
        System.out.println("Base type: " + mimeType.getBaseType());
        System.out.println("a: " + mimeType.get("a"));
        System.out.println("b: " + mimeType.get("b"));
        System.out.println("c: " + mimeType.get("c"));
    }
}
