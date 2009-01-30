package pivot.wtk.text.test;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import pivot.wtk.text.PlainTextSerializer;

public class JTextAreaTest {
    public static void main(String[] args) {
        PlainTextSerializer serializer = new PlainTextSerializer("UTF-8");
        InputStream inputStream = PlainTextSerializerTest.class.getResourceAsStream("pivot.txt");

        String text = null;
        try {
            StringWriter writer = new StringWriter();
            serializer.writeObject(serializer.readObject(inputStream), writer);
            text = writer.toString();
        } catch(Exception exception) {
            System.out.println(exception);
        }

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        // scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(320, 240));

        textArea.setText(text);

        JFrame frame = new JFrame();
        frame.add(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }
}
