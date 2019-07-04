package be.ugent.equatic.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import be.ugent.equatic.exception.FederatedAuthenticationException;
import be.ugent.equatic.web.AccountController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles authentication failure when user signs in through federated IdP.
 */
public class FederatedSignInAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    /**
     * Wraps the authentication exception in {@link FederatedAuthenticationException} an saves it in user session.
     * <p>
     * Redirects to sign in page.
     *
     * @param request   the HttpServletRequest
     * @param response  the HttpServletResponse
     * @param exception the AuthenticationException
     * @throws IOException      when IO fails
     * @throws ServletException when servlet fails
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        FederatedAuthenticationException federatedAuthException = new FederatedAuthenticationException(exception);
        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, federatedAuthException);

        redirectStrategy.sendRedirect(request, response, AccountController.VIEW_LOGIN);
    }
}
