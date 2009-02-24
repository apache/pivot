/*
 * Copyright (c) 2008 VMware, Inc.
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
package pivot.tools.net;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import javax.swing.JTextArea;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.collections.immutable.ImmutableMap;
import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;
import pivot.util.ListenerList;
import pivot.util.Resources;
import pivot.util.Vote;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskGroup;
import pivot.util.concurrent.TaskListener;
import pivot.web.BasicAuthentication;
import pivot.web.DeleteQuery;
import pivot.web.GetQuery;
import pivot.web.PostQuery;
import pivot.web.PutQuery;
import pivot.web.Query;
import pivot.wtk.Action;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.CardPane;
import pivot.wtk.Checkbox;
import pivot.wtk.Component;
import pivot.wtk.Dialog;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.MessageType;
import pivot.wtk.Meter;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.Sheet;
import pivot.wtk.SheetCloseListener;
import pivot.wtk.TaskAdapter;
import pivot.wtk.TextInput;
import pivot.wtk.Window;
import pivot.wtk.effects.SaturationDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;
import pivot.wtkx.WTKXSerializer;

/**
 *
 *
 * @author tvolkert
 */
@SuppressWarnings("unchecked")
public class HTTPClient implements Application {
    /**
     * The supported HTTP methods.
     *
     * @author tvolkert
     */
    private static enum Method {
        GET,
        POST,
        PUT,
        DELETE;

        public static Method decode(String value) {
            return valueOf(value.toUpperCase());
        }

        public boolean hasBody() {
            return (this == POST || this == PUT);
        }
    }

    /**
     * Serializes HTTP requests and responses to strings.
     *
     * @author tvolkert
     */
    private static class StringSerializer implements Serializer {
        public Object readObject(InputStream inputStream)
            throws IOException, SerializationException {
            StringBuilder stringBuilder = new StringBuilder();

            InputStreamReader reader = new InputStreamReader(inputStream);
            char[] characterBuffer = new char[1024];
            int charCount = 0;

            while ((charCount = reader.read(characterBuffer)) != -1) {
                stringBuilder.append(characterBuffer, 0, charCount);
            }

            System.out.println("IN  --> " + stringBuilder.toString());
            return stringBuilder.toString();
        }

        public void writeObject(Object object, OutputStream outputStream)
            throws IOException, SerializationException {
            String string = (String)object;
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            System.out.println("OUT --> " + string);
            writer.write(string);
            writer.flush();
        }

        public String getMIMEType(Object object) {
            return "text/plain";
        }
    }

    private WTKXSerializer serializer;
    private Window window;

    private static StringSerializer stringSerializer = new StringSerializer();

    private BasicAuthentication getAuthentication() {
        TextInput usernameInput = (TextInput)serializer.getObjectByName("authentication.username");
        TextInput passwordInput = (TextInput)serializer.getObjectByName("authentication.password");
        return new BasicAuthentication(usernameInput.getText(), passwordInput.getText());
    }

    private Query<?> getQuery() {
        Button.Group methods = Button.getGroup("method");
        Button selectedMethod = methods.getSelection();
        Method method = Method.decode((String)selectedMethod.getButtonData());

        TextInput hostInput = (TextInput)serializer.getObjectByName("server.host");
        TextInput portInput = (TextInput)serializer.getObjectByName("server.port");
        Checkbox secureCheckbox = (Checkbox)serializer.getObjectByName("server.secure");

        String host = hostInput.getText();
        int port = Integer.parseInt(portInput.getText());
        boolean secure = secureCheckbox.isSelected();

        TextInput pathInput = (TextInput)serializer.getObjectByName("request.path");
        // TODO Arguments
        String path = pathInput.getText();

        JTextArea textArea = (JTextArea)serializer.getObjectByName("request.bodyText");
        String body = textArea.getText();

        Query<?> query = null;

        switch (method) {
        case GET:
            query = new GetQuery(host, port, path, secure);
            break;

        case POST:
            query = new PostQuery(host, port, path, secure);
            ((PostQuery)query).setValue(body);
            break;

        case PUT:
            query = new PutQuery(host, port, path, secure);
            ((PutQuery)query).setValue(body);
            break;

        case DELETE:
            query = new DeleteQuery(host, port, path, secure);
            break;
        }
        query.setSerializer(stringSerializer);

        return query;
    }

    // Application methods

    @Override
    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "/mts-cm/home/tvolkert/project/sci/etc/sci.keystore"); 
        System.setProperty("javax.net.ssl.keyStorePassword", "bigbird1"); 

        // Load the main app window
        serializer = new WTKXSerializer();
        window = (Window)serializer.readObject("com/sci/test/application.wtkx");
        window.open(display);

        /*
        final SwingAdapter body = (SwingAdapter)serializer.getObjectByName("request.body");
        final Button.Group methods = Button.getGroup("method");
        methods.getGroupListeners().add(new Button.GroupListener() {
            @Override
            public void selectionChanged(Button.Group group, Button previousSelection) {
                Button selection = group.getSelection();
                Method method = Method.decode((String)selection.getButtonData());
                body.setDisplayable(method.hasBody());
            }
        });

        PushButton submitButton = (PushButton)serializer.getObjectByName("submit");
        submitButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(final Button button) {
                button.setEnabled(false);

                Query<Object> query = (Query<Object>)getQuery();
                //getAuthentication().authenticate(query);

                query.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
                    @Override
                    public void taskExecuted(Task<Object> task) {
                        button.setEnabled(true);
                        Object result = task.getResult();

                        // TODO
                        System.out.println(result);
                    }

                    @Override
                    public void executeFailed(Task<Object> task) {
                        button.setEnabled(true);
                        task.getFault().printStackTrace();
                    }
                }));
            }
        });
        */
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        // No-op
        return true;
    }

    @Override
    public void suspend() throws Exception {
        // No-op
    }

    @Override
    public void resume() throws Exception {
        // No-op
    }
}
