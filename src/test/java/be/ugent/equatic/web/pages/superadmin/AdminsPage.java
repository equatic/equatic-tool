package be.ugent.equatic.web.pages.superadmin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.web.admin.superadmin.AdminManagementController;
import be.ugent.equatic.web.pages.institutionaladmin.UsersPage;

import java.util.Optional;

public class AdminsPage extends UsersPage {

    @FindBy(className = "btn-create")
    private WebElement buttonCreate;

    public static String getRelativeUrl() {
        return AdminManagementController.VIEW_ADMINS_LIST;
    }

    public AdminsPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public CreateAdminPage clickCreate() {
        buttonCreate.click();

        return PageFactory.initElements(driver, CreateAdminPage.class);
    }

    public AdminsPage removeAdminPrivilege(User admin, HtmlUnitDriver driver) {
        Optional<WebElement> userRow = getUserRow(admin);
        userRow.get().findElement(By.className("btn-remove-privilege")).click();

        return PageFactory.initElements(driver, AdminsPage.class);
    }
}
