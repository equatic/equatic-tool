INSERT INTO COUNTRIES(CODE, NAME) VALUES ('GR', 'Greece');

UPDATE INSTITUTIONS SET COUNTRY_CODE = 'GR' WHERE COUNTRY_CODE = 'EL';

DELETE FROM COUNTRIES WHERE CODE = 'EL';