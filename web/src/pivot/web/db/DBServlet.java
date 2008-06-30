package pivot.web.db;

import javax.servlet.http.HttpServlet;

/**
 * Servlet that acts as a web-based interface to a database table (and optional
 * set of associated detail tables).
 *
 * NOTE This class is currently just a stub.
 *
 * TODO:
 * - Map one servlet to each top-level (master) table
 * - Provide a "secure" flag to support row-level security; when true, require
 *   the user to log in; assume that the top-level table contains a username
 *   column and append an additional "WHERE username = USER" to the query.
 * - Use built-in database security (usernames/roles); the servlet can return
 *   HTTP 401 when no username is provided by the caller, and 403 when the
 *   database returns an "unauthorized" exception via JDBC
 * - Support configuration of which columns can be queried
 * - Use a naming convention to map path components to table names, or make this
 *   configurable? If configurable, we'll need to do this for column names as
 *   well - might be overly cumbersome
 * - Support configurable serializers; default will be JSON, but provide an
 *   init param for others (allow callers to specify in header, by requesting
 *   a specific MIME type - we'll have a mapping of MIME type to serializer
 *   class, and can return HTTP 412)
 * - QUESTION How should we execute queries? One per table (e.g. master first,
 *   then detail(s)) or a single query that returns a large joined table?
 * - When generating JSON data from SQL result sets, it will be most efficient
 *   to produce JSON directly rather than creating collection classes and using
 *   the serializer. As a result, the content length will not be known until all
 *   the data is serialized. We will want to either buffer the entire response
 *   stream before sending it, or send it using chunked encoding (which may
 *   happen automatically if we don't set the content length)
 *
 * NOTES:
 * - URL query string arguments allow callers to filter the results, but only
 *   using AND operations (equality and LIKE, for wildcard patterns such as
 *   Foo* and F_o)
 * - Use prepared queries for performance?
 * - Use Derby for testing?
 *
 */
public class DBServlet extends HttpServlet {
    public static final long serialVersionUID = 0;
}
