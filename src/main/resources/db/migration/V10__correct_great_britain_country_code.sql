INSERT INTO COUNTRIES(CODE, NAME) VALUES ('GB', 'United Kingdom of Great Britain and Northern Ireland');

UPDATE INSTITUTIONS SET COUNTRY_CODE = 'GB' WHERE COUNTRY_CODE = 'UK';

DELETE FROM COUNTRIES WHERE CODE = 'UK';