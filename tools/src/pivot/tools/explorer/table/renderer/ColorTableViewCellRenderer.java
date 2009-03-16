package pivot.tools.explorer.table.renderer;

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;

public class ColorTableViewCellRenderer extends AbstractFlowPaneTableViewCellRenderer {
    private class ColorBadge extends Image {
        private Color color = Color.BLACK;
        public static final int SIZE = 14;

        public int getWidth() {
            return SIZE;
        }

        public int getHeight() {
            return SIZE;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void paint(Graphics2D graphics) {
            graphics.setColor(color);
            graphics.fillRect(0, 0, SIZE, SIZE);
            graphics.setColor(Color.GRAY);
            graphics.drawRect(0, 0, SIZE - 1, SIZE - 1);
            graphics.setColor(Color.WHITE);
            graphics.drawRect(1, 1, SIZE - 3, SIZE - 3);
            graphics.dispose();
        }
    }

    private ColorBadge colorBadge = new ColorBadge();
    private Label label = new Label();

    public ColorTableViewCellRenderer() {
        super();
        add(new ImageView(colorBadge));
        add(label);
    }

    @Override
    protected Component getStyleComponent() {
        return label;
    }

    public void render(Object cellData) {
        if (cellData instanceof Color) {
            Color color = (Color)cellData;
            colorBadge.setColor(color);
            label.setText(colorToHex(color));
        }
    }

    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(),
            color.getBlue());
    }
}
