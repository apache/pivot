package pivot.wtk.text.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

public class DocumentFragmentTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            Element root = document.createElement("root");
            document.appendChild(root);

            DocumentFragment documentFragment = document.createDocumentFragment();
            documentFragment.appendChild(document.createElement("test"));
            documentFragment.appendChild(document.createElement("test"));
            documentFragment.appendChild(document.createElement("test"));

            System.out.println(documentFragment.getChildNodes().getLength());

            root.appendChild(documentFragment);
            System.out.println(documentFragment.getChildNodes().getLength());
            System.out.println(root.getChildNodes().getLength());
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }
}
