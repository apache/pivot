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
package org.apache.pivot.wtk;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.content.LinkButtonDataRenderer;

/**
 * Specialized subclass of {@link LinkButton} that actually implements an HTML hyperlink
 * using the {@link Desktop#browse} method.
 * <p> By default the button text will be the string representation of the {@link URI} or
 * {@link URL} that is the target of the link.  But any arbitrary text can also be used
 * or a combination of icon and text using {@link ButtonData} (just like any other button).
 */
@DefaultProperty("buttonData")
public class HyperlinkButton extends LinkButton {
    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new LinkButtonDataRenderer();

    /**
     * Private class to implement the "browse" action of this button.
     * <p> Uses the {@link Desktop#browse} method to implement the functionality.
     */
    private class URIAction extends Action {
        private URI uri;

        public URIAction(final URI uri) {
            this.uri = uri;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(final URI uri) {
            this.uri = uri;
        }

        @Override
        public void perform(final Component source) {
            if (uri == null) {
                throw new RuntimeException("URI is null.");
            }
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    // TODO: Should there be an exception here instead of silent failure?
                    // Maybe an exception at constructor time (basically the whole idea
                    // is unsupported)
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(uri);
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    private URIAction action;

    /**
     * Default no-arg constructor needed for BXML files.
     * <p> Sets the hyperlink text to <code>""</code> (empty string).
     * and the {@link URI} target to null.
     */
    public HyperlinkButton() {
        this((URI) null);
    }

    /**
     * Construct a hyperlink button that references the given {@link URI}.
     * @param uri The target of this hyperlink.
     */
    public HyperlinkButton(final URI uri) {
        super();
        setUri(uri);

        installSkin(HyperlinkButton.class);
        setDataRenderer(DEFAULT_DATA_RENDERER);
    }

    /**
     * Construct a hyperlink button that references the given URI specified
     * by the string argument.
     * @param uriString The target of this hyperlink given as a string.
     * @throws URISyntaxException if the string argument is not a proper URI.
     */
    public HyperlinkButton(final String uriString) throws URISyntaxException {
        this(uriString == null ? null : new URI(uriString));
    }

    /**
     * Construct a hyperlink button that references the given {@link URL}.
     * @param url The target of this hyperlink.
     * @throws URISyntaxException if the URL does not specify a valid {@link URI}.
     */
    public HyperlinkButton(final URL url) throws URISyntaxException {
        this(url == null ? null : url.toURI());
    }

    /**
     * Constructor to set an alternate text for the link button, in addition
     * to the target {@link URI}.
     * @param text Alternate text for the hyperlink.
     * @param uri The target of this hyperlink.
     */
    public HyperlinkButton(final String text, final URI uri) {
        this(uri);
        setButtonData(text);
    }

    /**
     * Constructor to set an alternate text for the link button, in addition to
     * specifying the target location with a string.
     * @param text The alternate text for the hyperlink.
     * @param uriString The string specifying the hyperlink target.
     * @throws URISyntaxException if the string does not specify a valid {@link URI}.
     */
    public HyperlinkButton(final String text, final String uriString) throws URISyntaxException {
        this(text, new URI(uriString));
    }

    /**
     * Access the {@link URI} which is the target of this hyperlink.
     * @return The target for this hyperlink.
     */
    public URI getUri() {
        return action == null ? null : action.getUri();
    }

    /**
     * Set the {@link URI} which is the target of this hyperlink.
     * <p> Also sets the button data (that is the text string for the link)
     * to the string form of the <code>URI</code>, but only if there is not
     * already something set for the button data.  This logic allows the
     * button data to be set as an embedded element in BXML and still have
     * the URI set as an element property.
     * @param uri The target for this hyperlink.
     */
    public void setUri(final URI uri) {
        // If the user has already set the button data in BXML,
        // then don't set the data to this new URI string
        Object buttonData = getButtonData();
        if (Utils.isNullOrEmpty(buttonData)) {
            String uriString = uri == null ? "" : uri.toString();
            setButtonData(uriString);
        }
        if (action == null) {
            action = new URIAction(uri);
            setAction(action);
        } else {
            action.setUri(uri);
        }
    }

    /**
     * Set the {@link URI} target of this hyperlink via the string representation.
     * @param uriString String value of the hyperlink target.
     * @throws IllegalArgumentException if the string is <code>null</code> or empty.
     * @throws URISyntaxException if the string is not a valid {@link URI}.
     * @see #setUri(URI)
     */
    public void setUri(final String uriString) throws URISyntaxException {
        Utils.checkNullOrEmpty(uriString, "URI string");
        setUri(new URI(uriString));
    }

    /**
     * Set the target of this hyperlink via a {@link URL}.
     * @param url Target of this hyperlink.
     * @throws URISyntaxException if the <code>URL</code> does not specify
     * a valid {@link URI}.
     */
    public void setUrl(final URL url) throws URISyntaxException {
        setUri(url == null ? null : url.toURI());
    }

    /**
     * Set the {@link URL} target of this hyperlink using the string representation
     * of the <code>URL</code>.
     * @param urlString String form of the hyperlink target.
     * @throws IllegalArgumentException if the input string is <code>null</code> or empty.
     * @throws MalformedURLException if the string cannot be converted to a <code>URL</code>
     * @throws URISyntaxException if the <code>URL</code> does not specify a valid {@link URI}.
     */
    public void setUrl(final String urlString) throws MalformedURLException, URISyntaxException {
        Utils.checkNullOrEmpty(urlString, "URL string");
        setUrl(new URL(urlString));
    }

}

