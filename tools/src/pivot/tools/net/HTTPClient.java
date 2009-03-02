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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import pivot.collections.Dictionary;
import pivot.serialization.SerializationException;
import pivot.serialization.Serializer;
import pivot.util.Vote;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.web.BasicAuthentication;
import pivot.web.DeleteQuery;
import pivot.web.GetQuery;
import pivot.web.PostQuery;
import pivot.web.PutQuery;
import pivot.web.Query;
import pivot.wtk.Action;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Checkbox;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.ListButton;
import pivot.wtk.ListButtonSelectionListener;
import pivot.wtk.PushButton;
import pivot.wtk.Sheet;
import pivot.wtk.SheetStateListener;
import pivot.wtk.TaskAdapter;
import pivot.wtk.TextArea;
import pivot.wtk.TextInput;
import pivot.wtk.Window;
import pivot.wtk.content.ListItem;
import pivot.wtkx.WTKXSerializer;

/**
 * HTTP client.
 *
 * @author tvolkert
 */
@SuppressWarnings("unchecked")
public class HTTPClient implements Application {
    /**
     * The supported protocols.
     *
     * @author tvolkert
     */
    private static enum Protocol {
        HTTP,
        HTTPS;

        public static Protocol decode(String value) {
            return valueOf(value.toUpperCase());
        }

        public boolean isSecure() {
            return (this == HTTPS);
        }
    }

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
     * Considers all SSL hostnames as valid (performs no actual verification).
     *
     * @author tvolkert
     */
    private static class LenientHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private WTKXSerializer serializer;
    private Window window;

    private boolean authenticate = false;
    private boolean lenientHostnameVerification = false;

    private LenientHostnameVerifier lenientHostnameVerifier = new LenientHostnameVerifier();

    /**
     * Gets the query to issue to the server, authenticated if needed.
     */
    private HTTPRequest getHTTPRequest() {
        ListButton protocolListButton = (ListButton)serializer.getObjectByName("request.protocol");
        ListItem protocolListItem = (ListItem)protocolListButton.getSelectedValue();
        Protocol protocol = Protocol.decode(protocolListItem.getText());
        boolean secure = protocol.isSecure();

        TextInput hostTextInput = (TextInput)serializer.getObjectByName("request.host");
        String host = hostTextInput.getText();

        TextInput portTextInput = (TextInput)serializer.getObjectByName("request.port");
        String portText = portTextInput.getText();
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (Exception ex) {
            port = secure ? 443 : 80;
        }

        TextInput pathTextInput = (TextInput)serializer.getObjectByName("request.path");
        String path = pathTextInput.getText();

        ListButton methodListButton = (ListButton)serializer.getObjectByName("request.method");
        ListItem methodListItem = (ListItem)methodListButton.getSelectedValue();
        Method method = Method.decode(methodListItem.getText());

        // Construct the HTTP request
        HTTPRequest httpRequest = new HTTPRequest(method.toString(), protocol.toString(), host, port, path);

        TextArea textArea = (TextArea)serializer.getObjectByName("request.body");
        String body = textArea.getText();
        httpRequest.setBody(body.getBytes());

        if (lenientHostnameVerification) {
            // Use a lenient hostname verifier to ensude that the request goes through
            httpRequest.setHostnameVerifier(lenientHostnameVerifier);
        }

        if (authenticate) {
            TextInput usernameInput = (TextInput)serializer.getObjectByName("request.username");
            TextInput passwordInput = (TextInput)serializer.getObjectByName("request.password");

            String username = usernameInput.getText();
            String password = passwordInput.getText();

            // TODO
        }

        return httpRequest;
    }

    // Application methods

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        new Action("toggleAuthenticationAction") {
            public String getDescription() {
                return "Toggles authentication of requests";
            }

            public void perform() {
                authenticate = !authenticate;

                Component component = (Component)serializer.getObjectByName("request.authentication");
                component.setDisplayable(authenticate);
            }
        };

        new Action("toggleHostnameVerificationAction") {
            public String getDescription() {
                return "Toggles lenient hostname verification";
            }

            public void perform() {
                lenientHostnameVerification = !lenientHostnameVerification;
            }
        };

        new Action("setKeystoreAction") {
            private String keystorePath = null;
            private String keystorePassword = null;

            public String getDescription() {
                return "Sets a trusted keystore";
            }

            public void perform() {
                final WTKXSerializer sheetSerializer = new WTKXSerializer();
                final Sheet sheet;

                try {
                    sheet = (Sheet)sheetSerializer.readObject("pivot/tools/net/setKeystore.wtkx");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                Button okButton = (Button)sheetSerializer.getObjectByName("okButton");
                okButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        sheet.close(true);
                    }
                });

                Button cancelButton = (Button)sheetSerializer.getObjectByName("cancelButton");
                cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        sheet.close(false);
                    }
                });

                if (keystorePath != null) {
                    TextInput pathTextInput = (TextInput)sheetSerializer.getObjectByName("path");
                    pathTextInput.setText(keystorePath);
                }

                if (keystorePassword != null) {
                    TextInput passwdTextInput = (TextInput)sheetSerializer.getObjectByName("passwd");
                    passwdTextInput.setText(keystorePassword);
                }

                sheet.getSheetStateListeners().add(new SheetStateListener() {
                    public Vote previewSheetClose(Sheet sheet, boolean result) {
                        Vote vote = Vote.APPROVE;

                        if (result) {
                            TextInput pathTextInput = (TextInput)sheetSerializer.getObjectByName("path");
                            TextInput passwdTextInput = (TextInput)sheetSerializer.getObjectByName("passwd");

                            keystorePath = pathTextInput.getText();
                            keystorePassword = passwdTextInput.getText();

                            File file = new File(keystorePath);
                            if (!file.exists()
                                || !file.isFile()) {
                                vote = Vote.DENY;
                            } else if (!file.canRead()) {
                                vote = Vote.DENY;
                            }
                        }

                        return vote;
                    }

                    public void sheetCloseVetoed(Sheet sheet, Vote reaso) {
                        // No-op
                    }

                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            System.setProperty("javax.net.ssl.trustStore", keystorePath);
                            System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
                            System.out.println("Set system properties");
                        }
                    }
                });

                sheet.open(window);
            }
        };

        // Load the main app window
        serializer = new WTKXSerializer();
        window = (Window)serializer.readObject("pivot/tools/net/application.wtkx");
        window.open(display);

        PushButton submitButton = (PushButton)serializer.getObjectByName("request.submit");
        submitButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(final Button button) {
                button.setEnabled(false);

                HTTPRequest httpRequest = getHTTPRequest();
                httpRequest.execute(new TaskAdapter<HTTPResponse>(new TaskListener<HTTPResponse>() {
                    public void taskExecuted(Task<HTTPResponse> task) {
                        button.setEnabled(true);
                        HTTPResponse httpResponse = task.getResult();

                        // TODO
                        System.out.println(httpResponse);
                        System.out.println(new String(httpResponse.getBody()));
                    }

                    public void executeFailed(Task<HTTPResponse> task) {
                        button.setEnabled(true);
                        task.getFault().printStackTrace();
                    }
                }));
            }
        });
    }

    public boolean shutdown(boolean optional) throws Exception {
        // No-op
        return true;
    }

    public void suspend() throws Exception {
        // No-op
    }

    public void resume() throws Exception {
        // No-op
    }
}
