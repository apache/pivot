package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.text.List;

class TextAreaSkinListView extends TextAreaSkinVerticalElementView {

    protected int maxIndexTextWidth;
    
    public TextAreaSkinListView(TextAreaSkin textAreaSkin, List list) {
        super(textAreaSkin, list);
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
