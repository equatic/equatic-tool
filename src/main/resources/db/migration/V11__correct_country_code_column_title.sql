UPDATE DATA_SHEET_COLUMNS
SET TITLE = 'Receiving Organisation Country'
WHERE CODE = 'COUNTRY_CODE' AND DATA_SHEET_CODE IN ('SMS', 'SMP');

UPDATE DATA_SHEET_COLUMNS
SET TITLE = 'Institution Country'
WHERE CODE = 'COUNTRY_CODE' AND DATA_SHEET_CODE = 'RANKINGS';