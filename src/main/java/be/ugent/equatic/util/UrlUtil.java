package be.ugent.equatic.util;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil {

    /**
     * Returns the root URL of a request.
     * Root URL consists of schema, server name, optional port and context path.
     *
     * @param request the HttpServletRequest
     * @return URL
     * @throws MalformedURLException when the URL is malformed
     */
    public static URL getRootUrlFromRequest(HttpServletRequest request) throws MalformedURLException {
        String schema = request.getScheme();
        int port = request.getServerPort();

        boolean addPort = !((schema.equals("http") && port == 80) || (schema.equals("https") && port == 443));
        return new URL(
                schema + "://" + request.getServerName() + (addPort ? ":" + port : "") + request.getContextPath());
    }
}
