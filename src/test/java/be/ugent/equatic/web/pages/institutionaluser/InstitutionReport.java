package be.ugent.equatic.web.pages.institutionaluser;

import org.openqa.selenium.WebDriver;
import be.ugent.equatic.web.pages.AbstractPage;
import be.ugent.equatic.web.user.institutional.InstitutionReportController;

public class InstitutionReport extends AbstractPage {

    public static String getRelativeUrl() {
        return InstitutionReportController.VIEW_INSTITUTION_REPORT;
    }

    public InstitutionReport(WebDriver driver) throws Exception {
        super(driver);
    }
}
