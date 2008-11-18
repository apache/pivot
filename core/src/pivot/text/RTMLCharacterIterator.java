package pivot.text;

import java.text.AttributedCharacterIterator;

import org.w3c.dom.Element;

public class RTMLCharacterIterator implements AttributedCharacterIterator {
    private Element element;

    public RTMLCharacterIterator(Element element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null.");
        }

        this.element = element;
    }

    public int getIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getBeginIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getEndIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    public char setIndex(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public char current() {
        // TODO Auto-generated method stub
        return 0;
    }

    public char first() {
        // TODO Auto-generated method stub
        return 0;
    }

    public char last() {
        // TODO Auto-generated method stub
        return 0;
    }

    public char next() {
        // TODO Auto-generated method stub
        return 0;
    }

    public char previous() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRunLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRunLimit(Attribute arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRunLimit(java.util.Set<? extends Attribute> arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRunStart() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRunStart(Attribute arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getRunStart(java.util.Set<? extends Attribute> arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object getAttribute(Attribute arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public java.util.Map<Attribute, Object> getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    public java.util.Set<Attribute> getAllAttributeKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object clone() {
        // TODO
        return null;
    }
}
