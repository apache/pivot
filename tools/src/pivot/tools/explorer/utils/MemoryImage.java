package pivot.tools.explorer.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import pivot.wtk.Dimensions;
import pivot.wtk.media.Image;

/**
 *
 * Image based on BufferedImage with fixed size
 * Can be used draw images on the fly
 *
 * @author Eugene Ryzhikov
 * @date   Aug 19, 2008
 *
 */
public class MemoryImage extends Image {

	private BufferedImage image;

	public MemoryImage( int width, int height ) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public MemoryImage( int size ) {
		this(size,size);
	}

	@Override
	public Graphics2D getGraphics() {
		return (Graphics2D) image.getGraphics();
	}

	public int getHeight() {
		return image.getHeight();
	}

	public int getPreferredHeight(int width) {
		return getHeight();
	}

	public Dimensions getPreferredSize() {
		return new Dimensions(getWidth(),getHeight());
	}

	public int getPreferredWidth(int height) {
		return getWidth();
	}

	public int getWidth() {
		return image.getWidth();
	}

	public void paint(Graphics2D graphics) {
		graphics.drawImage(image, 0, 0, null);
	}

	public void setSize(int width, int height) {
	}

}
