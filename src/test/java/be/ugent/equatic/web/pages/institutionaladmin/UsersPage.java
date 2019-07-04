package be.ugent.equatic.web.pages.institutionaladmin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.web.admin.UserManagementController;
import be.ugent.equatic.web.pages.AbstractPage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsersPage extends AbstractPage {

    @FindBy(className = "user-row")
    private List<WebElement> userTableElements;

    @FindBy(id = "btn-confirm-add-privilege")
    private WebElement addPrivilegeConfirmButton;

    public static String getRelativeUrl() {
        return UserManagementController.VIEW_USERS_LIST;
    }

    public UsersPage(WebDriver driver) throws Exception {
        super(driver);
    }

    private List<WebElement> getUserTableElements() {
        return userTableElements;
    }

    public <T extends UsersPage> T activateUser(User user, HtmlUnitDriver driver, Class<T> usersPageClass) {
        Optional<WebElement> userRow = getUserRow(user);
        userRow.get().findElement(By.className("btn-activate")).click();

        return PageFactory.initElements(driver, usersPageClass);
    }

    public <T extends UsersPage> T deactivateUser(User user, HtmlUnitDriver driver, Class<T> usersPageClass) {
        Optional<WebElement> userRow = getUserRow(user);
        userRow.get().findElement(By.className("btn-deactivate")).click();

        return PageFactory.initElements(driver, usersPageClass);
    }

    public <T extends AbstractPage> T editUser(User user, HtmlUnitDriver driver, Class<T> editPageClass) {
        Optional<WebElement> userRow = getUserRow(user);
        userRow.get().findElement(By.className("btn-edit")).click();

        return PageFactory.initElements(driver, editPageClass);
    }

    protected Optional<WebElement> getUserRow(User user) {
        return userTableElements.stream().filter(
                webElement -> webElement.findElement(By.name("username")).getText().equals(user.getUsername()))
                .findFirst();
    }

    public List<String> getUsernameList() {
        return getUserTableElements().stream()
                .map(webElement -> webElement.findElement(By.name("username")).getText())
                .collect(Collectors.toList());
    }

    public UsersPage addAdminPrivilege(User user, HtmlUnitDriver driver) {
        Optional<WebElement> userRow = getUserRow(user);
        userRow.get().findElement(By.className("btn-add-privilege")).click();
        wait.until(ExpectedConditions.visibilityOf(addPrivilegeConfirmButton));
        addPrivilegeConfirmButton.click();

        return PageFactory.initElements(driver, UsersPage.class);
    }

    public UsersPage removeAdminPrivilege(User admin, HtmlUnitDriver driver) {
        Optional<WebElement> userRow = getUserRow(admin);
        userRow.get().findElement(By.className("btn-remove-privilege")).click();

        return PageFactory.initElements(driver, UsersPage.class);
    }
}
