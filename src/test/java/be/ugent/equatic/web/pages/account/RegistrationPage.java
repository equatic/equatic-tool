package be.ugent.equatic.web.pages.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.domain.User;
import be.ugent.equatic.web.AccountController;
import be.ugent.equatic.web.pages.AbstractPage;

import java.util.List;
import java.util.stream.Collectors;

public class RegistrationPage extends AbstractPage {

    @FindBy(id = "input-username")
    private WebElement username;

    @FindBy(id = "input-firstname")
    private WebElement firstname;

    @FindBy(id = "input-lastname")
    private WebElement lastname;

    @FindBy(id = "input-email")
    private WebElement email;

    @FindBy(id = "input-password")
    private WebElement password;

    @FindBy(id = "input-confirm-password")
    private WebElement confirmPassword;

    @FindBy(id = "submit-register")
    private WebElement registerButton;

    @FindBy(id = "error-box")
    private WebElement errorBox;

    public static String getRelativeUrl() {
        return AccountController.VIEW_REGISTER;
    }

    public RegistrationPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public List<String> getErrors() {
        return errorBox.findElements(By.tagName("p")).stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public WebElement getUsername() {
        return username;
    }

    public WebElement getFirstname() {
        return firstname;
    }

    public WebElement getLastname() {
        return lastname;
    }

    public WebElement getEmail() {
        return email;
    }

    public WebElement getPassword() {
        return password;
    }

    public WebElement getConfirmPassword() {
        return confirmPassword;
    }

    public boolean isRegistrationBoxPresent() {
        return driver.findElements(By.id("registration-box")).size() > 0;
    }

    public RegistrationPage register() {
        registerButton.click();

        return PageFactory.initElements(driver, RegistrationPage.class);
    }

    public void fillWithUserData(User user) {
        username.clear();
        username.sendKeys(user.getUsername());
        firstname.clear();
        firstname.sendKeys(user.getFirstname());
        lastname.clear();
        lastname.sendKeys(user.getLastname());
        email.clear();
        email.sendKeys(user.getEmail());
        password.sendKeys(DEFAULT_PASSWORD);
        confirmPassword.sendKeys(DEFAULT_PASSWORD);
    }
}
