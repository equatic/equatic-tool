package be.ugent.equatic.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataSheetRowRepository extends JpaRepository<DataSheetRow, Long> {

    void deleteByInstitutionAndDataSheetAndAcademicYear(Institution institution, DataSheet dataSheet,
                                                        AcademicYear academicYear);

    @Query("select dsr.dataSheet, count(dsr.id), count(distinct dsr.partnerInstitution) " +
            "from DataSheetRow dsr where dsr.institution = ?1 and dsr.academicYear = ?2 and dsr.selfAssessment = ?3 group by dsr.dataSheet")
    List<Object[]> getUploadStatsForInstitutionAndAcademicYear(Institution institution, AcademicYear academicYear,
                                                               boolean selfAssessment);

    @Query("select dsr.dataSheet, count(dsr.id) " +
            "from DataSheetRow dsr where dsr.academicYear = ?1 group by dsr.dataSheet")
    List<Object[]> getSuperAdminUploadStatsForAcademicYear(AcademicYear academicYear);

    @Query("select distinct dsr.partnerInstitution from DataSheetRow dsr where dsr.institution = ?1 and dsr.selfAssessment = false")
    List<Institution> getPartnerInstitutions(Institution institution);

    @Query("select distinct dsr.partnerInstitution from DataSheetRow dsr " +
            "where dsr.institution in ?1 and dsr.academicYear in ?2 and dsr.selfAssessment = false")
    List<Institution> getPartnerInstitutions(List<Institution> institutions, List<AcademicYear> academicYears);

    @Query("select distinct dsr.partnerInstitution from DataSheetRow dsr " +
            "where dsr.institution in ?1 and dsr.academicYear in ?2 and dsr.partnerInstitution in ?3 and dsr.selfAssessment = false")
    List<Institution> getPartnerInstitutionsFiltered(List<Institution> institutions, List<AcademicYear> academicYears,
                                                     List<Institution> filteredInstitutions);

    List<DataSheetRow> findByDataSheetAndInstitutionAndAcademicYear(DataSheet dataSheet, Institution institution,
                                                                    AcademicYear academicYear);

    List<DataSheetRow> findByDataSheetAndAcademicYear(DataSheet dataSheet, AcademicYear academicYear);

    DataSheetRow findTopByDataSheetCodeOrderByAcademicYearDesc(DataSheetCode dataSheetCode);

    @Query("select dsrv.value from DataSheetRow dsr join dsr.values dsrv " +
            "where dsr.partnerInstitution = ?1 and dsrv.columnCode in ?2 and dsr.academicYear = ?3")
    String getRankingPosition(Institution institution, DataSheetColumnCode dataSheetColumnCode,
                              AcademicYear academicYear);

    @Query("select count(distinct dsr.id) " +
            "from DataSheetRow dsr join dsr.values dsrv " +
            "where dsr.institution in ?1 and dsr.academicYear in ?2 and (true = ?3 or dsr.isced in (?4)) " +
            "and dsr.partnerInstitution = ?5 and dsrv.columnCode = ?6 ")
    int countRowsWithColumn(Institution institution, List<AcademicYear> academicYears, boolean ignoreIsced,
                            List<Isced> isceds, Institution partnerInstitution, DataSheetColumnCode columnCode);

    @Query("select count(distinct dsr.id) " +
            "from DataSheetRow dsr join dsr.values dsrv " +
            "where dsr.institution in ?1 and dsr.academicYear in ?2 and (true = ?3 or dsr.isced in (?4)) " +
            "and dsr.partnerInstitution = ?5 and dsrv.columnCode = ?6 and dsrv.valueCode = ?7 ")
    int countRowsWithColumnAndValue(Institution institution, List<AcademicYear> academicYears, boolean ignoreIsced,
                                    List<Isced> isceds, Institution partnerInstitution,
                                    DataSheetColumnCode columnCode, DataSheetValueCode valueCode);

    @Query("select count(distinct dsr.id) " +
            "from DataSheetRow dsr join dsr.values dsrv " +
            "where dsr.institution in ?1 and dsr.academicYear in ?2 and (true = ?3 or dsr.isced in (?4)) " +
            "and dsr.partnerInstitution = ?5 and dsr.dataSheet.code = ?6")
    int countDataSheetRows(Institution institution, List<AcademicYear> academicYears, boolean ignoreIsced,
                           List<Isced> isceds, Institution partnerInstitution, DataSheetCode dataSheetCode);

    @Query("select sum(dsrv.numericValue) " +
            "from DataSheetRow dsr join dsr.values dsrv " +
            "where dsr.institution in ?1 and dsr.academicYear in ?2 and (true = ?3 or dsr.isced in (?4)) " +
            "and dsr.partnerInstitution = ?5 and dsrv.columnCode = ?6")
    Integer countSumDataSheetRowColumns(Institution institution, List<AcademicYear> academicYears, boolean ignoreIsced,
                                        List<Isced> isceds, Institution partnerInstitution,
                                        DataSheetColumnCode graduatesNumber);
}
