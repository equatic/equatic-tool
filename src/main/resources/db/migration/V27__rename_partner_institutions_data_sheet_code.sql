INSERT INTO DATA_SHEETS (CODE) VALUES ('INSTITUTIONAL_AGREEMENTS');

UPDATE DATA_SHEET_COLUMNS SET DATA_SHEET_CODE = 'INSTITUTIONAL_AGREEMENTS' WHERE DATA_SHEET_CODE = 'PARTNER_INSTITUTIONS';
UPDATE DATA_SHEET_ROWS SET DATA_SHEET_CODE = 'INSTITUTIONAL_AGREEMENTS' WHERE DATA_SHEET_CODE = 'PARTNER_INSTITUTIONS';
UPDATE DATA_SHEET_UPLOADS SET DATA_SHEET_CODE = 'INSTITUTIONAL_AGREEMENTS' WHERE DATA_SHEET_CODE = 'PARTNER_INSTITUTIONS';

DELETE FROM DATA_SHEETS WHERE CODE = 'PARTNER_INSTITUTIONS';