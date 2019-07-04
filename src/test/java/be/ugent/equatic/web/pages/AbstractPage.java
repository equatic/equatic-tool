package be.ugent.equatic.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import be.ugent.equatic.domain.Institution;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

abstract public class AbstractPage {

    private static final String SERVER_PORT = "8080";
    private static final int TIME_OUT_IN_SECONDS = 10;
    private static final String SUGGESTIONS_SELECTOR = ".tt-menu .tt-dataset .tt-suggestion";

    protected static final String DEFAULT_PASSWORD = "password";

    protected WebDriver driver;

    protected WebDriverWait wait;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    @FindBy(css = ".alert-warning")
    private WebElement warningMessage;

    @FindBy(css = ".alert-danger")
    private WebElement errorMessage;

    @FindBy(css = "nav #username")
    private WebElement navBarUsername;

    @FindBy(css = ".menu.nav-menu")
    private WebElement mainNavigationMenu;

    @FindBy(id = "error-box")
    private WebElement errorBox;

    @FindBy(css = "input#select-inst")
    private WebElement inputSelectInstitution;

    @FindBy(css = "select#select-inst")
    private WebElement selectorSelectInstitution;

    @FindBy(css = SUGGESTIONS_SELECTOR)
    private List<WebElement> suggestions;

    public AbstractPage(WebDriver driver) throws Exception {
        this.driver = driver;
        wait = new WebDriverWait(driver, TIME_OUT_IN_SECONDS);

        URL currentUrl = new URL(driver.getCurrentUrl());
        URL expectedUrl = getUrl();
        String expectedPathMatch = expectedUrl.getPath().replaceAll("\\{\\w+}", "\\\\w\\+");
        if (!currentUrl.getProtocol().equals(expectedUrl.getProtocol())
                || !currentUrl.getHost().equals(expectedUrl.getHost())
                || !currentUrl.getPath().matches(expectedPathMatch)) {
            logPageSource();
            throw new IllegalStateException(
                    "Expected URL " + expectedUrl.toString() + " but was " + currentUrl.toString());
        }
    }

    protected void logPageSource() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.severe(driver.getPageSource());
    }

    public URL getUrl() throws Exception {
        return getUrlForClass(this.getClass());
    }

    public static <T extends AbstractPage> T to(WebDriver driver, Class<T> resultPageClass) throws Exception {
        driver.get(getUrlForClass(resultPageClass).toString());
        return PageFactory.initElements(driver, resultPageClass);
    }

    private static URL getUrlForClass(Class<? extends AbstractPage> pageClass) throws Exception {
        String relativeUrl = (String) pageClass.getDeclaredMethod("getRelativeUrl").invoke(null);
        return new URL(getAppRootUrl() + relativeUrl);
    }

    public static URL getAppRootUrl() throws MalformedURLException {
        return new URL("http://localhost:" + SERVER_PORT);
    }

    public WebElement getSuccessMessage() {
        return successMessage;
    }

    public WebElement getWarningMessage() {
        return warningMessage;
    }

    public WebElement getErrorMessage() {
        return errorMessage;
    }

    public WebElement getNavBarUsername() {
        return navBarUsername;
    }

    private WebElement getMainNavigationMenu() {
        return mainNavigationMenu;
    }

    public <T extends AbstractPage> T selectActionFromMenu(WebElement menu, String actionPath, Class<T> resultPageClass) {
        Actions actions = new Actions(driver);
        actions.moveToElement(menu);
        WebElement link = menu.findElement(By.cssSelector("a[href='" + actionPath + "']"));
        actions.moveToElement(link);
        actions.click();
        actions.perform();

        return PageFactory.initElements(driver, resultPageClass);
    }

    public WebElement getInstitutionalAdminMenu() {
        return getMainNavigationMenu().findElement(By.className("institutional-admin-menu"));
    }

    public WebElement getSuperAdminMenu() {
        return getMainNavigationMenu().findElement(By.className("super-admin-menu"));
    }

    public List<String> getErrors() {
        return errorBox.findElements(By.tagName("p")).stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public void selectInstitutionByVisibleText(Institution institution) {
        inputSelectInstitution.sendKeys(institution.getDisplayName());

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(SUGGESTIONS_SELECTOR)));

        if (suggestions.size() != 1) {
            throw new RuntimeException("More than one suggestion: " + suggestions);
        }

        WebElement suggestion = suggestions.get(0);
        suggestion.click();
    }

    public boolean isInstitutionSelectorReadonly() {
        return selectorSelectInstitution.getAttribute("readonly") != null;
    }

    public String getSelectedInstitutionDisplayName() {
        return new Select(selectorSelectInstitution).getFirstSelectedOption().getText();
    }
}
