package be.ugent.equatic.security;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Modifies the default FormLoginConfigurer to handle additional institution selector.
 * <p>
 * Based on {@link org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer}.
 * <p>
 * Uses {@link InstitutionUsernamePasswordAuthenticationFilter} instead of {@link UsernamePasswordAuthenticationFilter}.
 *
 * @see org.springframework.security.config.annotation.web.HttpSecurityBuilder
 */
public class FormLoginConfigurer<H extends HttpSecurityBuilder<H>>
        extends AbstractAuthenticationFilterConfigurer<H, FormLoginConfigurer<H>, UsernamePasswordAuthenticationFilter> {

    public FormLoginConfigurer() {
        super(new InstitutionUsernamePasswordAuthenticationFilter(), null);
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

    public FormLoginConfigurer<H> loginPage(String loginPage) {
        return super.loginPage(loginPage);
    }
}
