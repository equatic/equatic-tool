package be.ugent.equatic.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.exception.InstitutionNotFoundException;
import be.ugent.equatic.service.InstitutionService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filters sign in request that should be handled by federated IdP. Redirects those requests to SAML login action.
 * <p>
 * A filter has to be configured with {@link #setLoginUrl(String)} and {@link #setSamlLoginUrl(String)} before use.
 */
@Service
public class FederatedSignInFilter implements Filter {

    @Autowired
    private InstitutionService institutionService;

    /**
     * Sign in action URL.
     */
    private String loginUrl;
    /**
     * Federated sign in entry action URL. Requires SAML entity ID as `idp` parameter.
     */
    private String samlLoginUrl;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * Filters sign in requests for institutions with federated IdP and redirects them to SAML login action.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher(loginUrl, "POST");
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (!requestMatcher.matches(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        Long institutionId = Long.parseLong(request.getParameter("institution"));
        try {
            Institution institution = institutionService.findById(institutionId);
            if (!institution.isWithFederatedIdP()) {
                chain.doFilter(request, response);
                return;
            }

            HttpServletResponse httpResponse = (HttpServletResponse) response;
            UriComponents uriComponents = UriComponentsBuilder.fromPath(httpRequest.getContextPath() + samlLoginUrl)
                    .queryParam("idp", institution.getIdpEntityId()).build();
            httpResponse.sendRedirect(uriComponents.toString());
        } catch (InstitutionNotFoundException ex) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public void setSamlLoginUrl(String samlLoginUrl) {
        this.samlLoginUrl = samlLoginUrl;
    }
}
