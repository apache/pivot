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
package org.apache.pivot.web;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.HexUtils;
import org.apache.pivot.util.MD5;
import org.apache.pivot.util.concurrent.TaskExecutionException;

/**
 * Implementation of the {@link Authentication} interface supporting the
 * HTTP <a href="http://tools.ietf.org/rfc/rfc2617.txt">Digest Authentication</a> scheme.
 * <br/>
 * Portions of code here are taken from Apache Tomcat, and from Apache Commons HTTP Client.
 *
 * TODO:
 *   - verify how to reuse the authorization data (if already authenticated) ...
 *   - verify if/how to handle the nonce count, for queries after the first ...
 *
 *   - future work:
 *     -- verify if this works with redirects, proxy, etc ...
 *     -- verify if implement also the algorithm "MD5-sess"
 *
 * @see DigestAuthenticator and related classes from Tomcat 6 sources
 * @see DigestScheme and related classes from HTTPCommons 3.1 sources
 */
public class DigestAuthentication implements Authentication {
    private static final String HTTP_RESPONSE_AUTHENTICATE_HEADER_KEY = "WWW-Authenticate";
    private static final String HTTP_REPLY_AUTHENTICATE_HEADER_KEY = "Authorization";
    // private static final String HTTP_RESPONSE_AUTHENTICATED_HEADER_KEY = "Authentication-Info";
    // private static final String HTTP_RESPONSE_MIME_TYPE_HEADER_KEY = "Content-Type";

    private static final String HTTP_REPLY_FIELD_SEPARATOR = ":";

    private static final String AUTH_FIELD_KEY_USERNAME = "username";
    private static final String AUTH_FIELD_KEY_REALM = "realm";
    private static final String AUTH_FIELD_KEY_RESPONSE = "response";
    private static final String AUTH_FIELD_KEY_URI = "uri";
    private static final String AUTH_FIELD_KEY_CNONCE = "cnonce";
    private static final String AUTH_FIELD_KEY_NC = "nc";
    private static final String AUTH_FIELD_KEY_NONCE = "nonce";
    private static final String AUTH_FIELD_KEY_DOMAIN = "domain";
    private static final String AUTH_FIELD_KEY_ALGORITHM = "algorithm";
    private static final String AUTH_FIELD_KEY_QOP = "qop";

    private static final String AUTH_FIELD_VALUE_QOP_AUTH = "auth";
    private static final String AUTH_FIELD_VALUE_ALGORITHM_AUTH_MD5 = "MD5";
    // private static final String AUTH_FIELD_VALUE_ALGORITHM_AUTH_MD5_SESSION = "MD5-sess";

    private static final String AUTH_FIELD_VALUE_NC_FIRST = "00000001";

    /** The Digest String constant, used in many places (from the standard). */
    protected static final String DIGEST_KEY = "Digest ";

    /**
     * The default message digest algorithm to use if we cannot use the
     * requested one.
     */
    protected static final String DEFAULT_ALGORITHM = "MD5";

    /** The default private key. */
    protected static final String PRIVATE_KEY = "Pivot";

    private static final String EMPTY_STRING = "";

    /** The username */
    private String username;

    /** The password */
    private String password;

    /**
     * The message digest algorithm to be used when generating session
     * identifiers. This must be an algorithm supported by the
     * <code>java.security.MessageDigest</code> class on your platform.
     */
    protected String algorithm = DEFAULT_ALGORITHM;

    /** The (digest) encoding charset */
    private String encoding;

    /**
     * Default constructor.
     *
     * @deprecated Do not use. Null user name or Null Password are no longer
     * allowed.
     */
    @Deprecated
    public DigestAuthentication() {
        this(null, null, null, null);
    }

    /**
     * The constructor with the username and password arguments.
     *
     * @param username the user name
     * @param password the password
     */
    public DigestAuthentication(String username, String password) {
        this(username, password, DEFAULT_ALGORITHM, null);
    }

    /**
     * The constructor with the username, password, and other invariant
     * arguments.
     *
     * @param username the user name
     * @param password the password
     * @param algorithm the algorithm to use (if null, the default will be used)
     * @param encoding the encoding for Strings (for digesting strings)
     */
    public DigestAuthentication(String username, String password, String algorithm, String encoding) {
        super();

        if (username == null) {
            throw new IllegalArgumentException("Username may not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password may not be null");
        }

        this.username = username;
        this.password = password;

        this.algorithm = algorithm;

        setDigestEncoding(encoding);
    }

