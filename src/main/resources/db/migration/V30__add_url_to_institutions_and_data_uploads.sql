ALTER TABLE INSTITUTIONS
  ADD URL VARCHAR(200) CONSTRAINT INSTITUTION_URL_UK UNIQUE;

INSERT INTO DATA_SHEET_COLUMN_CODES (CODE) VALUES ('URL');

INSERT INTO DATA_SHEET_COLUMN_TYPES (CODE, VALUE_TYPE, IS_FIXED_VALUE, IS_MULTIPLE_CHOICE)
VALUES ('URL', 'STRING', 0, 0);

INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'INSTITUTIONS', 'URL', 'URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'SMS', 'URL', 'Receiving Organisation URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'SMP', 'URL', 'Receiving Organisation URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'RANKINGS', 'URL', 'Institution URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'STUDENTS_INCOMING', 'URL', 'Sending Organisation URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'STAFF_INCOMING', 'URL', 'Sending Organisation URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'STAFF_OUTGOING', 'URL', 'Receiving Organisation URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'INSTITUTIONAL_AGREEMENTS', 'URL', 'Partner institution URL');
INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE) VALUES ('URL', 'GRADING_TABLES', 'URL', 'Partner institution URL');