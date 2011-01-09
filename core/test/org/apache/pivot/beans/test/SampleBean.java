package org.apache.pivot.beans.test;

public class SampleBean {
    public enum TestEnum {
        ABC_DEF
    }

    private int a = 20;
    private String b = "ABCD";
    private TestEnum testEnum = null;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void setA(String a) {
        setA(-Integer.parseInt(a));
    }

    public String getB() {
        return b;
    }

    public TestEnum getTestEnum() {
        return testEnum;
    }

    public void setTestEnum(TestEnum testEnum) {
        this.testEnum = testEnum;
    }
}
