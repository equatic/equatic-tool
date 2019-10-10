package be.ugent.equatic.service;

import be.ugent.equatic.domain.*;
import be.ugent.equatic.util.BroadIscedStat;
import be.ugent.equatic.util.IscedStat;
import be.ugent.equatic.web.user.national.NationalUserReportRow;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.hibernate.type.DoubleType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSheetRowService {

    private static final String GRADING_TABLE_QUERY = "SELECT 1\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS GRADING_TABLES\n" +
            "              WHERE DATA_SHEET_CODE = :gradingTablesDataSheet AND ACADEMIC_YEAR IN (:academicYears)\n" +
            "                    AND GRADING_TABLES.PARTNER_INST_ID = :partnerInstitution";

    private static final String EXCHANGE_OF_ECTS_DOCUMENTS_QUERY = "SELECT\n" +
            "  ROWS_COUNTED.PARTNER_INST_ID,\n" +
            "  0.25 * AVG(LA.SUBSCORE) + 0.25 * AVG(TOR_CERTIFICATE.SUBSCORE) +\n" +
            "  CASE WHEN EXISTS (\n" +
            "              SELECT 1\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS GRADING_TABLES\n" +
            "              WHERE DATA_SHEET_CODE = :gradingTablesDataSheet AND ACADEMIC_YEAR IN (:academicYears)\n" +
            "                    AND GRADING_TABLES.PARTNER_INST_ID = ROWS_COUNTED.PARTNER_INST_ID\n" +
            "            )\n" +
            "    THEN 0.5\n" +
            "  ELSE 0 END AS SCORE,\n" +
            "  MAX(ROWS_COUNTED.COUNT) AS STUDENTS_COUNT,\n" +
            "  VAR_POP(LA.SUBSCORE) * MAX(ROWS_COUNTED.COUNT) AS LA_SUM_SQUARE_DIFF,\n" +
            "  AVG(LA.SUBSCORE) AS LA_SUBSCORE,\n" +
            "  VAR_POP(TOR_CERTIFICATE.SUBSCORE) * MAX(ROWS_COUNTED.COUNT) AS TOR_SUM_SQUARE_DIFF,\n" +
            "  AVG(TOR_CERTIFICATE.SUBSCORE) AS TOR_SUBSCORE\n" +
            "FROM\n" +
            "  (\n" +
            "    SELECT\n" +
            "      PARTNER_INST_ID,\n" +
            "      COUNT(DISTINCT DATA_SHEET_ROWS.ID) AS COUNT\n" +
            "    FROM DATA_SHEET_ROWS\n" +
            "      JOIN DATA_SHEET_ROW_VALUES ON DATA_SHEET_ROWS.ID = DATA_SHEET_ROW_VALUES.ROW_ID\n" +
            "    WHERE COLUMN_CODE IN\n" +
            "          (:smsLaSignedColumn, :smsLaNotSignedColumn, :smpLaSignedColumn, :smpLaNotSignedColumn, :smsTorColumn, :smpCertificateColumn)\n" +
            "          AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "    GROUP BY PARTNER_INST_ID\n" +
            "  ) ROWS_COUNTED\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                PARTNER_INST_ID,\n" +
            "                CASE SMS_LA_SIGNED.VALUE_CODE\n" +
            "                WHEN :LA_SIGNED_YES\n" +
            "                  THEN 1\n" +
            "                ELSE (\n" +
            "                  CASE SMS_LA_NOT_SIGNED.VALUE_CODE\n" +
            "                  WHEN :LA_NOT_SIGNED_SMS_RECEIVING\n" +
            "                    THEN 0\n" +
            "                  ELSE 1 END\n" +
            "                ) END AS SUBSCORE\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS\n" +
            "                JOIN DATA_SHEET_ROW_VALUES SMS_LA_SIGNED\n" +
            "                  ON DATA_SHEET_ROWS.ID = SMS_LA_SIGNED.ROW_ID\n" +
            "                     AND SMS_LA_SIGNED.COLUMN_CODE = :smsLaSignedColumn\n" +
            "                LEFT JOIN DATA_SHEET_ROW_VALUES SMS_LA_NOT_SIGNED\n" +
            "                  ON DATA_SHEET_ROWS.ID = SMS_LA_NOT_SIGNED.ROW_ID\n" +
            "                     AND SMS_LA_NOT_SIGNED.COLUMN_CODE = :smsLaNotSignedColumn\n" +
            "              WHERE\n" +
            "                ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "              UNION ALL\n" +
            "              SELECT\n" +
            "                PARTNER_INST_ID,\n" +
            "                CASE SMP_LA_SIGNED.VALUE_CODE\n" +
            "                WHEN :LA_SIGNED_YES\n" +
            "                  THEN 1\n" +
            "                ELSE (\n" +
            "                  CASE SMP_LA_NOT_SIGNED.VALUE_CODE\n" +
            "                  WHEN :LA_NOT_SIGNED_SMP_RECEIVING\n" +
            "                    THEN 0\n" +
            "                  ELSE 1 END\n" +
            "                ) END AS SUBSCORE\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS\n" +
            "                JOIN DATA_SHEET_ROW_VALUES SMP_LA_SIGNED\n" +
            "                  ON DATA_SHEET_ROWS.ID = SMP_LA_SIGNED.ROW_ID\n" +
            "                     AND SMP_LA_SIGNED.COLUMN_CODE = :smpLaSignedColumn\n" +
            "                LEFT JOIN DATA_SHEET_ROW_VALUES SMP_LA_NOT_SIGNED\n" +
            "                  ON DATA_SHEET_ROWS.ID = SMP_LA_NOT_SIGNED.ROW_ID\n" +
            "                     AND SMP_LA_NOT_SIGNED.COLUMN_CODE = :smpLaNotSignedColumn\n" +
            "              WHERE\n" +
            "                ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "            ) LA ON LA.PARTNER_INST_ID = ROWS_COUNTED.PARTNER_INST_ID\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                PARTNER_INST_ID,\n" +
            "                CASE SMS_TOR.VALUE_CODE\n" +
            "                WHEN :YES\n" +
            "                  THEN 1\n" +
            "                ELSE 0 END AS SUBSCORE\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS\n" +
            "                JOIN DATA_SHEET_ROW_VALUES SMS_TOR\n" +
            "                  ON DATA_SHEET_ROWS.ID = SMS_TOR.ROW_ID\n" +
            "                     AND SMS_TOR.COLUMN_CODE = :smsTorColumn\n" +
            "              WHERE\n" +
            "                ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "              UNION ALL\n" +
            "              SELECT\n" +
            "                PARTNER_INST_ID,\n" +
            "                CASE SMP_CERTIFICATE.VALUE_CODE\n" +
            "                WHEN :YES\n" +
            "                  THEN 1\n" +
            "                ELSE 0 END AS SUBSCORE\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS\n" +
            "                JOIN DATA_SHEET_ROW_VALUES SMP_CERTIFICATE\n" +
            "                  ON DATA_SHEET_ROWS.ID = SMP_CERTIFICATE.ROW_ID\n" +
            "                     AND SMP_CERTIFICATE.COLUMN_CODE = :smpCertificateColumn\n" +
            "              WHERE\n" +
            "                ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "            ) TOR_CERTIFICATE ON TOR_CERTIFICATE.PARTNER_INST_ID = ROWS_COUNTED.PARTNER_INST_ID\n" +
            "WHERE 1 = 1 ";

    private static final String LA_SIGNED_PERCENTAGE_QUERY = "SELECT 100 * AVG(SUBSCORE) AS PERCENTAGE\n" +
            "FROM (\n" +
            "  SELECT CASE SMS_LA_SIGNED.VALUE_CODE\n" +
            "         WHEN :LA_SIGNED_YES\n" +
            "           THEN 1\n" +
            "         ELSE (\n" +
            "           CASE SMS_LA_NOT_SIGNED.VALUE_CODE\n" +
            "           WHEN :LA_NOT_SIGNED_SMS_RECEIVING\n" +
            "             THEN 0\n" +
            "           ELSE 1 END\n" +
            "         ) END AS SUBSCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES SMS_LA_SIGNED\n" +
            "      ON DATA_SHEET_ROWS.ID = SMS_LA_SIGNED.ROW_ID\n" +
            "         AND SMS_LA_SIGNED.COLUMN_CODE = :smsLaSignedColumn\n" +
            "    LEFT JOIN DATA_SHEET_ROW_VALUES SMS_LA_NOT_SIGNED\n" +
            "      ON DATA_SHEET_ROWS.ID = SMS_LA_NOT_SIGNED.ROW_ID\n" +
            "         AND SMS_LA_NOT_SIGNED.COLUMN_CODE = :smsLaNotSignedColumn\n" +
            "         AND SMS_LA_NOT_SIGNED.VALUE_CODE = :LA_NOT_SIGNED_SMS_RECEIVING\n" +
            "  WHERE\n" +
            "    ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "    AND PARTNER_INST_ID = (:partnerInstitution)\n" +
            "  UNION ALL\n" +
            "  SELECT CASE SMP_LA_SIGNED.VALUE_CODE\n" +
            "         WHEN :LA_SIGNED_YES\n" +
            "           THEN 1\n" +
            "         ELSE (\n" +
            "           CASE SMP_LA_NOT_SIGNED.VALUE_CODE\n" +
            "           WHEN :LA_NOT_SIGNED_SMP_RECEIVING\n" +
            "             THEN 0\n" +
            "           ELSE 1 END\n" +
            "         ) END AS SUBSCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES SMP_LA_SIGNED\n" +
            "      ON DATA_SHEET_ROWS.ID = SMP_LA_SIGNED.ROW_ID\n" +
            "         AND SMP_LA_SIGNED.COLUMN_CODE = :smpLaSignedColumn\n" +
            "    LEFT JOIN DATA_SHEET_ROW_VALUES SMP_LA_NOT_SIGNED\n" +
            "      ON DATA_SHEET_ROWS.ID = SMP_LA_NOT_SIGNED.ROW_ID\n" +
            "         AND SMP_LA_NOT_SIGNED.COLUMN_CODE = :smpLaNotSignedColumn\n" +
            "         AND SMP_LA_NOT_SIGNED.VALUE_CODE = :LA_NOT_SIGNED_SMP_RECEIVING\n" +
            "  WHERE\n" +
            "    ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "    AND PARTNER_INST_ID = (:partnerInstitution)\n" +
            ")";

    private static final String TOR_ON_TIME_PERCENTAGE_QUERY = "SELECT 100 * AVG(SUBSCORE) AS PERCENTAGE\n" +
            "FROM (\n" +
            "  SELECT CASE SMS_TOR.VALUE_CODE\n" +
            "         WHEN :YES\n" +
            "           THEN 1\n" +
            "         ELSE 0 END AS SUBSCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES SMS_TOR\n" +
            "      ON DATA_SHEET_ROWS.ID = SMS_TOR.ROW_ID\n" +
            "         AND SMS_TOR.COLUMN_CODE = :smsTorColumn\n" +
            "  WHERE\n" +
            "    ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND\n" +
            "    INST_ID IN (:institutions)\n" +
            "    AND PARTNER_INST_ID = (:partnerInstitution)\n" +
            "  UNION ALL\n" +
            "  SELECT CASE SMP_CERTIFICATE.VALUE_CODE\n" +
            "         WHEN :YES\n" +
            "           THEN 1\n" +
            "         ELSE 0 END AS SUBSCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES SMP_CERTIFICATE\n" +
            "      ON DATA_SHEET_ROWS.ID = SMP_CERTIFICATE.ROW_ID\n" +
            "         AND SMP_CERTIFICATE.COLUMN_CODE = :smpCertificateColumn\n" +
            "  WHERE\n" +
            "    ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND\n" +
            "    INST_ID IN (:institutions)\n" +
            "    AND PARTNER_INST_ID = (:partnerInstitution)\n" +
            ")";

    private static final String SUPPORT_SUBSCORE_QUERY = " AVG(DEFAULT_VALUE) AS AVG\n" +
            "    FROM DATA_SHEET_ROWS\n" +
            "      JOIN DATA_SHEET_ROW_VALUES ON DATA_SHEET_ROWS.ID = DATA_SHEET_ROW_VALUES.ROW_ID\n" +
            "      JOIN DATA_SHEET_VALUE_CODES ON CODE = DATA_SHEET_ROW_VALUES.VALUE_CODE\n" +
            "    WHERE\n" +
            "      COLUMN_CODE IN (:supportColumnCodes)\n" +
            "      AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds))\n" +
            "      AND INST_ID IN (:institutions)\n";

    private static final String FACILITIES_SUBSCORE_QUERY = " AVG(DEFAULT_VALUE) AS AVG\n" +
            "    FROM DATA_SHEET_ROWS\n" +
            "      JOIN DATA_SHEET_ROW_VALUES ON DATA_SHEET_ROWS.ID = DATA_SHEET_ROW_VALUES.ROW_ID\n" +
            "      JOIN DATA_SHEET_VALUE_CODES ON CODE = DATA_SHEET_ROW_VALUES.VALUE_CODE\n" +
            "    WHERE\n" +
            "      COLUMN_CODE IN (:facilitiesColumnCodes)\n" +
            "      AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds))\n" +
            "      AND INST_ID IN (:institutions)\n";

    private static final String SUPPORT_AND_FACILITIES_QUERY = "SELECT\n" +
            "  DATA_SHEET_ROWS.PARTNER_INST_ID,\n" +
            "  AVG(CASE WHEN (FACILITIES_AVGS.AVG IS NULL) THEN SUPPORT_AVGS.AVG\n" +
            "    ELSE 0.8 * SUPPORT_AVGS.AVG + 0.2 * FACILITIES_AVGS.AVG END) AS SCORE,\n" +
            "  COUNT(DISTINCT DATA_SHEET_ROWS.ID) AS STUDENTS_COUNT,\n" +
            "  VAR_POP(SUPPORT_AVGS.AVG) * COUNT(DISTINCT DATA_SHEET_ROWS.ID) AS SUPPORT_SUM_SQUARE_DIFF,\n" +
            "  AVG(SUPPORT_AVGS.AVG) AS SUPPORT_SUBSCORE,\n" +
            "  VAR_POP(FACILITIES_AVGS.AVG) * COUNT(DISTINCT DATA_SHEET_ROWS.ID) AS FACILITIES_SUM_SQUARE_DIFF,\n" +
            "  AVG(FACILITIES_AVGS.AVG) AS FACILITIES_SUBSCORE\n" +
            "FROM\n" +
            "  DATA_SHEET_ROWS\n" +
            "  LEFT JOIN (\n" +
            "    SELECT PARTNER_INST_ID, DATA_SHEET_ROWS.ID AS ROW_ID, " + SUPPORT_SUBSCORE_QUERY +
            "    GROUP BY PARTNER_INST_ID, DATA_SHEET_ROWS.ID\n" +
            "  ) SUPPORT_AVGS ON DATA_SHEET_ROWS.ID = SUPPORT_AVGS.ROW_ID\n" +
            "  LEFT JOIN (\n" +
            "    SELECT PARTNER_INST_ID, DATA_SHEET_ROWS.ID AS ROW_ID, " + FACILITIES_SUBSCORE_QUERY +
            "    GROUP BY PARTNER_INST_ID, DATA_SHEET_ROWS.ID\n" +
            "  ) FACILITIES_AVGS ON DATA_SHEET_ROWS.ID = FACILITIES_AVGS.ROW_ID\n" +
            "WHERE\n" +
            "  SUPPORT_AVGS.ROW_ID IS NOT NULL AND INST_ID IN (:institutions) ";

    private static final String STUDY_SUCCESS_PERCENTAGE_QUERY = "SELECT\n" +
            "    100 * SUM(CREDITS_COMPLETED.NUMERIC_VALUE) / SUM(CREDITS_TAKEN.NUMERIC_VALUE) PERCENTAGE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES CREDITS_TAKEN\n" +
            "      ON DATA_SHEET_ROWS.ID = CREDITS_TAKEN.ROW_ID" +
            "         AND CREDITS_TAKEN.COLUMN_CODE = :creditsTakenColumn\n" +
            "    JOIN DATA_SHEET_ROW_VALUES CREDITS_COMPLETED\n" +
            "      ON DATA_SHEET_ROWS.ID = CREDITS_COMPLETED.ROW_ID" +
            "         AND CREDITS_COMPLETED.COLUMN_CODE = :creditsCompletedColumn\n" +
            "  WHERE\n" +
            "    DATA_SHEET_CODE = :dataSheet\n" +
            "    AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID = :institution" +
            "    AND PARTNER_INST_ID = :partnerInstitution";

    private static final String STUDENTS_AVERAGE_CREDITS_PER_DAY_QUERY = "SELECT\n" +
            "    AVG(LEAST(CREDITS_TAKEN.NUMERIC_VALUE / (END_DATE.DATE_VALUE - START_DATE.DATE_VALUE), 0.2)) AVERAGE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES CREDITS_TAKEN\n" +
            "      ON DATA_SHEET_ROWS.ID = CREDITS_TAKEN.ROW_ID" +
            "         AND CREDITS_TAKEN.COLUMN_CODE = :creditsTakenColumn\n" +
            "    JOIN DATA_SHEET_ROW_VALUES START_DATE\n" +
            "      ON DATA_SHEET_ROWS.ID = START_DATE.ROW_ID\n" +
            "         AND START_DATE.COLUMN_CODE = :startDateColumn\n" +
            "    JOIN DATA_SHEET_ROW_VALUES END_DATE\n" +
            "      ON DATA_SHEET_ROWS.ID = END_DATE.ROW_ID\n" +
            "         AND END_DATE.COLUMN_CODE = :endDateColumn\n" +
            "  WHERE\n" +
            "    DATA_SHEET_CODE = :dataSheet\n" +
            "    AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID = :institution" +
            "    AND PARTNER_INST_ID = :partnerInstitution";

    private static final String STUDENTS_CREDITS_DAYS_COMMON_QUERY = "SELECT\n" +
            "  PARTNER_INST_ID,\n" +
            "  AVG(CASE WHEN RAW_SCORE > 1.0 THEN 1.0 ELSE RAW_SCORE END) AS SCORE,\n" +
            "  COUNT(RAW_SCORE) AS STUDENTS_COUNT,\n" +
            "  VAR_POP(CASE WHEN RAW_SCORE > 1.0 THEN 1.0 ELSE RAW_SCORE END) * COUNT(RAW_SCORE) AS SUM_SQUARE_DIFF\n" +
            "FROM (\n" +
            "  SELECT\n" +
            "    PARTNER_INST_ID,\n" +
            "    CREDITS_COMPLETED.NUMERIC_VALUE / (0.2 * (END_DATE.DATE_VALUE - START_DATE.DATE_VALUE)) RAW_SCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES CREDITS_COMPLETED\n" +
            "      ON DATA_SHEET_ROWS.ID = CREDITS_COMPLETED.ROW_ID" +
            "         AND CREDITS_COMPLETED.COLUMN_CODE = :creditsCompletedColumn\n" +
            "    JOIN DATA_SHEET_ROW_VALUES START_DATE\n" +
            "      ON DATA_SHEET_ROWS.ID = START_DATE.ROW_ID\n" +
            "         AND START_DATE.COLUMN_CODE = :startDateColumn\n" +
            "    JOIN DATA_SHEET_ROW_VALUES END_DATE\n" +
            "      ON DATA_SHEET_ROWS.ID = END_DATE.ROW_ID\n" +
            "         AND END_DATE.COLUMN_CODE = :endDateColumn\n" +
            "  WHERE\n" +
            "    DATA_SHEET_CODE = :dataSheet\n" +
            "    AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "    AND END_DATE.DATE_VALUE > START_DATE.DATE_VALUE\n" +
            ") STUDENT_SCORES\n" +
            "WHERE 1 = 1 ";

    private static final String COURSE_CATALOGUE_INFORMATION_QUERY = "SELECT\n" +
            "  PARTNER_INST_ID,\n" +
            "  AVG(RAW_SCORE) AS SCORE,\n" +
            "  COUNT(CASE WHEN RAW_SCORE IS NOT NULL THEN 1 END) AS STUDENTS_COUNT,\n" +
            "  VAR_POP(RAW_SCORE) * COUNT(CASE WHEN RAW_SCORE IS NOT NULL THEN 1 END) AS SUM_SQUARE_DIFF\n" +
            "FROM (\n" +
            "  SELECT\n" +
            "    PARTNER_INST_ID,\n" +
            "    SUM(DEFAULT_VALUE) RAW_SCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES\n" +
            "      ON DATA_SHEET_ROWS.ID = ROW_ID\n" +
            "         AND COLUMN_CODE = :smsCourseCatalogueColumn\n" +
            "    JOIN DATA_SHEET_VALUE_CODES ON CODE = VALUE_CODE\n" +
            "  WHERE\n" +
            "    ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "  GROUP BY PARTNER_INST_ID, DATA_SHEET_ROWS.ID\n" +
            ") STUDENT_SCORES\n" +
            "WHERE 1 = 1 ";

    private static final String EDUCATIONAL_COOPERATION_QUERY = "SELECT\n" +
            "  PARTNER_PROJECTS.PARTNER_INST_ID,\n" +
            "  DENSE_RANK()\n" +
            "  OVER (\n" +
            "    ORDER BY PROJECTS_COUNT ) * 50 / (SELECT COUNT( DISTINCT COUNT(DISTINCT ID)) AS MAX_PROJECTS_COUNT\n" +
            "   FROM\n" +
            "     DATA_SHEET_ROWS\n" +
            "   WHERE\n" +
            "     DATA_SHEET_CODE = :dataSheet\n" +
            "     AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "   GROUP BY PARTNER_INST_ID)" +
            "  + CASE WHEN GRADUATES_COUNT IS NULL\n" +
            "    THEN 0\n" +
            "    ELSE 25 + 25 * GRADUATES_COUNT / (SELECT SUM(DATA_SHEET_ROW_VALUES.NUMERIC_VALUE)\n" +
            "                            FROM DATA_SHEET_ROWS\n" +
            "                              JOIN DATA_SHEET_ROW_VALUES\n" +
            "                                ON DATA_SHEET_ROWS.ID = ROW_ID AND COLUMN_CODE = :graduatesNumberColumn\n" +
            "                                   AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)) END AS SCORE\n" +
            "FROM (\n" +
            "  SELECT\n" +
            "    PARTNER_INST_ID,\n" +
            "    COUNT(DISTINCT ID) AS PROJECTS_COUNT\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "  WHERE\n" +
            "    DATA_SHEET_CODE = :dataSheet\n" +
            "    AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "  GROUP BY PARTNER_INST_ID\n" +
            ") PARTNER_PROJECTS\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                PARTNER_INST_ID,\n" +
            "                SUM(DATA_SHEET_ROW_VALUES.NUMERIC_VALUE) AS GRADUATES_COUNT\n" +
            "              FROM\n" +
            "                DATA_SHEET_ROWS\n" +
            "                JOIN DATA_SHEET_ROW_VALUES\n" +
            "                  ON DATA_SHEET_ROWS.ID = ROW_ID AND COLUMN_CODE = :graduatesNumberColumn\n" +
            "              GROUP BY PARTNER_INST_ID\n" +
            "            ) PARTNER_PROGRAMMES ON PARTNER_PROJECTS.PARTNER_INST_ID = PARTNER_PROGRAMMES.PARTNER_INST_ID\n" +
            "WHERE 1 = 1 ";

    private static final String CALCULATE_SCORES_FROM_DEFAULT_VALUES_QUERY = "SELECT\n" +
            "  PARTNER_INST_ID,\n" +
            "  AVG(RAW_SCORE) AS SCORE,\n" +
            "  COUNT(CASE WHEN RAW_SCORE IS NOT NULL THEN 1 END) AS STUDENTS_COUNT,\n" +
            "  VAR_POP(RAW_SCORE) * COUNT(CASE WHEN RAW_SCORE IS NOT NULL THEN 1 END) AS SUM_SQUARE_DIFF\n" +
            "FROM (\n" +
            "  SELECT\n" +
            "    PARTNER_INST_ID,\n" +
            "    AVG(DEFAULT_VALUE) RAW_SCORE\n" +
            "  FROM\n" +
            "    DATA_SHEET_ROWS\n" +
            "    JOIN DATA_SHEET_ROW_VALUES\n" +
            "      ON DATA_SHEET_ROWS.ID = ROW_ID\n" +
            "         AND COLUMN_CODE IN (:columnCodes)\n" +
            "    JOIN DATA_SHEET_VALUE_CODES ON CODE = VALUE_CODE\n" +
            "  WHERE\n" +
            "    ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "  GROUP BY PARTNER_INST_ID, DATA_SHEET_ROWS.ID\n" +
            ") STUDENT_SCORES\n" +
            "WHERE 1 = 1 ";

    private static final String DATA_SHEET_ROWS_COUNT_QUERY = "SELECT\n" +
            "  COUNT(*) AS COUNT\n" +
            "FROM DATA_SHEET_ROWS\n" +
            "WHERE DATA_SHEET_CODE = (:dataSheet)\n" +
            "      AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)" +
            "      AND PARTNER_INST_ID = (:partnerInstitution)";

    private static final String ACADEMIC_YEARS_WITH_MOBILITY_COUNT_QUERY = "SELECT\n" +
            "  COUNT(DISTINCT ACADEMIC_YEAR) AS COUNT\n" +
            "FROM DATA_SHEET_ROWS\n" +
            "WHERE DATA_SHEET_CODE IN (:mobilityDataSheets)\n" +
            "      AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n";

    private static final String MOBILITY_RATE_QUERY = "SELECT\n" +
            "  PARTNER_INST_ID,\n" +
            "  (100 - 2 * ABS(50 - 100 * NVL(SUM(CASE WHEN DATA_SHEET_CODE IN (:incomingDataSheets)\n" +
            "    THEN 1 END), 0) / COUNT(ID)) +\n" +
            "   100 * COUNT(DISTINCT ACADEMIC_YEAR) / :academicYearsWithMobilityCount) / 2 AS SCORE,\n" +
            "  COUNT(ID)                                                                   AS STUDENTS_COUNT\n" +
            "FROM DATA_SHEET_ROWS\n" +
            "WHERE DATA_SHEET_CODE IN (:mobilityDataSheets)\n" +
            "      AND ACADEMIC_YEAR IN (:academicYears) AND\n" +
            "      (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions) ";

    private static final String BROAD_ISCED_STAT_QUERY = "SELECT\n" +
            "  BROAD_ISCED_COUNT,\n" +
            "  MIN(NARROW_ISCED_COUNT) MIN_NARROW_ISCED_COUNT,\n" +
            "  MAX(NARROW_ISCED_COUNT) MAX_NARROW_ISCED_COUNT\n" +
            "FROM (\n" +
            "  SELECT\n" +
            "    COUNT(DISTINCT SUBSTR(ISCED_CODE, 1, 2)) BROAD_ISCED_COUNT,\n" +
            "    COUNT(DISTINCT ISCED_CODE) NARROW_ISCED_COUNT\n" +
            "  FROM DATA_SHEET_ROWS\n" +
            "  WHERE\n" +
            "    DATA_SHEET_CODE = :dataSheet\n" +
            "    AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions)\n" +
            "  GROUP BY PARTNER_INST_ID\n" +
            ")\n" +
            "GROUP BY BROAD_ISCED_COUNT";

    private static final String PARTNER_ISCED_STAT_QUERY = "SELECT\n" +
            "  PARTNER_INST_ID,\n" +
            "  COUNT(DISTINCT SUBSTR(ISCED_CODE, 1, 2)) BROAD_ISCED_COUNT,\n" +
            "  COUNT(DISTINCT ISCED_CODE) NARROW_ISCED_COUNT\n" +
            "FROM DATA_SHEET_ROWS\n" +
            "WHERE\n" +
            "  DATA_SHEET_CODE = :dataSheet\n" +
            "  AND ACADEMIC_YEAR IN (:academicYears) AND (:ignoreIsced = 1 OR ISCED_CODE IN (:isceds)) AND INST_ID IN (:institutions) ";

    private static final String NATIONAL_USER_REPORT_QUERY = "SELECT\n" +
            "  INSTITUTIONS_TO.DISPLAY_NAME                                   AS institutionToDisplayName,\n" +
            "  INSTITUTIONS_FROM.DISPLAY_NAME                                 AS institutionFromDisplayName,\n" +
            "  STUDENTS_OUTGOING.COUNT                                        AS outgoingStudents,\n" +
            "  STUDENTS_INCOMING.COUNT                                        AS incomingStudents,\n" +
            "  STAFF_OUTGOING.COUNT                                           AS outgoingStaff,\n" +
            "  STAFF_INCOMING.COUNT                                           AS incomingStaff,\n" +
            "  SUBSTR(INVOLVEMENT_ISCED.ISCED_CODE, 1,\n" +
            "         2)                                                      AS broadIsced,\n" +
            "  COALESCE(INVOLVEMENT_ISCED.ISCED_CODE, STUDENTS_OUTGOING.ISCED_CODE, STUDENTS_INCOMING.ISCED_CODE,\n" +
            "           STAFF_OUTGOING.ISCED_CODE, STAFF_INCOMING.ISCED_CODE) AS narrowIsced,\n" +
            "  PARTNERS_WITH_DATA.MIN_ACADEMIC_YEAR                           AS uploadMinAcademicYear,\n" +
            "  PARTNERS_WITH_DATA.MAX_ACADEMIC_YEAR                           AS uploadMaxAcademicYear\n" +
            "FROM\n" +
            "  (\n" +
            "    SELECT\n" +
            "      INST_ID,\n" +
            "      PARTNER_INST_ID,\n" +
            "      MIN(ACADEMIC_YEAR) AS MIN_ACADEMIC_YEAR,\n" +
            "      MAX(ACADEMIC_YEAR) AS MAX_ACADEMIC_YEAR\n" +
            "    FROM DATA_SHEET_ROWS\n" +
            "    WHERE\n" +
            "      INST_ID IN (:institutionsFrom)\n" +
            "      AND PARTNER_INST_ID IN (:institutionsTo)\n" +
            "    GROUP BY INST_ID, PARTNER_INST_ID\n" +
            "  ) PARTNERS_WITH_DATA\n" +
            "  JOIN INSTITUTIONS INSTITUTIONS_FROM\n" +
            "    ON INSTITUTIONS_FROM.ID = PARTNERS_WITH_DATA.INST_ID\n" +
            "  JOIN INSTITUTIONS INSTITUTIONS_TO\n" +
            "    ON INSTITUTIONS_TO.ID = PARTNERS_WITH_DATA.PARTNER_INST_ID\n" +
            "  CROSS JOIN (\n" +
            "               SELECT CODE\n" +
            "               FROM ISCEDS\n" +
            "               UNION SELECT NULL AS CODE\n" +
            "                     FROM dual\n" +
            "             ) ISCEDS\n" +
            "  LEFT JOIN (\n" +
            "              SELECT DISTINCT\n" +
            "                ISCED_CODE,\n" +
            "                INST_ID,\n" +
            "                PARTNER_INST_ID\n" +
            "              FROM DATA_SHEET_ROWS\n" +
            "              WHERE\n" +
            "                DATA_SHEET_CODE = (:institutionalAgreementsDataSheet)\n" +
            "            ) INVOLVEMENT_ISCED\n" +
            "    ON INVOLVEMENT_ISCED.INST_ID = INSTITUTIONS_FROM.ID\n" +
            "       AND INVOLVEMENT_ISCED.PARTNER_INST_ID = INSTITUTIONS_TO.ID\n" +
            "       AND INVOLVEMENT_ISCED.ISCED_CODE = ISCEDS.CODE\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                COUNT(*) AS COUNT,\n" +
            "                INST_ID,\n" +
            "                PARTNER_INST_ID,\n" +
            "                ISCED_CODE\n" +
            "              FROM DATA_SHEET_ROWS\n" +
            "              WHERE DATA_SHEET_CODE = (:studentsOutgoingDataSheet)\n" +
            "                    AND ACADEMIC_YEAR IN (:academicYears)\n" +
            "              GROUP BY INST_ID, PARTNER_INST_ID, ISCED_CODE\n" +
            "            ) STUDENTS_OUTGOING\n" +
            "    ON STUDENTS_OUTGOING.INST_ID = INSTITUTIONS_FROM.ID\n" +
            "       AND STUDENTS_OUTGOING.PARTNER_INST_ID = INSTITUTIONS_TO.ID\n" +
            "       AND (STUDENTS_OUTGOING.ISCED_CODE = ISCEDS.CODE OR (STUDENTS_OUTGOING.ISCED_CODE IS NULL AND ISCEDS.CODE IS NULL))\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                COUNT(*) AS COUNT,\n" +
            "                INST_ID,\n" +
            "                PARTNER_INST_ID,\n" +
            "                ISCED_CODE\n" +
            "              FROM DATA_SHEET_ROWS\n" +
            "              WHERE DATA_SHEET_CODE = (:studentsIncomingDataSheet)\n" +
            "                    AND ACADEMIC_YEAR IN (:academicYears)\n" +
            "              GROUP BY INST_ID, PARTNER_INST_ID, ISCED_CODE\n" +
            "            ) STUDENTS_INCOMING\n" +
            "    ON STUDENTS_INCOMING.INST_ID = INSTITUTIONS_FROM.ID\n" +
            "       AND STUDENTS_INCOMING.PARTNER_INST_ID = INSTITUTIONS_TO.ID\n" +
            "       AND (STUDENTS_INCOMING.ISCED_CODE = ISCEDS.CODE OR (STUDENTS_INCOMING.ISCED_CODE IS NULL AND ISCEDS.CODE IS NULL))\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                COUNT(*) AS COUNT,\n" +
            "                INST_ID,\n" +
            "                PARTNER_INST_ID,\n" +
            "                ISCED_CODE\n" +
            "              FROM DATA_SHEET_ROWS\n" +
            "              WHERE DATA_SHEET_CODE = (:staffOutgoingDataSheet)\n" +
            "                    AND ACADEMIC_YEAR IN (:academicYears)\n" +
            "              GROUP BY INST_ID, PARTNER_INST_ID, ISCED_CODE\n" +
            "            ) STAFF_OUTGOING\n" +
            "    ON STAFF_OUTGOING.INST_ID = INSTITUTIONS_FROM.ID\n" +
            "       AND STAFF_OUTGOING.PARTNER_INST_ID = INSTITUTIONS_TO.ID\n" +
            "       AND (STAFF_OUTGOING.ISCED_CODE = ISCEDS.CODE OR (STAFF_OUTGOING.ISCED_CODE IS NULL AND ISCEDS.CODE IS NULL))\n" +
            "  LEFT JOIN (\n" +
            "              SELECT\n" +
            "                COUNT(*) AS COUNT,\n" +
            "                INST_ID,\n" +
            "                PARTNER_INST_ID,\n" +
            "                ISCED_CODE\n" +
            "              FROM DATA_SHEET_ROWS\n" +
            "              WHERE DATA_SHEET_CODE = (:staffIncomingDataSheet)\n" +
            "                    AND ACADEMIC_YEAR IN (:academicYears)\n" +
            "              GROUP BY INST_ID, PARTNER_INST_ID, ISCED_CODE\n" +
            "            ) STAFF_INCOMING\n" +
            "    ON STAFF_INCOMING.INST_ID = INSTITUTIONS_FROM.ID\n" +
            "       AND STAFF_INCOMING.PARTNER_INST_ID = INSTITUTIONS_TO.ID\n" +
            "       AND (STAFF_INCOMING.ISCED_CODE = ISCEDS.CODE OR (STAFF_INCOMING.ISCED_CODE IS NULL AND ISCEDS.CODE IS NULL))\n" +
            "WHERE\n" +
            "  COALESCE(STUDENTS_OUTGOING.COUNT, STUDENTS_INCOMING.COUNT, STAFF_OUTGOING.COUNT, STAFF_INCOMING.COUNT) IS NOT NULL OR\n" +
            "  INVOLVEMENT_ISCED.ISCED_CODE IS NOT NULL\n" +
            "ORDER BY INSTITUTIONS_TO.DISPLAY_NAME, INSTITUTIONS_FROM.DISPLAY_NAME, INVOLVEMENT_ISCED.ISCED_CODE";

    private static final String RANKING_AVERAGES_QUERY = "SELECT\n" +
            "  PARTNER_INST_ID,\n" +
            "  AVG(\n" +
            "      CASE WHEN REGEXP_LIKE(VALUE, '\\d+-\\d+') OR REGEXP_LIKE(VALUE, '\\d+–\\d+')\n" +
            "        THEN (TO_NUMBER(REGEXP_SUBSTR(VALUE, '\\d+'))\n" +
            "              + TO_NUMBER(REGEXP_SUBSTR(VALUE, '\\d+', 1, 2))) / 2\n" +
            "      ELSE TO_NUMBER(VALUE) END\n" +
            "  ) AS AVERAGE\n" +
            "FROM\n" +
            "  DATA_SHEET_ROWS\n" +
            "  JOIN DATA_SHEET_ROW_VALUES\n" +
            "    ON DATA_SHEET_ROWS.ID = DATA_SHEET_ROW_VALUES.ROW_ID\n" +
            "WHERE\n" +
            "  DATA_SHEET_CODE = :dataSheet\n" +
            "  AND ACADEMIC_YEAR = :academicYear\n" +
            "GROUP BY PARTNER_INST_ID";

    private static final String[] SUPPORT_COLUMN_CODES = new String[]{
            DataSheetColumnCode.SMS_SUPPORT_ADMINISTRATIVE.name(),
            DataSheetColumnCode.SMS_SUPPORT_MENTORING.name(),
            DataSheetColumnCode.SMS_SUPPORT_INITIATIVES.name(),
            DataSheetColumnCode.SMS_DEAL_QUESTIONS.name(),
            DataSheetColumnCode.SMS_FIND_ACCOMMODATION.name(),
            DataSheetColumnCode.SMP_DEAL_QUESTIONS.name(),
            DataSheetColumnCode.SMP_FIND_ACCOMMODATION.name()
    };

    private static final String[] FACILITIES_COLUMN_CODES = new String[]{
            DataSheetColumnCode.SMS_FACILITIES_CLASSROOM.name(),
            DataSheetColumnCode.SMS_FACILITIES_STUDY_ROOMS.name(),
            DataSheetColumnCode.SMS_FACILITIES_LIBRARIES.name(),
            DataSheetColumnCode.SMS_FACILITIES_ACCESS_PC.name(),
            DataSheetColumnCode.SMS_FACILITIES_ACCESS_INTERNET.name(),
            DataSheetColumnCode.SMS_FACILITIES_CAFETERIA.name(),
            DataSheetColumnCode.SMS_FACILITIES_ACCESS_PUBLICATIONS.name()
    };

    private static final String[] INCOMING_DATA_SHEET_CODES = new String[]{
            DataSheetCode.STUDENTS_INCOMING.name(),
            DataSheetCode.STAFF_INCOMING.name()
    };

    private static final String[] MOBILITY_DATA_SHEET_CODES = new String[]{
            DataSheetCode.STUDENTS_INCOMING.name(),
            DataSheetCode.STAFF_INCOMING.name(),
            DataSheetCode.STUDENTS_OUTGOING.name(),
            DataSheetCode.STAFF_OUTGOING.name()
    };

    @Autowired
    private DataSheetRowRepository dataSheetRowRepository;

    @Autowired
    private EntityManager entityManager;

    public List<DataSheetRow> findAll() {
        return dataSheetRowRepository.findAll();
    }

    @Transactional
    public List<DataSheetRow> save(List<DataSheetRow> rows) {
        return dataSheetRowRepository.save(rows);
    }

    @Transactional
    public void deleteAll() {
        dataSheetRowRepository.deleteAll();
    }

    @Transactional
    public void deleteByInstitutionAndDataSheetAndAcademicYear(Institution institution, DataSheet dataSheet,
                                                               AcademicYear academicYear) {
        dataSheetRowRepository.deleteByInstitutionAndDataSheetAndAcademicYear(institution, dataSheet, academicYear);
    }

    public List<UploadStat> getUploadStatsForInstitutionAndAcademicYear(Institution institution,
                                                                        AcademicYear academicYear,
                                                                        boolean selfAssessment) {
        return dataSheetRowRepository.getUploadStatsForInstitutionAndAcademicYear(institution, academicYear,
                selfAssessment).stream()
                .map(result -> new UploadStat((DataSheet) result[0], (Long) result[1], (Long) result[2]))
                .collect(Collectors.toList());
    }

    public List<UploadStat> getSuperAdminUploadStatsForAcademicYear(AcademicYear academicYear) {
        return dataSheetRowRepository.getSuperAdminUploadStatsForAcademicYear(academicYear).stream()
                .map(result -> new UploadStat((DataSheet) result[0], null, (Long) result[1]))
                .collect(Collectors.toList());
    }

    public List<Institution> getPartnerInstitutions(Institution institution) {
        return dataSheetRowRepository.getPartnerInstitutions(institution);
    }

    public Map<Long, Institution> getPartnerInstitutionMap(List<Institution> institutions,
                                                           List<AcademicYear> academicYears,
                                                           List<Institution> filteredInstitutions) {
        List<Institution> partnerInstitutions;
        if (filteredInstitutions == null) {
            partnerInstitutions = dataSheetRowRepository.getPartnerInstitutions(institutions, academicYears);
        } else {
            partnerInstitutions = dataSheetRowRepository.getPartnerInstitutionsFiltered(institutions, academicYears,
                    filteredInstitutions);
        }

        Map<Long, Institution> partnerInstitutionMap = new HashMap<>();
        for (Institution partnerInstitution : partnerInstitutions) {
            partnerInstitutionMap.put(partnerInstitution.getId(), partnerInstitution);
        }

        return partnerInstitutionMap;
    }

    Map<Country, List<Institution>> partnerInstitutionsByCountryMap(List<Institution> institutions,
                                                                    List<AcademicYear> academicYears) {
        List<Institution> partnerInstitutions = dataSheetRowRepository.getPartnerInstitutions(institutions,
                academicYears);

        HashMap<Country, List<Institution>> resultMap = new HashMap<>();
        for (Institution partnerInstitution : partnerInstitutions) {
            Country country = partnerInstitution.getCountry();
            if (resultMap.containsKey(country)) {
                resultMap.get(country).add(partnerInstitution);
            } else {
                resultMap.put(country, new ArrayList<>(Collections.singleton(partnerInstitution)));
            }
        }

        return resultMap;
    }

    @Transactional(readOnly = true)
    public List<Object[]> calculateExchangeOfEctsDocumentsScores(
            List<Institution> institutions, List<AcademicYear> academicYears, List<Isced> isceds,
            List<Institution> filteredInstitutions) {
        SQLQuery query = getCommonScoresQuery(institutions, academicYears, isceds, filteredInstitutions,
                EXCHANGE_OF_ECTS_DOCUMENTS_QUERY, "ROWS_COUNTED.PARTNER_INST_ID");

        query.setParameter("gradingTablesDataSheet", DataSheetCode.GRADING_TABLES.name())
                .setParameter("smsLaSignedColumn", DataSheetColumnCode.SMS_LA_SIGNED.name())
                .setParameter("smsLaNotSignedColumn", DataSheetColumnCode.SMS_LA_NOT_SIGNED.name())
                .setParameter("smpLaSignedColumn", DataSheetColumnCode.SMP_LA_SIGNED.name())
                .setParameter("smpLaNotSignedColumn", DataSheetColumnCode.SMP_LA_NOT_SIGNED.name())
                .setParameter("smsTorColumn", DataSheetColumnCode.SMS_TOR.name())
                .setParameter("smpCertificateColumn", DataSheetColumnCode.SMP_CERTIFICATE.name())
                .setParameter("YES", DataSheetValueCode.YES.name())
                .setParameter("LA_SIGNED_YES", DataSheetValueCode.LA_SIGNED_YES.name())
                .setParameter("LA_NOT_SIGNED_SMS_RECEIVING", DataSheetValueCode.LA_NOT_SIGNED_SMS_RECEIVING.name())
                .setParameter("LA_NOT_SIGNED_SMP_RECEIVING", DataSheetValueCode.LA_NOT_SIGNED_SMP_RECEIVING.name());

        query.addScalar("LA_SUM_SQUARE_DIFF", DoubleType.INSTANCE);
        query.addScalar("LA_SUBSCORE", DoubleType.INSTANCE);
        query.addScalar("TOR_SUM_SQUARE_DIFF", DoubleType.INSTANCE);
        query.addScalar("TOR_SUBSCORE", DoubleType.INSTANCE);

        return query.list();
    }

    @Transactional(readOnly = true)
    public Integer getLaSignedPercentage(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                         Institution partnerInstitution) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(LA_SIGNED_PERCENTAGE_QUERY);

        setCommonParametersForQuery(query, Collections.singletonList(institution), academicYears, isceds);

        query.setParameter("smsLaSignedColumn", DataSheetColumnCode.SMS_LA_SIGNED.name())
                .setParameter("smsLaNotSignedColumn", DataSheetColumnCode.SMS_LA_NOT_SIGNED.name())
                .setParameter("smpLaSignedColumn", DataSheetColumnCode.SMP_LA_SIGNED.name())
                .setParameter("smpLaNotSignedColumn", DataSheetColumnCode.SMP_LA_NOT_SIGNED.name())
                .setParameter("LA_SIGNED_YES", DataSheetValueCode.LA_SIGNED_YES.name())
                .setParameter("LA_NOT_SIGNED_SMS_RECEIVING", DataSheetValueCode.LA_NOT_SIGNED_SMS_RECEIVING.name())
                .setParameter("LA_NOT_SIGNED_SMP_RECEIVING", DataSheetValueCode.LA_NOT_SIGNED_SMP_RECEIVING.name())
                .setParameter("partnerInstitution", partnerInstitution.getId());

        query.addScalar("PERCENTAGE", IntegerType.INSTANCE);

        return (Integer) query.uniqueResult();
    }

    @Transactional(readOnly = true)
    public Integer getTorOnTimePercentage(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                          Institution partnerInstitution) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(TOR_ON_TIME_PERCENTAGE_QUERY);

        setCommonParametersForQuery(query, Collections.singletonList(institution), academicYears, isceds);

        query.setParameter("smsTorColumn", DataSheetColumnCode.SMS_TOR.name())
                .setParameter("smpCertificateColumn", DataSheetColumnCode.SMP_CERTIFICATE.name())
                .setParameter("YES", DataSheetValueCode.YES.name())
                .setParameter("partnerInstitution", partnerInstitution.getId());

        query.addScalar("PERCENTAGE", IntegerType.INSTANCE);

        return (Integer) query.uniqueResult();
    }

    public boolean isGradingTableAvailable(Institution partnerInstitution, List<AcademicYear> academicYears) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(GRADING_TABLE_QUERY);

        query.setParameter("gradingTablesDataSheet", DataSheetCode.GRADING_TABLES.name())
                .setParameterList("academicYears", academicYears.stream().map(AcademicYear::getAcademicYear).toArray())
                .setParameter("partnerInstitution", partnerInstitution.getId());

        return query.list().size() > 0;
    }

    public Optional<Double> getSupportSubscore(Institution institution, List<AcademicYear> academicYears,
                                               List<Isced> isceds,
                                               Institution partnerInstitution) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(
                "SELECT " + SUPPORT_SUBSCORE_QUERY + " AND PARTNER_INST_ID = :partnerInstitution");

        query.setParameterList("supportColumnCodes", SUPPORT_COLUMN_CODES)
                .setParameter("partnerInstitution", partnerInstitution.getId());

        setCommonParametersForQuery(query, Collections.singletonList(institution), academicYears, isceds);

        query.addScalar("AVG", DoubleType.INSTANCE);

        return Optional.ofNullable((Double) query.uniqueResult());
    }

    public Optional<Double> getFacilitiesSubscore(Institution institution, List<AcademicYear> academicYears,
                                                  List<Isced> isceds,
                                                  Institution partnerInstitution) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(
                "SELECT " + FACILITIES_SUBSCORE_QUERY + " AND PARTNER_INST_ID = :partnerInstitution");

        query.setParameterList("facilitiesColumnCodes", FACILITIES_COLUMN_CODES)
                .setParameter("partnerInstitution", partnerInstitution.getId());

        setCommonParametersForQuery(query, Collections.singletonList(institution), academicYears, isceds);

        query.addScalar("AVG", DoubleType.INSTANCE);

        return Optional.ofNullable((Double) query.uniqueResult());
    }

    public List<Object[]> calculateSupportAndFacilitiesScores(List<Institution> institutions,
                                                              List<AcademicYear> academicYears,
                                                              List<Isced> isceds,
                                                              List<Institution> filteredInstitutions) {
        SQLQuery query = getCommonScoresQuery(institutions, academicYears, isceds, filteredInstitutions,
                SUPPORT_AND_FACILITIES_QUERY, "DATA_SHEET_ROWS.PARTNER_INST_ID");

        query.setParameterList("supportColumnCodes", SUPPORT_COLUMN_CODES)
                .setParameterList("facilitiesColumnCodes", FACILITIES_COLUMN_CODES);

        query.addScalar("SUPPORT_SUM_SQUARE_DIFF", DoubleType.INSTANCE);
        query.addScalar("SUPPORT_SUBSCORE", DoubleType.INSTANCE);
        query.addScalar("FACILITIES_SUM_SQUARE_DIFF", DoubleType.INSTANCE);
        query.addScalar("FACILITIES_SUBSCORE", DoubleType.INSTANCE);

        return query.list();
    }

    public Integer getStudySuccessPercentage(Institution institution, List<AcademicYear> academicYears,
                                             List<Isced> isceds, Institution partnerInstitution,
                                             DataSheetCode dataSheetCode) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(STUDY_SUCCESS_PERCENTAGE_QUERY);

        query.setParameter("dataSheet", dataSheetCode.name())
                .setParameter("creditsTakenColumn", DataSheetColumnCode.CREDITS_TAKEN.name())
                .setParameter("creditsCompletedColumn", DataSheetColumnCode.CREDITS_COMPLETED.name())
                .setParameterList("academicYears", academicYears.stream().map(AcademicYear::getAcademicYear).toArray())
                .setParameterList("isceds", getIscedCodesFromIsceds(isceds))
                .setParameter("ignoreIsced", isceds == null ? 1 : 0)
                .setParameter("institution", institution.getId())
                .setParameter("partnerInstitution", partnerInstitution.getId());

        query.addScalar("PERCENTAGE", IntegerType.INSTANCE);

        return (Integer) query.uniqueResult();
    }

    public Double getAverageCreditsPerDay(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                          Institution partnerInstitution, DataSheetCode dataSheetCode) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(STUDENTS_AVERAGE_CREDITS_PER_DAY_QUERY);

        query.setParameter("dataSheet", dataSheetCode.name())
                .setParameter("startDateColumn", DataSheetColumnCode.START_DATE.name())
                .setParameter("endDateColumn", DataSheetColumnCode.END_DATE.name())
                .setParameter("creditsTakenColumn", DataSheetColumnCode.CREDITS_TAKEN.name())
                .setParameterList("academicYears", academicYears.stream().map(AcademicYear::getAcademicYear).toArray())
                .setParameterList("isceds", getIscedCodesFromIsceds(isceds))
                .setParameter("ignoreIsced", isceds == null ? 1 : 0)
                .setParameter("institution", institution.getId())
                .setParameter("partnerInstitution", partnerInstitution.getId());

        query.addScalar("AVERAGE", DoubleType.INSTANCE);

        return (Double) query.uniqueResult();
    }

    public List<Object[]> calculateStudentsCreditsDaysScores(List<Institution> institutions,
                                                             List<AcademicYear> academicYears,
                                                             List<Isced> isceds,
                                                             List<Institution> filteredInstitutions,
                                                             DataSheetCode dataSheetCode) {
        SQLQuery query = getCommonScoresQuery(institutions, academicYears, isceds, filteredInstitutions,
                STUDENTS_CREDITS_DAYS_COMMON_QUERY, "PARTNER_INST_ID");

        query.setParameter("dataSheet", dataSheetCode.name())
                .setParameter("startDateColumn", DataSheetColumnCode.START_DATE.name())
                .setParameter("endDateColumn", DataSheetColumnCode.END_DATE.name())
                .setParameter("creditsCompletedColumn", DataSheetColumnCode.CREDITS_COMPLETED.name());

        query.addScalar("SUM_SQUARE_DIFF", DoubleType.INSTANCE);

        return query.list();
    }

    public List<Object[]> calculateScoresFromDefaultValues(List<Institution> institutions,
                                                           List<AcademicYear> academicYears,
                                                           List<Isced> isceds,
                                                           List<DataSheetColumnCode> columnCodes,
                                                           List<Institution> filteredInstitutions) {
        SQLQuery query = getCommonScoresQuery(institutions, academicYears, isceds, filteredInstitutions,
                CALCULATE_SCORES_FROM_DEFAULT_VALUES_QUERY, "PARTNER_INST_ID");

        query.setParameterList("columnCodes", columnCodes.stream().map(DataSheetColumnCode::toString).toArray());

        query.addScalar("SUM_SQUARE_DIFF", DoubleType.INSTANCE);

        return query.list();
    }

    public List<Object[]> calculateCourseCatalogueInformationScores(List<Institution> institutions,
                                                                    List<AcademicYear> academicYears,
                                                                    List<Isced> isceds,
                                                                    List<Institution> filteredInstitutions) {
        SQLQuery query = getCommonScoresQuery(institutions, academicYears, isceds, filteredInstitutions,
                COURSE_CATALOGUE_INFORMATION_QUERY, "PARTNER_INST_ID");

        query.setParameter("smsCourseCatalogueColumn", DataSheetColumnCode.SMS_COURSE_CATALOGUE.name());

        query.addScalar("SUM_SQUARE_DIFF", DoubleType.INSTANCE);

        return query.list();
    }

    public List<Object[]> calculateEducationalCooperationScores(List<Institution> institutions,
                                                                List<AcademicYear> academicYears,
                                                                List<Isced> isceds,
                                                                List<Institution> filteredInstitutions) {
        Session session = getSession();
        String rawQuery = EDUCATIONAL_COOPERATION_QUERY;

        if (filteredInstitutions != null) {
            rawQuery += "AND PARTNER_INST_ID IN (:partnerInstitutions) ";
        }

        SQLQuery query = session.createSQLQuery(rawQuery);

        if (filteredInstitutions != null) {
            query.setParameterList("partnerInstitutions",
                    filteredInstitutions.stream().map(Institution::getId).toArray());
        }

        setCommonParametersForQuery(query, institutions, academicYears, isceds);

        query.addScalar("PARTNER_INST_ID", LongType.INSTANCE)
                .addScalar("SCORE", DoubleType.INSTANCE);

        query.setParameter("dataSheet", DataSheetCode.EDUCATIONAL_PROJECTS.name())
                .setParameter("graduatesNumberColumn", DataSheetColumnCode.GRADUATES_NUMBER.name());

        return query.list();
    }

    public Integer getDataSheetRowsCount(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                         Institution partnerInstitution, DataSheetCode dataSheetCode) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(DATA_SHEET_ROWS_COUNT_QUERY);

        setCommonParametersForQuery(query, Collections.singletonList(institution), academicYears, isceds);

        query.setParameter("dataSheet", dataSheetCode.name())
                .setParameter("partnerInstitution", partnerInstitution.getId());

        query.addScalar("COUNT", IntegerType.INSTANCE);

        return (Integer) query.uniqueResult();
    }

    public List<Object[]> calculateMobilityRateScores(List<Institution> institutions, List<AcademicYear> academicYears,
                                                      List<Isced> isceds, List<Institution> filteredInstitutions,
                                                      Integer academicYearsWithMobilityCount) {
        SQLQuery query = getCommonScoresQuery(institutions, academicYears, isceds, filteredInstitutions,
                MOBILITY_RATE_QUERY, "PARTNER_INST_ID");

        query.setParameter("academicYearsWithMobilityCount", academicYearsWithMobilityCount)
                .setParameterList("mobilityDataSheets", MOBILITY_DATA_SHEET_CODES)
                .setParameterList("incomingDataSheets", INCOMING_DATA_SHEET_CODES);

        return query.list();
    }

    public Integer getAcademicYearsWithMobilityCount(List<Institution> institutions, List<AcademicYear> academicYears,
                                                     List<Isced> isceds) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(ACADEMIC_YEARS_WITH_MOBILITY_COUNT_QUERY);

        setCommonParametersForQuery(query, institutions, academicYears, isceds);

        query.setParameterList("mobilityDataSheets", MOBILITY_DATA_SHEET_CODES);

        query.addScalar("COUNT", IntegerType.INSTANCE);

        return (Integer) query.uniqueResult();
    }

    public Map<Integer, BroadIscedStat> getBroadIscedStatMap(List<Institution> institutions,
                                                             List<AcademicYear> academicYears,
                                                             List<Isced> isceds) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(BROAD_ISCED_STAT_QUERY);

        query.setParameter("dataSheet", DataSheetCode.INSTITUTIONAL_AGREEMENTS.name());

        setCommonParametersForQuery(query, institutions, academicYears, isceds);

        query.addScalar("BROAD_ISCED_COUNT", IntegerType.INSTANCE)
                .addScalar("MIN_NARROW_ISCED_COUNT", IntegerType.INSTANCE)
                .addScalar("MAX_NARROW_ISCED_COUNT", IntegerType.INSTANCE);

        List<Object[]> broadIscedStats = query.list();

        Map<Integer, BroadIscedStat> iscedStatMap = new HashMap<>();
        for (Object[] entry : broadIscedStats) {
            Integer broadIscedCount = (Integer) entry[0];
            Integer minNarrowIscedCount = (Integer) entry[1];
            Integer maxNarrowIscedCount = (Integer) entry[2];

            iscedStatMap.put(broadIscedCount, new BroadIscedStat(minNarrowIscedCount, maxNarrowIscedCount));
        }

        return iscedStatMap;
    }

    public Map<Long, IscedStat> getPartnerInstitutionsIscedStatMap(List<Institution> institutions,
                                                                   List<AcademicYear> academicYears,
                                                                   List<Isced> isceds,
                                                                   List<Institution> filteredInstitutions) {
        SQLQuery query = getCommonQuery(institutions, academicYears, isceds, filteredInstitutions,
                PARTNER_ISCED_STAT_QUERY, "PARTNER_INST_ID");

        query.setParameter("dataSheet", DataSheetCode.INSTITUTIONAL_AGREEMENTS.name());

        query.addScalar("PARTNER_INST_ID", LongType.INSTANCE)
                .addScalar("BROAD_ISCED_COUNT", IntegerType.INSTANCE)
                .addScalar("NARROW_ISCED_COUNT", IntegerType.INSTANCE);

        List<Object[]> partnerInstitutionsIscedStats = query.list();

        Map<Long, IscedStat> partnerInstitutionsIscedStatMap = new HashMap<>();
        for (Object[] entry : partnerInstitutionsIscedStats) {
            Long institutionId = (Long) entry[0];
            Integer broadIscedCount = (Integer) entry[1];
            Integer narrowIscedCount = (Integer) entry[2];

            partnerInstitutionsIscedStatMap.put(institutionId, new IscedStat(broadIscedCount, narrowIscedCount));
        }

        return partnerInstitutionsIscedStatMap;
    }

    private SQLQuery getCommonScoresQuery(List<Institution> institutions, List<AcademicYear> academicYears,
                                          List<Isced> isceds, List<Institution> filteredInstitutions, String rawQuery,
                                          String partnerInstitutionIdColumn) {
        SQLQuery query = getCommonQuery(institutions, academicYears, isceds, filteredInstitutions, rawQuery,
                partnerInstitutionIdColumn);

        query.addScalar("PARTNER_INST_ID", LongType.INSTANCE)
                .addScalar("SCORE", DoubleType.INSTANCE)
                .addScalar("STUDENTS_COUNT", LongType.INSTANCE);

        return query;
    }

    private SQLQuery getCommonQuery(List<Institution> institutions, List<AcademicYear> academicYears,
                                    List<Isced> isceds, List<Institution> filteredInstitutions, String rawQuery,
                                    String partnerInstitutionIdColumn) {
        Session session = getSession();

        if (filteredInstitutions != null) {
            rawQuery += "AND " + partnerInstitutionIdColumn + " IN (:partnerInstitutions) ";
        }

        SQLQuery query = session.createSQLQuery(rawQuery + "GROUP BY " + partnerInstitutionIdColumn);

        if (filteredInstitutions != null) {
            query.setParameterList("partnerInstitutions",
                    filteredInstitutions.stream().map(Institution::getId).toArray());
        }

        setCommonParametersForQuery(query, institutions, academicYears, isceds);
        return query;
    }

    private void setCommonParametersForQuery(SQLQuery query, List<Institution> institutions,
                                             List<AcademicYear> academicYears,
                                             List<Isced> isceds) {
        query.setParameterList("institutions", institutions.stream().map(Institution::getId).toArray())
                .setParameterList("academicYears", academicYears.stream().map(AcademicYear::getAcademicYear).toArray())
                .setParameterList("isceds", getIscedCodesFromIsceds(isceds))
                .setParameter("ignoreIsced", isceds == null ? 1 : 0);
    }

    private String[] getIscedCodesFromIsceds(List<Isced> isceds) {
        if (isceds == null) {
            return new String[]{""};
        } else {
            return isceds.stream().map(Isced::getCode).toArray(String[]::new);
        }
    }

    private Session getSession() {
        Session session = entityManager.unwrap(Session.class);
        if (session == null) {
            throw new RuntimeException("Could not obtain Session");
        }
        return session;
    }

    public List<DataSheetRow> findByDataSheetAndInstitutionAndAcademicYear(DataSheet dataSheet, Institution institution,
                                                                           AcademicYear academicYear) {
        return dataSheetRowRepository.findByDataSheetAndInstitutionAndAcademicYear(dataSheet, institution,
                academicYear);
    }

    public List<DataSheetRow> findByDataSheetAndAcademicYear(DataSheet dataSheet, AcademicYear academicYear) {
        return dataSheetRowRepository.findByDataSheetAndAcademicYear(dataSheet, academicYear);
    }

    public List<NationalUserReportRow> getNationalUserReportRows(List<Institution> institutionsTo,
                                                                 List<Institution> institutionsFrom,
                                                                 List<AcademicYear> academicYears) {
        if (institutionsTo.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        Session session = getSession();

        SQLQuery query = session.createSQLQuery(NATIONAL_USER_REPORT_QUERY);

        query.setParameterList("institutionsTo", institutionsTo.stream().map(Institution::getId).toArray())
                .setParameterList("institutionsFrom", institutionsFrom.stream().map(Institution::getId).toArray())
                .setParameterList("academicYears", academicYears.stream().map(AcademicYear::getAcademicYear).toArray())
                .setParameter("studentsOutgoingDataSheet", DataSheetCode.STUDENTS_OUTGOING.name())
                .setParameter("studentsIncomingDataSheet", DataSheetCode.STUDENTS_INCOMING.name())
                .setParameter("staffOutgoingDataSheet", DataSheetCode.STAFF_OUTGOING.name())
                .setParameter("staffIncomingDataSheet", DataSheetCode.STAFF_INCOMING.name())
                .setParameter("institutionalAgreementsDataSheet", DataSheetCode.INSTITUTIONAL_AGREEMENTS.name());

        query.addScalar("institutionToDisplayName", StringType.INSTANCE)
                .addScalar("institutionFromDisplayName", StringType.INSTANCE)
                .addScalar("outgoingStudents", IntegerType.INSTANCE)
                .addScalar("incomingStudents", IntegerType.INSTANCE)
                .addScalar("outgoingStaff", IntegerType.INSTANCE)
                .addScalar("incomingStaff", IntegerType.INSTANCE)
                .addScalar("broadIsced", StringType.INSTANCE)
                .addScalar("narrowIsced", StringType.INSTANCE)
                .addScalar("uploadMinAcademicYear", StringType.INSTANCE)
                .addScalar("uploadMaxAcademicYear", StringType.INSTANCE)
                .setResultTransformer(Transformers.aliasToBean(NationalUserReportRow.class));

        return query.list();
    }

    public AcademicYear getMostRecentAcademicYearByDataSheetCode(DataSheetCode dataSheetCode) {
        DataSheetRow mostRecentAcademicYearRow =
                dataSheetRowRepository.findTopByDataSheetCodeOrderByAcademicYearDesc(dataSheetCode);
        if (mostRecentAcademicYearRow == null) {
            return null;
        }

        return mostRecentAcademicYearRow.getAcademicYear();
    }

    public List<Object[]> getPartnerInstitutionsRankingAverages(AcademicYear academicYear) {
        Session session = getSession();

        SQLQuery query = session.createSQLQuery(RANKING_AVERAGES_QUERY);

        query.setParameter("dataSheet", DataSheetCode.RANKINGS.name())
                .setParameter("academicYear", academicYear.getAcademicYear());

        query.addScalar("PARTNER_INST_ID", LongType.INSTANCE)
                .addScalar("AVERAGE", DoubleType.INSTANCE);

        return query.list();
    }

    public String getRankingPosition(Institution institution, DataSheetColumnCode rankingColumnCode,
                                     AcademicYear academicYear) {
        return dataSheetRowRepository.getRankingPosition(institution, rankingColumnCode, academicYear);
    }

    @Transactional(readOnly = true)
    public int countRowsWithColumn(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                   Institution partnerInstitution, DataSheetColumnCode columnCode) {
        return dataSheetRowRepository.countRowsWithColumn(institution, academicYears, isceds == null, isceds,
                partnerInstitution, columnCode);
    }

    @Transactional(readOnly = true)
    public int countRowsWithColumnAndValue(Institution institution, List<AcademicYear> academicYears,
                                           List<Isced> isceds, Institution partnerInstitution,
                                           DataSheetColumnCode columnCode, DataSheetValueCode valueCode) {
        return dataSheetRowRepository.countRowsWithColumnAndValue(institution, academicYears, isceds == null, isceds,
                partnerInstitution, columnCode, valueCode);
    }

    @Transactional(readOnly = true)
    public int countEducationalProjects(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                        Institution partnerInstitution) {
        return dataSheetRowRepository.countDataSheetRows(institution, academicYears, isceds == null, isceds,
                partnerInstitution, DataSheetCode.EDUCATIONAL_PROJECTS);
    }

    @Transactional(readOnly = true)
    public int countJointProgrammes(Institution institution, List<AcademicYear> academicYears, List<Isced> isceds,
                                    Institution partnerInstitution) {
        return dataSheetRowRepository.countDataSheetRows(institution, academicYears, isceds == null, isceds,
                partnerInstitution, DataSheetCode.JOINT_PROGRAMMES);
    }

    @Transactional(readOnly = true)
    public int countJointProgrammesGraduates(Institution institution, List<AcademicYear> academicYears,
                                             List<Isced> isceds, Institution partnerInstitution) {
        Integer count =
                dataSheetRowRepository.countSumDataSheetRowColumns(institution, academicYears, isceds == null, isceds,
                        partnerInstitution, DataSheetColumnCode.GRADUATES_NUMBER);
        return count == null ? 0 : count;
    }
}
