package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Spacer;
import pivot.wtk.skin.ComponentSkin;

public class SpacerSkin extends ComponentSkin {
    protected Color color = DEFAULT_COLOR;
    protected int thickness = DEFAULT_THICKNESS;
    protected int padding = DEFAULT_PADDING;
    
    private static final Color DEFAULT_COLOR = Color.BLACK; 
    private static final int DEFAULT_THICKNESS = 1;
    private static final int DEFAULT_PADDING = 4;
    
    protected static final String COLOR_KEY = "color";
    protected static final String THICKNESS_KEY = "thickness";
    protected static final String PADDING_KEY = "padding";
    
    public void install(Component component) {
        validateComponentType(component, Spacer.class);
        
        super.install(component);
    }
    
    public int getPreferredWidth(int height) {
        return 0;
    }

    public int getPreferredHeight(int width) {
        return thickness + padding * 2;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        
        Line2D line = new Line2D.Double(0, height / 2, width, height / 2);
        graphics.setStroke(new BasicStroke(thickness));
        graphics.setColor(color);
        graphics.draw(line);
    }

    /**
     * @return
     * <tt>false</tt>; spacers are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }
    
    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(COLOR_KEY)) {
            value = color;
        } else if (key.equals(THICKNESS_KEY)) {
            value = thickness;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = color;
            color = (Color)value;

            repaintComponent();
        } else if (key.equals(THICKNESS_KEY)) {
            if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, false);

            previousValue = thickness;
            thickness = (Integer)value;

            invalidateComponent();
        } else if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, false);

            previousValue = padding;
            padding = (Integer)value;

            invalidateComponent();
        } else {
            previousValue = super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else if (key.equals(THICKNESS_KEY)) {
            previousValue = put(key, DEFAULT_THICKNESS);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(COLOR_KEY)
            || key.equals(THICKNESS_KEY)
            || key.equals(PADDING_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
