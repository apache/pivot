package pivot.wtk.skin.text;

import java.awt.Graphics2D;

import pivot.wtk.Dimensions;
import pivot.wtk.text.TextNode;
import pivot.wtk.text.TextNodeListener;

public class TextNodeView extends NodeView implements TextNodeListener {
    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void paint(Graphics2D graphics) {
        // TODO Auto-generated method stub

    }

    public NodeView breakAt(int x) {
        // TODO
        return null;
    }

    public void charactersInserted(TextNode textNode, int index, int count) {
        // TODO Auto-generated method stub

    }

    public void charactersRemoved(TextNode textNode, int index,
        String characters) {
        // TODO Auto-generated method stub
    }
}
