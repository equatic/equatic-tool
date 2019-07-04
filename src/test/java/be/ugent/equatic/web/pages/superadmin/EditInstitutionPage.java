package be.ugent.equatic.web.pages.superadmin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import be.ugent.equatic.web.admin.superadmin.InstitutionManagementController;
import be.ugent.equatic.web.pages.AbstractPage;

public class EditInstitutionPage extends AbstractPage {

    @FindBy(id = "input-pic")
    private WebElement pic;

    @FindBy(id = "input-erasmusCode")
    private WebElement erasmusCode;

    @FindBy(id = "input-name")
    private WebElement name;

    @FindBy(id = "input-nameEn")
    private WebElement nameEn;

    @FindBy(id = "select-country")
    private WebElement selectCountry;

    @FindBy(id = "submit-edit")
    private WebElement submitEdit;

    @FindBy(id = "input-idpEntityId")
    private WebElement idpEntityId;

    @FindBy(id = "input-idpMetadataUrl")
    private WebElement idpMetadataUrl;

    public static String getRelativeUrl() {
        return InstitutionManagementController.VIEW_EDIT_INSTITUTION;
    }

    public EditInstitutionPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public EditInstitutionPage editExpectError() {
        submitEdit.click();

        return PageFactory.initElements(driver, EditInstitutionPage.class);
    }

    public InstitutionsPage editExpectSuccess() {
        submitEdit.click();

        return PageFactory.initElements(driver, InstitutionsPage.class);
    }

    public WebElement getPic() {
        return pic;
    }

    public WebElement getErasmusCode() {
        return erasmusCode;
    }

    public WebElement getName() {
        return name;
    }

    public WebElement getNameEn() {
        return nameEn;
    }

    public Select getCountriesSelect() {
        return new Select(selectCountry);
    }

    public WebElement getIdpEntityId() {
        return idpEntityId;
    }

    public WebElement getIdpMetadataUrl() {
        return idpMetadataUrl;
    }
}