    /**
     * Return the User Name property.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return the Password property.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Return the message digest algorithm for this Manager.
     *
     * @return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Get this object string.
     *
     * @return main data in a formed string
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append(this.getClass().getName());
        result.append(HTTP_REPLY_FIELD_SEPARATOR);
        result.append(username);
        result.append(HTTP_REPLY_FIELD_SEPARATOR);
        result.append(password);
        result.append(HTTP_REPLY_FIELD_SEPARATOR);
        result.append(algorithm);
        result.append(HTTP_REPLY_FIELD_SEPARATOR);
        result.append(encoding);

        return result.toString();
    }

    /**
     * Returns the (digest) encoding charset.
     *
     * @return The charset (may be null) for platform default
     */
    public String getDigestEncoding() {
        return encoding;
    }

    /**
     * Set the (digest) encoding to use.
     *
     * @param encoding The encoding to use, or if null a default will be used
     */
    private void setDigestEncoding(String encoding) {
        // this.encoding = encoding;

        // String charset = getParameter("charset"); // future: read the charset from the response header
        String charset = encoding;

        // if a charset is not specified, default to ISO-8859-1
        // if (charset == null)
        // charset = "ISO-8859-1";

        this.encoding = charset;
        // System.out.println("charset = \"" + charset + "\"");
    }

    /**
     * Returns the IP Address of the given Host.
     *
     * @param hostName the host name, or if null the local host will be used
     * @return the ID Address, as a String
     */
    protected static String getIPAddress(String hostName) throws UnknownHostException {
        InetAddress inetAddr = null;

        if (hostName == null || hostName.length() < 1) {
            inetAddr = InetAddress.getByName(hostName);
        }
        else {
            inetAddr = InetAddress.getLocalHost();
        }

        byte[] ipAddr = inetAddr.getAddress();

        StringBuffer ipAddressAsString = new StringBuffer();
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddressAsString.append(".");
            }

