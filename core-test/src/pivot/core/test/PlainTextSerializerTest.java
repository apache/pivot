package pivot.core.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import pivot.serialization.PlainTextSerializer;

public class PlainTextSerializerTest {
    public static void main(String[] args) {
        try {
            String text = "Hello, World!";

            PlainTextSerializer serializer = new PlainTextSerializer();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            serializer.writeObject(text, byteArrayOutputStream);
            byteArrayOutputStream.close();

            byte[] data = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            text = (String)serializer.readObject(byteArrayInputStream);

            System.out.println(text);
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }
}
