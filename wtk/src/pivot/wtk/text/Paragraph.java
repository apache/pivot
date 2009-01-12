/*
 * Copyright (c) 2009 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.text;

/**
 * Node representing a paragraph element.
 *
 * @author gbrown
 */
public class Paragraph extends Block {
    public Paragraph() {
        super();
    }

    public Paragraph(String text) {
        super();
        add(new TextNode(text));
    }

    public Paragraph(Paragraph paragraph, boolean recursive) {
        super(paragraph, recursive);
    }

    @Override
    public void insert(Node node, int index) {
        if (node instanceof Element
            && !(node instanceof Span)) {
            throw new IllegalArgumentException("Child node must be an instance of "
                + TextNode.class.getName() + " or " + Span.class.getName());
        }

        super.insert(node, index);
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new Paragraph(this, recursive);
    }
}
