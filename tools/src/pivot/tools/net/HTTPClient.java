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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.util.Base64;
import pivot.util.Vote;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.wtk.Action;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Display;
import pivot.wtk.ListButton;
import pivot.wtk.PushButton;
import pivot.wtk.Sheet;
import pivot.wtk.SheetStateListener;
import pivot.wtk.TableView;
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

    private static class Credentials {
        private String username;
        private String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
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

    private Credentials credentials = null;
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
            // Use a lenient hostname verifier to ensure that the request goes through
            httpRequest.setHostnameVerifier(lenientHostnameVerifier);
        }

        if (credentials != null) {
            String token = credentials.getUsername() + ":" + credentials.getPassword();
            String encodedToken = Base64.encode(token.getBytes());
            httpRequest.getRequestHeaders().put("Authorization", "Basic " + encodedToken);
        }

        return httpRequest;
    }

    // Application methods

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        new Action("setAuthenticationAction") {
            public String getDescription() {
                return "Specifies authentication credentials";
            }

            public void perform() {
                final WTKXSerializer sheetSerializer = new WTKXSerializer();
                final Sheet sheet;

                try {
                    sheet = (Sheet)sheetSerializer.readObject("pivot/tools/net/setAuthentication.wtkx");
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

                if (credentials != null) {
                    TextInput usernameTextInput = (TextInput)sheetSerializer.getObjectByName("username");
                    TextInput passwordTextInput = (TextInput)sheetSerializer.getObjectByName("password");
                    usernameTextInput.setText(credentials.getUsername());
                    passwordTextInput.setText(credentials.getPassword());
                }

                sheet.getSheetStateListeners().add(new SheetStateListener() {
                    public Vote previewSheetClose(Sheet sheet, boolean result) {
                        return Vote.APPROVE;
                    }

                    public void sheetCloseVetoed(Sheet sheet, Vote reaso) {
                        // No-op
                    }

                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            TextInput usernameTextInput = (TextInput)
                                sheetSerializer.getObjectByName("username");
                            TextInput passwordTextInput = (TextInput)
                                sheetSerializer.getObjectByName("password");

                            String username = usernameTextInput.getText();
                            String password = passwordTextInput.getText();

                            if (username.length() == 0 && password.length() == 0) {
                                credentials = null;
                            } else {
                                credentials = new Credentials(username, password);
                            }
                        }
                    }
                });

                sheet.open(window);
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
                        Transaction transaction = new Transaction((HTTPRequest)task, httpResponse);

                        TableView tableView = (TableView)serializer.getObjectByName("log.tableView");
                        List<Transaction> tableData = (List<Transaction>)tableView.getTableData();
                        tableData.add(transaction);
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
