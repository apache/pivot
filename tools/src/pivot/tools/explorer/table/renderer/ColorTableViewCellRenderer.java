package pivot.tools.explorer.table.renderer;

import java.awt.Color;
import java.awt.Graphics;

import pivot.tools.explorer.utils.BufferedImage;
import pivot.wtk.Component;
import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.media.Image;

public class ColorTableViewCellRenderer extends AbstractFlowPaneTableViewCellRenderer {

	private Image image = new BufferedImage(14);
	private Label text  = new Label();


	public ColorTableViewCellRenderer() {
		super();
    	add( new ImageView( image ));
    	add( text );
	}

	@Override
	protected Component getStyleComponent() {
		return text;
	}

	public void render(Object cellData) {
        if (cellData instanceof Color) {
        	Color color = (Color)cellData;
        	drawColorBadge(color);
        	text.setText(colorToHex(color));
        }
	}

	private String colorToHex( Color color ) {
		return String.format( "#%02x%02x%02x",color.getRed(), color.getGreen(), color.getBlue() );
	}

	private void drawColorBadge( Color color ) {

		int size = image.getHeight()-1;
		Graphics g = image.getGraphics();
    	g.setColor(color);
    	g.fillRect(0, 0, size, size);
    	g.setColor(Color.GRAY);
    	g.drawRect(0, 0, size, size);
    	g.setColor(Color.WHITE);
    	g.drawRect(1, 1, size-2, size-2);
    	g.dispose();

	}

}
