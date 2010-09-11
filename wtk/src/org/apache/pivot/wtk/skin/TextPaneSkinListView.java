package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.text.List;

class TextPaneSkinListView extends TextPaneSkinVerticalElementView {

    protected int maxIndexTextWidth;

    public TextPaneSkinListView(TextPaneSkin textPaneSkin, List list) {
        super(textPaneSkin, list);
    }

    public int getMaxIndexTextWidth() {
        return maxIndexTextWidth;
    }

    @Override
    public void validate() {
        if (!isValid()) {
            verticalValidate();
            super.validate();
        }
    }
}
