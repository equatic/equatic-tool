package be.ugent.equatic.web.pages.superadmin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.web.admin.superadmin.AdminManagementController;
import be.ugent.equatic.web.pages.account.RegistrationPage;

public class EditAdminPage extends RegistrationPage {

    @FindBy(id = "submit-edit")
    private WebElement submitEdit;

    public static String getRelativeUrl() {
        return AdminManagementController.VIEW_EDIT_ADMIN;
    }

    public EditAdminPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public AdminsPage submitEditExpectSuccess() {
        submitEdit.click();

        return PageFactory.initElements(driver, AdminsPage.class);
    }
}
