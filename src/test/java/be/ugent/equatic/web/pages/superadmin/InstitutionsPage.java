package be.ugent.equatic.web.pages.superadmin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import be.ugent.equatic.domain.Institution;
import be.ugent.equatic.web.admin.superadmin.InstitutionManagementController;
import be.ugent.equatic.web.pages.AbstractPage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstitutionsPage extends AbstractPage {

    private static final String INSTITUTION_ROW = "#institutionsTable tbody tr";

    @FindBy(css = INSTITUTION_ROW)
    private List<WebElement> institutionTableElements;

    public static String getRelativeUrl() {
        return InstitutionManagementController.VIEW_INSTITUTIONS_LIST;
    }

    public InstitutionsPage(WebDriver driver) throws Exception {
        super(driver);
    }

    public List<String> getNameList() throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(INSTITUTION_ROW)));

        return institutionTableElements.stream()
                .map(webElement -> webElement.findElement(By.cssSelector("td span.name")).getText())
                .collect(Collectors.toList());
    }

    public EditInstitutionPage clickEdit(Institution institution) {
        Optional<WebElement> institutionRow = getInstitutionRow(institution);

        By buttonLocator = By.name("button-edit");
        wait.until(ExpectedConditions.visibilityOfElementLocated(buttonLocator));
        institutionRow.get().findElement(buttonLocator).click();

        return PageFactory.initElements(driver, EditInstitutionPage.class);
    }

    private Optional<WebElement> getInstitutionRow(Institution institution) {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(INSTITUTION_ROW)));

        return institutionTableElements.stream()
                .filter(webElement -> webElement.findElement(By.cssSelector("td span.name")).getText()
                        .equals(institution.getName())).findFirst();
    }
}
