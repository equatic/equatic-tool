package be.ugent.equatic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.ugent.equatic.indicator.IndicatorCode;
import be.ugent.equatic.web.util.InstitutionsScoresMode;
import be.ugent.equatic.web.util.InstitutionsScoresOptions;

import java.util.Arrays;
import java.util.List;

@Service
public class InstitutionsScoresOptionsService {

    @Autowired
    private AcademicYearsOptionService academicYearsOptionService;

    public void initializeInstitutionsScoresOptions(InstitutionsScoresOptions options) {
        academicYearsOptionService.initializeAcademicYearsOption(options);

        List<IndicatorCode> indicatorCodes = options.getIndicatorCodes();
        if (indicatorCodes == null) {
            indicatorCodes = Arrays.asList(IndicatorCode.values());
            options.setIndicatorCodes(indicatorCodes);
        }

        InstitutionsScoresMode mode = options.getMode();
        if (mode == null) {
            mode = InstitutionsScoresMode.DETAILED;
            options.setMode(mode);
        }
    }
}
