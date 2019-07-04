DELETE FROM DATA_SHEET_COLUMNS
WHERE
  CODE = 'SMS_FIND_ACCOMMODATION' AND
  TITLE =
  'How satisfied were you with the guidance you received from the receiving institution on how to find an accommodation?';

INSERT INTO DATA_SHEET_COLUMNS (CODE, DATA_SHEET_CODE, TYPE, TITLE)
VALUES ('SMS_FIND_ACCOMMODATION', 'SMS', 'SATISFIED',
        'How satisfied were you with the guidance you received from the receiving institution on how to find accommodation?')