package be.ugent.equatic.filter;

import com.github.dandelion.core.web.DandelionServlet;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DandelionServletFilter extends DandelionServlet implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/dandelion-assets/**", "GET");

        if (!requestMatcher.matches((HttpServletRequest) request)) {
            chain.doFilter(request, response);

            return;
        }

        doGet((HttpServletRequest) request, (HttpServletResponse) response);
    }

    @Override
    public void destroy() {

    }
}
