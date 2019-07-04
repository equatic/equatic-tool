package be.ugent.equatic.web.pages.superadmin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.web.admin.superadmin.InstitutionSelectionController;
import be.ugent.equatic.web.pages.AbstractPage;

public class SelectInstitutionPage extends AbstractPage {

    @FindBy(id = "button-select")
    private WebElement buttonSelect;

    public static String getRelativeUrl() {
        return InstitutionSelectionController.VIEW_SELECT_INSTITUTION;
    }

    public SelectInstitutionPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public <T extends AbstractPage> T selectInstitution(Institution institution, Class<T> resultPageClass)
            throws Exception {
        selectInstitutionByVisibleText(institution);
        buttonSelect.click();

        return PageFactory.initElements(driver, resultPageClass);
    }
}
