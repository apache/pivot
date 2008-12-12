package pivot.wtk;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import pivot.collections.ArrayList;
import pivot.serialization.PlainTextSerializer;
import pivot.serialization.SerializationException;
import pivot.util.MIMEType;
import pivot.wtk.data.Manifest;

class RemoteManifest extends Manifest {
    private Transferable transferable;
    private ArrayList<DataFlavor> content;

    public RemoteManifest(Transferable transferable) {
        this.transferable = transferable;

        // Extract applicable content
        content = new ArrayList<DataFlavor>();

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
        for (int i = 0, n = transferDataFlavors.length; i < n; i++) {
            DataFlavor dataFlavor = transferDataFlavors[i];

            if (dataFlavor.getRepresentationClass() == InputStream.class) {
                System.out.println(content.getLength() + " " + dataFlavor.getMimeType());
                content.add(dataFlavor);
            }
        }
    }

    @Override
    public String getMIMEType(int index) {
        return content.get(index).getMimeType();
    }

    @Override
    public InputStream getInputStream(int index) throws IOException {
        InputStream inputStream = null;

        try {
            DataFlavor dataFlavor = content.get(index);
            Object transferData = transferable.getTransferData(dataFlavor);

            if (transferData instanceof InputStream) {
                inputStream = (InputStream)transferData;
            } else {
                // TODO This shouldn't be necessary. It may be due to a bug
                // in the JVM and requires further investigation.

                if (transferData instanceof Reader) {
                    // Read the data into a string buffer
                    Reader reader = (Reader)transferData;
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    StringBuilder stringBuilder = new StringBuilder();

                    String line = bufferedReader.readLine();
                    while (line != null) {
                        stringBuilder.append(line);
                        line = bufferedReader.readLine();
                    }

                    bufferedReader.close();

                    // Write the data to a memory buffer
                    String text = stringBuilder.toString();

                    MIMEType mimeType = MIMEType.decode(dataFlavor.getMimeType());
                    PlainTextSerializer serializer = new PlainTextSerializer(mimeType.get("charset"));
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    try {
                        serializer.writeObject(text, byteArrayOutputStream);
                    } catch(SerializationException exception) {
                        System.err.println(exception);
                    }

                    byteArrayOutputStream.close();

                    byte[] data = byteArrayOutputStream.toByteArray();
                    inputStream = new ByteArrayInputStream(data);
                }
            }
        } catch(UnsupportedFlavorException exception) {
            // No-op; shouldn't get here
        }

        return inputStream;
    }

    @Override
    public int getLength() {
        return content.getLength();
    }
}
