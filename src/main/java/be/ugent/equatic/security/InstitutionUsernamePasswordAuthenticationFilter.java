package be.ugent.equatic.security;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extends UsernamePasswordAuthenticationFilter to handle additional institution selector.
 *
 * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 */
public class InstitutionUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * Based on parent class implementation.
     * <p>
     * Gets additional institution ID and creates {@link InstitutionUsernamePasswordAuthenticationToken}.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Long institutionId = Long.parseLong(request.getParameter("institution"));

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        InstitutionUsernamePasswordAuthenticationToken authRequest =
                new InstitutionUsernamePasswordAuthenticationToken(username, password, institutionId);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
