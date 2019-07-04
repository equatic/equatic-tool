package be.ugent.equatic.web.pages.superadmin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.web.admin.superadmin.AdminManagementController;
import be.ugent.equatic.web.pages.account.RegistrationPage;

public class CreateAdminPage extends RegistrationPage {

    @FindBy(id = "submit-create")
    private WebElement submitCreate;

    public static String getRelativeUrl() {
        return AdminManagementController.VIEW_CREATE_ADMIN;
    }

    public CreateAdminPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public CreateAdminPage submitCreateExpectError() {
        submitCreate.click();

        return PageFactory.initElements(driver, CreateAdminPage.class);
    }

    public AdminsPage submitCreateExpectSuccess() {
        submitCreate.click();

        return PageFactory.initElements(driver, AdminsPage.class);
    }
}
