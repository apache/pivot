/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.demos.richtexteditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.ColorChooserButtonSelectionListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListButtonDataRenderer;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Element;
import org.apache.pivot.wtk.text.Node;
import org.apache.pivot.wtk.text.Paragraph;
import org.apache.pivot.wtk.text.TextNode;

/**
 * Demonstrates the use of the rich-text functionality in TextArea.
 */
public class RichTextEditorDemo implements Application {
    private Window window = null;
    @BXML
    private TextArea textarea = null;
    @BXML
    private PushButton openFileButton = null;
    @BXML
    private PushButton saveFileButton = null;
    @BXML
    private PushButton boldButton = null;
    @BXML
    private PushButton italicButton = null;
    @BXML
    private PushButton underlineButton = null;
    @BXML
    private PushButton strikethroughButton = null;
    @BXML
    private ColorChooserButton foregroundColorChooserButton = null;
    @BXML
    private ColorChooserButton backgroundColorChooserButton = null;
    @BXML
    private ListButton fontFamilyListButton = null;
    @BXML
    private ListButton fontSizeListButton = null;
    @BXML
    private Checkbox wrapTextCheckbox = null;
    @BXML
    private PushButton alignLeftButton= null;
    @BXML
    private PushButton alignCentreButton= null;
    @BXML
    private PushButton alignRightButton= null;

    private File loadedFile = null;

