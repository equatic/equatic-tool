package be.ugent.equatic.web.pages.account;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import be.ugent.equatic.web.AccountController;
import be.ugent.equatic.web.pages.AbstractPage;

import java.util.List;
import java.util.stream.Collectors;

public class ResetPasswordPage extends AbstractPage {

    @FindBy(id = "input-password")
    private WebElement password;

    @FindBy(id = "input-confirm-password")
    private WebElement confirmPassword;

    @FindBy(id = "submit-reset-password")
    private WebElement resetPasswordButton;

    @FindBy(id = "error-box")
    private WebElement errorBox;

    public static String getRelativeUrl() {
        return AccountController.VIEW_RESET_PASSWORD;
    }

    public ResetPasswordPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public ResetPasswordPage resetPassword() {
        resetPasswordButton.click();

        return PageFactory.initElements(driver, ResetPasswordPage.class);
    }

    public List<String> getErrors() {
        // TODO: extract form error box handling
        return errorBox.findElements(By.tagName("p")).stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public WebElement getPassword() {
        return password;
    }

    public WebElement getConfirmPassword() {
        return confirmPassword;
    }
}
