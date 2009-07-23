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
package org.apache.pivot.tools.net;

import java.io.File;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Base64;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtkx.WTKXSerializer;

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
    public enum Protocol {
        HTTP,
        HTTPS;

        public boolean isSecure() {
            return (this == HTTPS);
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

    private Frame detailsFrame = null;

    private Credentials credentials = null;
    private boolean lenientHostnameVerification = false;

    private LenientHostnameVerifier lenientHostnameVerifier = new LenientHostnameVerifier();

    /**
     * Gets the query to issue to the server, authenticated if needed.
     */
    private Request getRequest() {
        ListButton protocolListButton = (ListButton)serializer.get("request.protocol");
        ListItem protocolListItem = (ListItem)protocolListButton.getSelectedItem();
        Protocol protocol = Protocol.valueOf(protocolListItem.getText().toUpperCase());
        boolean secure = protocol.isSecure();

        TextInput hostTextInput = (TextInput)serializer.get("request.host");
        String host = hostTextInput.getText();

        TextInput portTextInput = (TextInput)serializer.get("request.port");
        String portText = portTextInput.getText();
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (Exception ex) {
            port = secure ? 443 : 80;
        }

        TextInput pathTextInput = (TextInput)serializer.get("request.path");
        String path = pathTextInput.getText();

        ListButton methodListButton = (ListButton)serializer.get("request.method");
        ListItem methodListItem = (ListItem)methodListButton.getSelectedItem();

        // Construct the HTTP request
        Request httpRequest = new Request(methodListItem.getText(), protocol.toString(), host, port, path);

        TextArea textArea = (TextArea)serializer.get("request.body");
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

    public void startup(Display display, Map<String, String> properties) throws Exception {
        Action.getNamedActions().put("setAuthenticationAction", new Action() {
            public String getDescription() {
                return "Specifies authentication credentials";
            }

            public void perform() {
                final WTKXSerializer sheetSerializer = new WTKXSerializer();
                final Sheet sheet;

                try {
                    sheet = (Sheet)sheetSerializer.readObject(this, "setAuthentication.wtkx");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                Button okButton = (Button)sheetSerializer.get("okButton");
                okButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        sheet.close(true);
                    }
                });

                Button cancelButton = (Button)sheetSerializer.get("cancelButton");
                cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        sheet.close(false);
                    }
                });

                if (credentials != null) {
                    TextInput usernameTextInput = (TextInput)sheetSerializer.get("username");
                    TextInput passwordTextInput = (TextInput)sheetSerializer.get("password");
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
                                sheetSerializer.get("username");
                            TextInput passwordTextInput = (TextInput)
                                sheetSerializer.get("password");

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
        });

        Action.getNamedActions().put("toggleHostnameVerificationAction", new Action() {
            public String getDescription() {
                return "Toggles lenient hostname verification";
            }

            public void perform() {
                lenientHostnameVerification = !lenientHostnameVerification;
            }
        });

        Action.getNamedActions().put("setKeystoreAction", new Action() {
            private String keystorePath = null;
            private String keystorePassword = null;

            public String getDescription() {
                return "Sets a trusted keystore";
            }

            public void perform() {
                final WTKXSerializer sheetSerializer = new WTKXSerializer();
                final Sheet sheet;

                try {
                    sheet = (Sheet)sheetSerializer.readObject(this, "setKeystore.wtkx");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                Button okButton = (Button)sheetSerializer.get("okButton");
                okButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        sheet.close(true);
                    }
                });

                Button cancelButton = (Button)sheetSerializer.get("cancelButton");
                cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
                    public void buttonPressed(Button button) {
                        sheet.close(false);
                    }
                });

                if (keystorePath != null) {
                    TextInput pathTextInput = (TextInput)sheetSerializer.get("path");
                    pathTextInput.setText(keystorePath);
                }

                if (keystorePassword != null) {
                    TextInput passwdTextInput = (TextInput)sheetSerializer.get("passwd");
                    passwdTextInput.setText(keystorePassword);
                }

                sheet.getSheetStateListeners().add(new SheetStateListener() {
                    public Vote previewSheetClose(Sheet sheet, boolean result) {
                        Vote vote = Vote.APPROVE;

                        if (result) {
                            TextInput pathTextInput = (TextInput)sheetSerializer.get("path");
                            TextInput passwdTextInput = (TextInput)sheetSerializer.get("passwd");

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
        });

        // Load the main app window
        serializer = new WTKXSerializer();
        window = (Window)serializer.readObject(this, "application.wtkx");
        window.open(display);

        TableView tableView = (TableView)serializer.get("log.tableView");
        tableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                boolean consumed = false;

                if (button == Mouse.Button.LEFT && count == 2) {
                    consumed = true;

                    if (detailsFrame == null) {
                        final WTKXSerializer frameSerializer = new WTKXSerializer();

                        try {
                            detailsFrame = (Frame)frameSerializer.readObject
                                (this, "detailsFrame.wtkx");
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    detailsFrame.open(window);
                }

                return consumed;
            }
        });

        PushButton submitButton = (PushButton)serializer.get("request.submit");
        submitButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(final Button button) {
                button.setEnabled(false);

                Request httpRequest = getRequest();
                httpRequest.execute(new TaskAdapter<Response>(new TaskListener<Response>() {
                    public void taskExecuted(Task<Response> task) {
                        button.setEnabled(true);
                        Response httpResponse = task.getResult();
                        Transaction transaction = new Transaction((Request)task, httpResponse);

                        TableView tableView = (TableView)serializer.get("log.tableView");
                        List<Transaction> tableData = (List<Transaction>)tableView.getTableData();
                        tableData.add(transaction);
                    }

                    public void executeFailed(Task<Response> task) {
                        button.setEnabled(true);
                        task.getFault().printStackTrace();
                    }
                }));
            }
        });
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() throws Exception {
        // No-op
    }

    public void resume() throws Exception {
        // No-op
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(HTTPClient.class, args);
    }
}
