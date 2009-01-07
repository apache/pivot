package pivot.wtk.text.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import pivot.wtk.text.BlockElement;
import pivot.wtk.text.Document;
import pivot.wtk.text.Element;
import pivot.wtk.text.Node;
import pivot.wtk.text.Paragraph;
import pivot.wtk.text.PlainTextSerializer;
import pivot.wtk.text.TextNode;

public class PlainTextSerializerTest {
    public static void main(String[] args) {
        try {
            Document document = new Document();
            document.add(new Paragraph("Hello, World!"));
            document.add(new Paragraph("ABC"));
            document.add(new Paragraph("123"));

            PlainTextSerializer serializer = new PlainTextSerializer();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            serializer.writeObject(document, byteArrayOutputStream);
            byteArrayOutputStream.close();

            byte[] data = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            document = (Document)serializer.readObject(byteArrayInputStream);

            dump(document);
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }

    public static void dump(Element element) {
        for (Node node : element) {
            if (node instanceof Element) {
                Element childElement = (Element)node;
                dump(childElement);

                if (childElement instanceof BlockElement) {
                    System.out.print('\n');
                }
            } else {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode)node;
                    System.out.print(textNode.getText());
                }
            }
        }
    }
}
