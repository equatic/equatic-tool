package be.ugent.equatic.web.pages.institutionaladmin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.web.admin.UserManagementController;
import be.ugent.equatic.web.pages.account.RegistrationPage;

public class EditUserPage extends RegistrationPage {

    @FindBy(id = "submit-edit")
    private WebElement submitEdit;

    public static String getRelativeUrl() {
        return UserManagementController.VIEW_EDIT_USER;
    }

    public EditUserPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public UsersPage submitEditExpectSuccess() {
        submitEdit.click();

        return PageFactory.initElements(driver, UsersPage.class);
    }
}
