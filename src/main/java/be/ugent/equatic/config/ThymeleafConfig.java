package be.ugent.equatic.config;

import com.github.dandelion.datatables.thymeleaf.dialect.DataTablesDialect;
import com.github.dandelion.thymeleaf.dialect.DandelionDialect;
import net.sourceforge.pagesdialect.PagesDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures additional dialects to be added to Thymeleaf.
 */
@Configuration
public class ThymeleafConfig {

    /**
     * PagesDialect is used to add sorting and pagination to Thymeleaf tables {see http://pagesdialect.sourceforge.net/}.
     *
     * @return PagesDialect object, not null
     */
    @Bean
    public PagesDialect pagesDialect() {
        return new PagesDialect();
    }

    @Bean
    public DandelionDialect dandelionDialect() {
        return new DandelionDialect();
    }

    @Bean
    public DataTablesDialect dataTablesDialect() {
        return new DataTablesDialect();
    }
}
