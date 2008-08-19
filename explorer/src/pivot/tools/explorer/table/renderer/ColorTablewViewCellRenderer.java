package pivot.tools.explorer.table.renderer;

import java.awt.Color;
import java.awt.Graphics;

import pivot.wtk.ImageView;
import pivot.wtk.Label;
import pivot.wtk.TableView;
import pivot.wtk.TableView.Column;
import pivot.wtk.media.Image;

public class ColorTablewViewCellRenderer extends AbstractFlowPaneTableViewCellRenderer {

	private Image image;
	private static final int SIZE = 15;

	private Label text = new Label();


	public ColorTablewViewCellRenderer() {
		super();
		setStyleComponent(text);

		image = Image.load(getClass().getResource("square16.png"));

    	add( new ImageView( image ));
    	add( text );
	}

	public void render(Object value, TableView tableView, Column column, boolean rowSelected, boolean rowHighlighted,
			boolean rowDisabled) {

		Object cellData = getCellData(value, column);
        if (cellData instanceof Color) {

    		Graphics g = image.getGraphics();
        	g.setColor((Color)cellData);
        	g.fillRect(0, 0, SIZE, SIZE);
        	g.setColor(Color.BLACK);
        	g.drawRect(0, 0, SIZE, SIZE);
        	g.dispose();

        	text.setText( cellData.toString());

        }

        renderStyles(tableView, rowSelected, rowDisabled);

	}

}