            ipAddressAsString.append(ipAddr[i] & 0xFF);
        }

        return ipAddressAsString.toString();
    }

    /**
     * Authenticate
     *
     * @param query The Query
     */
    public void authenticate(Query<?> query) {

        try {
            String responseAuthenticateHeader = query.getResponseHeaders().get(
                HTTP_RESPONSE_AUTHENTICATE_HEADER_KEY);
// System.out.println("responseAuthenticateHeader " +
// HTTP_RESPONSE_AUTHENTICATE_HEADER_KEY + " = \n" +
// responseAuthenticateHeader + "\n");
            if (responseAuthenticateHeader == null) {
                // Digest authentication needs a first query to retrieve
                // authentication hints from the server,
                // then retry filling the missing values, so here i make the
                // first query call here ...
                try {
                    query.execute();
                } catch (TaskExecutionException tee) {
                    // tee.printStackTrace(); // expected, so ignore it
                }

                responseAuthenticateHeader = query.getResponseHeaders().get(
                    HTTP_RESPONSE_AUTHENTICATE_HEADER_KEY);
// System.out.println("responseAuthenticateHeader " +
// HTTP_RESPONSE_AUTHENTICATE_HEADER_KEY + " = \n" +
// responseAuthenticateHeader + "\n");
                if (responseAuthenticateHeader == null) {
                    return;
                }
            } else if (!responseAuthenticateHeader.startsWith(DIGEST_KEY)) {
                throw new RuntimeException(
                    "authentication header not for this authentication method");
            } else {
                // TODO: (maybe for the future)
                // authentication data already present:
                // try to reuse it, reading hints from
                // HTTP_RESPONSE_AUTHENTICATED_HEADER_KEY
                // -- for example, the nonce_count value to use should be read from there ...
                ;
            }

            String uri = query.getPath();
            String method = query.getMethod().toString();

            Map<String, String> responseAuthenticateHeaderMap = DigestAuthentication.splitAuthenticationHeader(responseAuthenticateHeader);

            // nOnce: clients must only read this value
            String nOnce = responseAuthenticateHeaderMap.get(AUTH_FIELD_KEY_NONCE);
            if (nOnce == null) {
                nOnce = "";
            }

            String realmName = responseAuthenticateHeaderMap.get(AUTH_FIELD_KEY_REALM);
            if (realmName == null) {
                realmName = "";
            }

            String algorithm = responseAuthenticateHeaderMap.get(AUTH_FIELD_KEY_ALGORITHM);
            if (algorithm == null || algorithm.length() < 1) {
                algorithm = AUTH_FIELD_VALUE_ALGORITHM_AUTH_MD5;
            }

            String qop = responseAuthenticateHeaderMap.get(AUTH_FIELD_KEY_QOP);
            String cnonce = responseAuthenticateHeaderMap.get(AUTH_FIELD_KEY_CNONCE);
            String nonce_count = responseAuthenticateHeaderMap.get(AUTH_FIELD_KEY_NC);
            // rule from RFC
            if (qop == null || qop.length() < 1) {
                cnonce = ""; // safer empty value
                nonce_count = ""; // = AUTH_FIELD_VALUE_NC_FIRST;
            } else {
                cnonce = generateRandomValue();
                nonce_count = AUTH_FIELD_VALUE_NC_FIRST;
                // increment the (hex) value by 1
                // or read from the response, and store for later queries ...
                // -- see upper ...
            }

            String opaque = responseAuthenticateHeaderMap.get("opaque");
            String response = calculateResponse(username, realmName,
                nOnce, nonce_count, cnonce, qop, method, uri);

            StringBuffer authenticateHeader = new StringBuffer(512);
            authenticateHeader.append(DIGEST_KEY);

            authenticateHeader.append("username=\"");
            authenticateHeader.append(username);
            authenticateHeader.append("\"");
            authenticateHeader.append(", realm=\"");
            authenticateHeader.append(realmName);
            authenticateHeader.append("\"");
            authenticateHeader.append(", nonce=\"");
            authenticateHeader.append(nOnce);
            authenticateHeader.append("\"");
            authenticateHeader.append(", uri=\"");
            authenticateHeader.append(uri);
            authenticateHeader.append("\"");
            authenticateHeader.append(", response=\"");
            authenticateHeader.append(response);
            authenticateHeader.append("\"");

            if (qop != null && qop.length() > 0) {
                authenticateHeader.append(", qop=");
                authenticateHeader.append(qop);
                authenticateHeader.append(", nc=");
                authenticateHeader.append(nonce_count);
                authenticateHeader.append(", cnonce=\"");
                authenticateHeader.append(cnonce);
                authenticateHeader.append("\"");
            }

            authenticateHeader.append(", algorithm=");
            authenticateHeader.append(algorithm);

            if (opaque != null && opaque.length() > 0) {
                authenticateHeader.append(", opaque=\"");
                authenticateHeader.append(opaque);
                authenticateHeader.append("\"");
            }

// System.out.println("authenticateHeader " + HTTP_REPLY_AUTHENTICATE_HEADER_KEY +
// " = \n" + authenticateHeader.toString() + "\n");

            query.getRequestHeaders().put(HTTP_REPLY_AUTHENTICATE_HEADER_KEY,
                authenticateHeader.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Return the digested response (only this field) value to put in the reply
     * authentication header, as described in RFC 2069; otherwise return
     * <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param realm Realm name
     * @param nOnce Unique (or supposedly unique) token which has been used for
     * this request
     * @param nc Number of query (starting from 1) with this authentication info
     * @param cnonce Value (usually random) chosen by the client
     * @param qop Quality Of Protection flag
     * @param method The HTTP method
     * @param uri The URI of the query
     * @return the digested value for the response field
     */
    private String calculateResponse(final String username, final String realm, final String nOnce,
        final String nc, final String cnonce, final String qop, final String method,
        final String uri) {
        String response = null;

        String md5a1 = getDigestUsernameAndRealm(getAlgorithm(), username, realm);
        // System.out.println("md5a1 = \"" + md5a1 + "\"");
        if (md5a1 == null) {
            return null;
        }

        String md5a2 = getDigestMethodAndUri(qop, method, uri);
        // System.out.println("md5a2 = \"" + md5a2 + "\"");
        if (md5a2 == null) {
            return null;
        }

        StringBuffer sbCompleteValue = new StringBuffer(512);
        sbCompleteValue.append(md5a1);

        sbCompleteValue.append(HTTP_REPLY_FIELD_SEPARATOR);
        sbCompleteValue.append((nOnce != null) ? nOnce : "");

        sbCompleteValue.append(HTTP_REPLY_FIELD_SEPARATOR);
        sbCompleteValue.append((nc != null) ? nc : "");

        sbCompleteValue.append(HTTP_REPLY_FIELD_SEPARATOR);
        sbCompleteValue.append((cnonce != null) ? cnonce : "");

        sbCompleteValue.append(HTTP_REPLY_FIELD_SEPARATOR);
        sbCompleteValue.append((qop != null) ? qop : "");

        sbCompleteValue.append(HTTP_REPLY_FIELD_SEPARATOR);
        sbCompleteValue.append(md5a2);

        String a3 = sbCompleteValue.toString();
// System.out.println("a3 = \"" + a3 + "\"");
        // byte[] digestBytes = MD5.digest(a3, getDigestEncoding());
        // String md5a3 = MD5.encode(digestBytes);
        String md5a3 = MD5.digestAsString(a3, encoding);
// System.out.println("md5a3 = \"" + md5a3 + "\"");

        response = md5a3;
        return response;
    }

    /**
     * Removes the quotes on a string. RFC2617 states quotes are optional for
     * all parameters except realm.
     *
     * @param quotedString The string with enclosing quotes
     * @param quotesRequired If quotes are required on the previous parameter
     */
    protected static String removeQuotes(final String quotedString, boolean quotesRequired) {
        // support both quoted and non-quoted
        if (quotedString.length() > 0 && quotedString.charAt(0) != '"' && !quotesRequired) {
            return quotedString;
        } else if (quotedString.length() > 2) {
            return quotedString.substring(1, quotedString.length() - 1);
        } else {
            return DigestAuthentication.EMPTY_STRING;
        }
    }

    /**
     * Removes the quotes on a string.
     *
     * @param quotedString The string with enclosing quotes
     */
    protected static String removeQuotes(String quotedString) {
        return removeQuotes(quotedString, false);
    }

    /**
     * Utility method that returns in a Map all the fields inside the given
     * Authentication Header (returned from the Server after a first query
     * without authentication or without successful authentication). Note that
     * some of these info are required to construct other fields for the
     * following reply.
     *
     * @param authorizationHeader the authorization info returned from the
     * Server.
     * @return a Map of key / value, both Strings
     */
    protected static Map<String, String> splitAuthenticationHeader(String authorizationHeader) {
// System.out.println("Authorization header: " + authorizationHeader);
        Map<String, String> map = new HashMap<String, String>();

        // Validate the authorization credentials format
        if (authorizationHeader == null) {
            return map;
        }
        else if (!authorizationHeader.startsWith(DIGEST_KEY)) {
            return map;
        }

        authorizationHeader = authorizationHeader.substring(DIGEST_KEY.length()).trim();

        String[] tokens = authorizationHeader.split(",(?=(?:[^\"]*\"[^\"]*\")+$)");

        String userName = null;
        String realmName = null;
        String nOnce = null;
        String uri = null;
        String domain = null;
        String response = null;
        String nc = null;
        String cNonce = null;
        String qop = null;

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];
            if (currentToken.length() == 0) {
                continue;
            }

            int equalSign = currentToken.indexOf('=');
            if (equalSign < 0) {
                return null;
            }

            String currentTokenName = currentToken.substring(0, equalSign).trim();
            String currentTokenValue = currentToken.substring(equalSign + 1).trim();
// System.out.println("tokens[" + i + "]: currentTokenName = \"" + currentTokenName
// + "\", currentTokenValue = \"" + currentTokenValue + "\"");

            if (AUTH_FIELD_KEY_USERNAME.equals(currentTokenName)) {
                userName = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_USERNAME, userName);
            } else if (AUTH_FIELD_KEY_REALM.equals(currentTokenName)) {
                realmName = removeQuotes(currentTokenValue, true);
                map.put(AUTH_FIELD_KEY_REALM, realmName);
            } else if (AUTH_FIELD_KEY_NONCE.equals(currentTokenName)) {
                nOnce = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_NONCE, nOnce);
            } else if (AUTH_FIELD_KEY_NC.equals(currentTokenName)) {
                nc = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_NC, nc);
            } else if (AUTH_FIELD_KEY_CNONCE.equals(currentTokenName)) {
                cNonce = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_CNONCE, cNonce);
            } else if (AUTH_FIELD_KEY_QOP.equals(currentTokenName)) {
                qop = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_QOP, qop);
            } else if (AUTH_FIELD_KEY_URI.equals(currentTokenName)) {
                uri = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_URI, uri);
            } else if (AUTH_FIELD_KEY_DOMAIN.equals(currentTokenName)) {
                domain = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_DOMAIN, domain);
            } else if (AUTH_FIELD_KEY_ALGORITHM.equals(currentTokenName)) {
                map.put(AUTH_FIELD_KEY_ALGORITHM, removeQuotes(currentTokenValue));
            } else if (AUTH_FIELD_KEY_RESPONSE.equals(currentTokenName)) {
                response = removeQuotes(currentTokenValue);
                map.put(AUTH_FIELD_KEY_RESPONSE, response);
            } else {
// System.out.println("Unknown token name: \"" + currentTokenName + "\"");
                map.put(currentTokenName, currentTokenValue);
            }

        }

        if ((realmName == null) || (nOnce == null)) {
// System.out.println("One of the mandatory fields is null , returning an empty map");
            return new HashMap<String, String>();
        }

        return map;
    }

    /**
     * Creates a random value based on the current time.
     *
     * @return The calculated value as aString, or null if an error occurs
     */
    public String generateRandomValue() {
        String randomValue = Long.toString(System.currentTimeMillis());
        return MD5.digestAsString(randomValue, encoding);
    }

    /**
     * Return the digest associated with given user name and realm.
     *
     * @param algorithm The algorithm to use
     * @param username Username of the Principal to look up
     * @param realm Realm name
     */
    protected String getDigestUsernameAndRealm(final String algorithm, final String username,
        final String realmName) {
        String a1 = null;
        if (AUTH_FIELD_VALUE_ALGORITHM_AUTH_MD5.equals(algorithm)) {
            StringBuffer bufferValue = new StringBuffer();
            bufferValue.append(username);
            bufferValue.append(HTTP_REPLY_FIELD_SEPARATOR);
            bufferValue.append(realmName);
            bufferValue.append(HTTP_REPLY_FIELD_SEPARATOR);
            bufferValue.append(password);

            a1 = bufferValue.toString();
        } else {
            a1 = "";
        }
// System.out.println("a1 = \"" + a1 + "\"");
        return MD5.digestAsString(a1, encoding);
    }

    /**
     * Return the digest associated with given method and uri.
     *
     * @param method The HTTP method
     * @param uri The URI of the query
     */
    protected String getDigestMethodAndUri(final String qop, final String method, final String uri) {
        String a2 = null;
        if (qop == null || AUTH_FIELD_VALUE_QOP_AUTH.equals(qop)) {
            StringBuffer bufferValue = new StringBuffer();
            bufferValue.append(method);
            bufferValue.append(HTTP_REPLY_FIELD_SEPARATOR);
            bufferValue.append(uri);

            a2 = bufferValue.toString();
        } else {
            a2 = ""; // dummy, to avoid NPE ...
            throw new RuntimeException("not supported qop value: \"" + qop + "\"");
        }
// System.out.println("a2 = \"" + a2 + "\"");

        return MD5.digestAsString(a2, encoding);
    }

    /**
     * Digest the given string using the algorithm specified and convert the
     * result to a corresponding hex string. If exception, the plain credentials
     * string is returned
     *
     * @param value the (input) value to digest
     * @param algorithm Algorithm used to do the digest
     * @param encoding Character encoding of the string to digest
     */
    public final static String digestString(final String value, final String algorithm,
        final String encoding) {

        try {
            // Obtain a new message digest with "digest" encryption
            MessageDigest md = (MessageDigest) MessageDigest.getInstance(algorithm).clone();

            // encode the credentials
            // Should use the digestEncoding, but that's not a static field
            if (encoding == null) {
                md.update(value.getBytes());
            } else {
                md.update(value.getBytes(encoding));
            }

            // Digest the credentials and return as hexadecimal
            return (HexUtils.convert(md.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
