package be.ugent.equatic.datasheet;

import be.ugent.equatic.domain.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import be.ugent.equatic.core.FixtureTest;
import be.ugent.equatic.exception.DataSheetProcessingException;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;

public class DataSheetParserTest extends FixtureTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void emptyDataSheetShouldRaiseError() {
        thrown.expect(is(new DataSheetProcessingException("equatic.dataSheet.invalidFormat.noRows")));

        String[][] dataSheetArray = {};

        DataSheetParser.parseDataSheet(DataSheetUtils.arrayToDataSheet(dataSheetArray), getInstitutionsDataSheet());
    }

    @Test
    public void dataSheetWoCountryCodeShouldRaiseError() {
        thrown.expect(is(new DataSheetProcessingException("equatic.dataSheet.invalidFormat.columnNotExists",
                "COUNTRY_CODE")));

        String[][] dataSheetArray = {{"PIC", "ERASMUS_CODE", "NAME", "NAME_EN"}};

        DataSheetParser.parseDataSheet(DataSheetUtils.arrayToDataSheet(dataSheetArray), getInstitutionsDataSheet());
    }

    @Test
    public void dataSheetWithWrongColumnShouldRaiseError() {
        thrown.expect(is(new DataSheetProcessingException("equatic.dataSheet.invalidFormat.columnNotExists",
                "COUNTRY_CODE")));

        String[][] dataSheetArray = {{"PIC", "ERASMUS_CODE", "NAME", "NAME_EN", "WRONG_COLUMN"}};

        DataSheetParser.parseDataSheet(DataSheetUtils.arrayToDataSheet(dataSheetArray), getInstitutionsDataSheet());
    }

    @Test
    public void dataSheetWithCorrectColumnsShouldPass() {
        String[][] dataSheetArray = {getInstitutionsDataSheet().getCurrentColumnTitles()};

        DataSheetParser.parseDataSheet(DataSheetUtils.arrayToDataSheet(dataSheetArray), getInstitutionsDataSheet());
    }

    private static DataSheet getInstitutionsDataSheet() {
        DataSheet institutionsDS = new DataSheet(DataSheetCode.INSTITUTIONS);

        DataSheetColumnType picCT = new DataSheetColumnType("PIC", DataSheetValueType.STRING, false, false);
        DataSheetColumnType erasmusCodeCT =
                new DataSheetColumnType("ERASMUS_CODE", DataSheetValueType.STRING, false, false);
        DataSheetColumnType countryCodeCT =
                new DataSheetColumnType("COUNTRY_CODE", DataSheetValueType.STRING, false, false);
        DataSheetColumnType nameCT = new DataSheetColumnType("NAME", DataSheetValueType.STRING, false, false);

        institutionsDS.setColumns(Arrays.asList(
                new DataSheetColumn(DataSheetColumnCode.PIC, institutionsDS, picCT, "PIC"),
                new DataSheetColumn(DataSheetColumnCode.ERASMUS_CODE, institutionsDS, erasmusCodeCT, "ERASMUS_CODE"),
                new DataSheetColumn(DataSheetColumnCode.NAME, institutionsDS, nameCT, "NAME"),
                new DataSheetColumn(DataSheetColumnCode.NAME_EN, institutionsDS, nameCT, "NAME_EN"),
                new DataSheetColumn(DataSheetColumnCode.COUNTRY_CODE, institutionsDS, countryCodeCT, "COUNTRY_CODE")));

        return institutionsDS;
    }
}