    public RichTextEditorDemo() {
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(RichTextEditorDemo.class, "richtexteditor.bxml");
        bxmlSerializer.bind(this, RichTextEditorDemo.class);

        window.setTitle("Apache Pivot Rich Text Editor Demo");

        // make the text on the "bold" button bold
        Font boldButtonFont = (Font)boldButton.getStyles().get("font");
        boldButton.getStyles().put("font", boldButtonFont.deriveFont(Font.BOLD));

        // make the text on the "italic" button italic
        Font italicButtonFont = (Font)italicButton.getStyles().get("font");
        italicButton.getStyles().put("font", italicButtonFont.deriveFont(Font.ITALIC));

        fontFamilyListButton.setListData(new ArrayList<String>(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        fontSizeListButton.setSelectedItem(fontFamilyListButton.getListData().get(0));
        fontFamilyListButton.setItemRenderer(new ListViewItemRenderer() {
            @Override
            public void render(Object item, int index, ListView listView, boolean selected,
                boolean checked, boolean highlighted, boolean disabled) {
                super.render(item, index, listView, selected, checked, highlighted, disabled);
                if (item != null) {
                    String fontFamilyName = (String)item;
                    label.getStyles().put("font", Font.decode(fontFamilyName + "-12"));
                }
            }
        });
        fontFamilyListButton.setDataRenderer(new ListButtonDataRenderer() {
            @Override
            public void render(Object data, Button button, boolean highlight) {
                super.render(data, button, highlight);
                if (data != null) {
                    String fontFamilyName = (String)data;
                    label.getStyles().put("font", Font.decode(fontFamilyName + "-12"));
                }
            }
        });

        fontSizeListButton.setListData(new NumericSpinnerData(12, 30, 1));
        fontSizeListButton.setSelectedItem(12);

        openFileButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();

                fileBrowserSheet.setMode(FileBrowserSheet.Mode.OPEN);
                fileBrowserSheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            loadedFile = fileBrowserSheet.getSelectedFile();

                            try {
                                StringBuilder buf = new StringBuilder();
                                BufferedReader reader = new BufferedReader(new FileReader(
                                    loadedFile));
                                while (reader.ready()) {
                                    buf.append(reader.readLine());
                                    buf.append("\n");
                                }
                                reader.close();
                                textarea.setText(buf.toString());
                                window.setTitle(loadedFile.getCanonicalPath());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Alert.alert(ex.getMessage(), window);
                            }
                        }
                    }
                });
            }
        });

        saveFileButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet fileBrowserSheet = new FileBrowserSheet();

                if (loadedFile != null) {
                    fileBrowserSheet.setMode(FileBrowserSheet.Mode.SAVE_TO);
                    fileBrowserSheet.setSelectedFile(loadedFile);
                } else {
                    fileBrowserSheet.setMode(FileBrowserSheet.Mode.SAVE_AS);
                }
                fileBrowserSheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File selectedFile = fileBrowserSheet.getSelectedFile();

                            try {
                                String buf = textarea.getText();
                                FileWriter writer = new FileWriter(selectedFile);
                                writer.write(buf);
                                writer.close();
                                loadedFile = selectedFile;
                                textarea.setText(buf.toString());
                                window.setTitle(loadedFile.getCanonicalPath());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                Alert.alert(ex.getMessage(), window);
                            }
                        }
                    }
                });
            }
        });

        boldButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyStyleToSelection(new StyleApplicator() {
                    @Override
                    public void apply(org.apache.pivot.wtk.text.Span span) {
                        if (span.getFont() != null) {
                            Font font = span.getFont();
                            if (font.getStyle() == Font.PLAIN) {
                                font = font.deriveFont(Font.BOLD);
                            } else if (font.getStyle() == Font.BOLD) {
                                font = font.deriveFont(Font.PLAIN);
                            } else {
                                // the font is BOLD+ITALIC
                                font = font.deriveFont(Font.ITALIC);
                            }
                            span.setFont(font);
                        } else {
                            span.setFont("Arial BOLD 12");
                        }
                    }
                });
            }
        });

        italicButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyStyleToSelection(new StyleApplicator() {
                    @Override
                    public void apply(org.apache.pivot.wtk.text.Span span) {
                        if (span.getFont() != null) {
                            Font font = span.getFont();
                            if (font.getStyle() == Font.PLAIN) {
                                font = font.deriveFont(Font.ITALIC);
                            } else if (font.getStyle() == Font.ITALIC) {
                                font = font.deriveFont(Font.PLAIN);
                            } else {
                                // the font is BOLD+ITALIC
                                font = font.deriveFont(Font.BOLD);
                            }
                            span.setFont(font);
                        } else {
                            span.setFont("Arial ITALIC 12");
                        }
                    }
                });
            }
        });

        underlineButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyStyleToSelection(new StyleApplicator() {
                    @Override
                    public void apply(org.apache.pivot.wtk.text.Span span) {
                        span.setUnderline(!span.isUnderline());
                    }
                });
            }
        });

        strikethroughButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyStyleToSelection(new StyleApplicator() {
                    @Override
                    public void apply(org.apache.pivot.wtk.text.Span span) {
                        span.setStrikethrough(!span.isStrikethrough());
                    }
                });
            }
        });

        foregroundColorChooserButton.getColorChooserButtonSelectionListeners().add(
            new ColorChooserButtonSelectionListener() {
                @Override
                public void selectedColorChanged(ColorChooserButton colorChooserButton,
                    Color previousSelectedColor) {
                    applyStyleToSelection(new StyleApplicator() {
                        @Override
                        public void apply(org.apache.pivot.wtk.text.Span span) {
                            span.setForegroundColor(foregroundColorChooserButton.getSelectedColor());
                        }
                    });
                }
            });

        backgroundColorChooserButton.getColorChooserButtonSelectionListeners().add(
            new ColorChooserButtonSelectionListener() {
                @Override
                public void selectedColorChanged(ColorChooserButton colorChooserButton,
                    Color previousSelectedColor) {
                    applyStyleToSelection(new StyleApplicator() {
                        @Override
                        public void apply(org.apache.pivot.wtk.text.Span span) {
                            span.setBackgroundColor(backgroundColorChooserButton.getSelectedColor());
                        }
                    });
                }
            });

        ListButtonSelectionListener fontButtonPressListener = new ListButtonSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                int selectedFontSize = (Integer)fontSizeListButton.getSelectedItem();
                String selectedFontFamily = (String)fontFamilyListButton.getSelectedItem();
                final Font derivedFont = Font.decode(selectedFontFamily + " " + selectedFontSize);

                applyStyleToSelection(new StyleApplicator() {
                    @Override
                    public void apply(org.apache.pivot.wtk.text.Span span) {
                        span.setFont(derivedFont);
                    }
                });
            }
        };
        fontFamilyListButton.getListButtonSelectionListeners().add(fontButtonPressListener);
        fontSizeListButton.getListButtonSelectionListeners().add(fontButtonPressListener);

        wrapTextCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                textarea.getStyles().put("wrapText", wrapTextCheckbox.isSelected());
            }
        });

        alignLeftButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyAlignmentStyle(HorizontalAlignment.LEFT);
            }
        });

        alignCentreButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyAlignmentStyle(HorizontalAlignment.CENTER);
            }
        });

        alignRightButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                applyAlignmentStyle(HorizontalAlignment.RIGHT);
            }
        });

        window.open(display);
        textarea.requestFocus();
    }

    private interface StyleApplicator {
        void apply(org.apache.pivot.wtk.text.Span span);
    }

    private void applyAlignmentStyle(HorizontalAlignment horizontalAlignment) {
        Node node = textarea.getDocument().getDescendantAt(textarea.getSelectionStart());
        while (node != null && !(node instanceof Paragraph)) {
            node = node.getParent();
        }
        if (node != null) {
            Paragraph paragraph = (Paragraph) node;
            paragraph.setHorizontalAlignment(horizontalAlignment);
        }
    }

    /** debugging tools */
    @SuppressWarnings("unused")
    private void dumpDocument() {
        dumpDocumentNode(textarea.getDocument(), System.out, 0);
    }

    /** debugging tools */
    private void dumpDocumentNode(Node node, PrintStream printStream, final int indent) {
        for (int i = 0; i < indent; i++) {
            printStream.append("  ");
        }
        printStream.append("<" + node.getClass().getSimpleName() + ">");
        if (node instanceof TextNode) {
            TextNode textNode = (TextNode)node;
            String text = textNode.getText();
            printStream.append(text);
            printStream.append("</" + node.getClass().getSimpleName() + ">");
            printStream.println();
        } else {
            printStream.println();
            if (node instanceof Element) {
                Element element = (Element)node;

                for (Node childNode : element) {
                    dumpDocumentNode(childNode, printStream, indent + 1);
                }
            } else {
                String text = node.toString();
                printStream.append(text);
            }
            for (int i = 0; i < indent; i++) {
                printStream.append("  ");
            }
            printStream.append("</" + node.getClass().getSimpleName() + ">");
            printStream.println();
        }
    }

    private void applyStyleToSelection(StyleApplicator styleApplicator) {
        org.apache.pivot.wtk.Span span = textarea.getSelection();
        if (span == null) {
            return;
        }
        applyStyle(textarea.getDocument(), span, styleApplicator);
    }

    private void applyStyle(Document document, org.apache.pivot.wtk.Span selectionSpan,
        StyleApplicator styleApplicator) {
        // I can't apply the styles while iterating over the tree, because I
        // need to update the tree.
        // So first collect a list of all the nodes in the tree.
        List<Node> nodeList = new ArrayList<Node>();
        collectNodes(document, nodeList);

        final int selectionStart = textarea.getSelectionStart();
        final int selectionLength = textarea.getSelectionLength();

        for (Node node : nodeList) {
            if (node instanceof org.apache.pivot.wtk.text.Span) {
                org.apache.pivot.wtk.text.Span span = (org.apache.pivot.wtk.text.Span)node;
                int documentOffset = node.getDocumentOffset();
                int characterCount = node.getCharacterCount();
                org.apache.pivot.wtk.Span textSpan = new org.apache.pivot.wtk.Span(documentOffset,
                    documentOffset + characterCount);
                if (selectionSpan.intersects(textSpan)) {
                    applyStyleToSpanNode(selectionSpan, styleApplicator, span, characterCount,
                        textSpan);
                }
            }
            if (node instanceof org.apache.pivot.wtk.text.TextNode) {
                org.apache.pivot.wtk.text.TextNode textNode = (org.apache.pivot.wtk.text.TextNode)node;
                int documentOffset = node.getDocumentOffset();
                int characterCount = node.getCharacterCount();
                org.apache.pivot.wtk.Span textSpan = new org.apache.pivot.wtk.Span(documentOffset,
                    documentOffset + characterCount);
                if (selectionSpan.intersects(textSpan)) {
                    applyStyleToTextNode(selectionSpan, styleApplicator, textNode, characterCount,
                        textSpan);
                }
            }
        }

        // maintain the selected range
        textarea.setSelection(selectionStart, selectionLength);
    }

    private void applyStyleToTextNode(org.apache.pivot.wtk.Span selectionSpan,
        StyleApplicator styleApplicator, org.apache.pivot.wtk.text.TextNode textNode,
        int characterCount, org.apache.pivot.wtk.Span textSpan) {
        if (selectionSpan.contains(textSpan)) {
            // if the text-node is contained wholly inside the selection, remove
            // the text-node, replace it with a Span, and apply the style
            Element parent = textNode.getParent();
            org.apache.pivot.wtk.text.Span newSpanNode = new org.apache.pivot.wtk.text.Span();
            newSpanNode.add(new TextNode(textNode.getText()));
            styleApplicator.apply(newSpanNode);
            int index = parent.remove(textNode);
            parent.insert(newSpanNode, index);
        } else if (selectionSpan.start <= textSpan.start) {
            // if the selection covers the first part of the text-node, split
            // off the first part of the text-node, and apply the style to it
            int intersectionLength = selectionSpan.end - textSpan.start;
            String part1 = textNode.getSubstring(0, intersectionLength);
            String part2 = textNode.getSubstring(intersectionLength, characterCount);

            org.apache.pivot.wtk.text.Span newSpanNode = new org.apache.pivot.wtk.text.Span();
            newSpanNode.add(new TextNode(part1));
            styleApplicator.apply(newSpanNode);

            Element parent = textNode.getParent();
            int index = parent.remove(textNode);
            parent.insert(newSpanNode, index);
            parent.insert(new TextNode(part2), index + 1);
        } else if (selectionSpan.end >= textSpan.end) {
            // if the selection covers the last part of the text-node, split off
            // the last part of the text-node, and apply the style to
            // it
            int intersectionStart = selectionSpan.start - textSpan.start;
            String part1 = textNode.getSubstring(0, intersectionStart);
            String part2 = textNode.getSubstring(intersectionStart, characterCount);

            org.apache.pivot.wtk.text.Span newSpanNode = new org.apache.pivot.wtk.text.Span();
            newSpanNode.add(new TextNode(part2));
            styleApplicator.apply(newSpanNode);

            Element parent = textNode.getParent();
            int index = parent.remove(textNode);
            parent.insert(new TextNode(part1), index);
            parent.insert(newSpanNode, index + 1);
        } else {
            // if the selection covers an internal part of the text-node, split
            // the
            // text-node into 3 parts, and apply the style to the second part
            int part2Start = selectionSpan.start - textSpan.start;
            int part2End = selectionSpan.end - textSpan.start + 1;
            String part1 = textNode.getSubstring(0, part2Start);
            String part2 = textNode.getSubstring(part2Start, part2End);
            String part3 = textNode.getSubstring(part2End, characterCount);

            org.apache.pivot.wtk.text.Span newSpanNode = new org.apache.pivot.wtk.text.Span();
            newSpanNode.add(new TextNode(part2));
            styleApplicator.apply(newSpanNode);

            Element parent = textNode.getParent();
            int index = parent.remove(textNode);
            parent.insert(new TextNode(part1), index);
            parent.insert(newSpanNode, index + 1);
            parent.insert(new TextNode(part3), index + 2);
        }
    }

    private void applyStyleToSpanNode(org.apache.pivot.wtk.Span selectionSpan,
        StyleApplicator styleApplicator, org.apache.pivot.wtk.text.Span spanNode,
        int characterCount, org.apache.pivot.wtk.Span textSpan) {
        if (selectionSpan.contains(textSpan)) {
            // if the span-node is contained wholly inside the
            // selection, apply the style
            styleApplicator.apply(spanNode);
        } else if (selectionSpan.start <= textSpan.start) {
            // if the selection covers the first part of the span-node, split
            // off the first part of the span-node, and apply the style to it
            int intersectionLength = selectionSpan.end - textSpan.start;
            org.apache.pivot.wtk.text.Span node1 = spanNode.getRange(0, intersectionLength);
            styleApplicator.apply(node1);
            Node node2 = spanNode.getRange(intersectionLength, characterCount - intersectionLength);
            Element parent = spanNode.getParent();
            int index = parent.remove(spanNode);
            parent.insert(node1, index);
            parent.insert(node2, index + 1);
        } else if (selectionSpan.end >= textSpan.end) {
            // if the selection covers the last part of the span-node, split off
            // the last part of the span-node, and apply the style to it
            int intersectionStart = selectionSpan.start - textSpan.start;
            org.apache.pivot.wtk.text.Span part1 = spanNode.getRange(0, intersectionStart);
            org.apache.pivot.wtk.text.Span part2 = spanNode.getRange(intersectionStart,
                characterCount - intersectionStart);

            styleApplicator.apply(part2);

            Element parent = spanNode.getParent();
            int index = parent.remove(spanNode);
            parent.insert(part1, index);
            parent.insert(part2, index + 1);
        } else {
            // if the selection covers an internal part of the span-node, split
            // the
            // span-node into 3 parts, and apply the style to the second part
            int part2Start = selectionSpan.start - textSpan.start;
            int part2End = selectionSpan.end - textSpan.start;
            org.apache.pivot.wtk.text.Span part1 = spanNode.getRange(0, part2Start);
            org.apache.pivot.wtk.text.Span part2 = spanNode.getRange(part2Start, part2End
                - part2Start);
            org.apache.pivot.wtk.text.Span part3 = spanNode.getRange(part2End, characterCount
                - part2End);

            styleApplicator.apply(part2);

            Element parent = spanNode.getParent();
            int index = parent.remove(spanNode);
            parent.insert(part1, index);
            parent.insert(part2, index + 1);
            parent.insert(part3, index + 2);
        }
    }

    private void collectNodes(org.apache.pivot.wtk.text.Node node, List<Node> nodeList) {
        // don't worry about the text-nodes that are children of Span nodes.
        if (node instanceof org.apache.pivot.wtk.text.Span) {
            return;
        }
        if (node instanceof org.apache.pivot.wtk.text.Element) {
            Element element = (Element)node;
            for (Node child : element) {
                nodeList.add(child);
                collectNodes(child, nodeList);
            }
        }
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(RichTextEditorDemo.class, args);
    }
}
