-- Institutions --
INSERT INTO INSTITUTIONS
(ID, PIC, ERASMUS_CODE, NAME, NAME_EN, DISPLAY_NAME, COUNTRY_CODE, IDP_ENTITY_ID, IDP_METADATA_URL)
VALUES
  (INSTITUTIONS_SEQ.NEXTVAL, '999986096', 'B  GENT01', 'Universiteit Gent', 'Ghent University', 'Ghent University',
   'BE', NULL, NULL);

INSERT INTO INSTITUTIONS
(ID, PIC, ERASMUS_CODE, NAME, NAME_EN, DISPLAY_NAME, COUNTRY_CODE, IDP_ENTITY_ID, IDP_METADATA_URL)
VALUES
  (INSTITUTIONS_SEQ.NEXTVAL, '999572294', 'PL WARSZAW01', 'Uniwersytet Warszawski', 'University of Warsaw',
   'University of Warsaw', 'PL',
   'https://idp.logowanie.uw.edu.pl/simplesaml/', 'https://idp.logowanie.uw.edu.pl/simplesaml/saml2/idp/metadata.php');

INSERT INTO INSTITUTIONS
(ID, PIC, ERASMUS_CODE, NAME, NAME_EN, DISPLAY_NAME, COUNTRY_CODE, IDP_ENTITY_ID, IDP_METADATA_URL)
VALUES
  (INSTITUTIONS_SEQ.NEXTVAL, NULL, NULL, 'Okta', NULL, 'Okta', 'GB', 'http://www.okta.com/exk5i2auor62BMS8V0h7',
   'https://dev-472907.oktapreview.com/app/exk5i2auor62BMS8V0h7/sso/saml/metadata');

INSERT INTO INSTITUTIONS
(ID, PIC, ERASMUS_CODE, NAME, NAME_EN, DISPLAY_NAME, COUNTRY_CODE, IDP_ENTITY_ID, IDP_METADATA_URL)
VALUES
  (INSTITUTIONS_SEQ.NEXTVAL, '999873188', 'A  GRAZ01', 'Karl-Franzens-Universität', 'Graz University of Graz',
   'Graz University of Graz', 'AT', NULL, NULL);

-- Super admin: change email for TST --

UPDATE USERS SET EMAIL = 'equatic-test@lists.ugent.be' WHERE USERNAME = 'superadmin';

-- UGent --

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'ugentlocal',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Universiteit Gent'),
   'UGent', 'Institutional admin', 'Paul.Leys@UGent.be', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

INSERT INTO USER_ROLES
(ID, ROLE, USER_ID)
VALUES
  (USER_ROLES_SEQ.NEXTVAL,
   'ROLE_ADMIN_INSTITUTIONAL',
   (SELECT USERS.ID
    FROM USERS
    WHERE USERS.USERNAME = 'ugentlocal'));

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'ugentlocal2',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Universiteit Gent'),
   'UGent', 'Institutional admin2', 'Mario.Maccarini@UGent.be', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

INSERT INTO USER_ROLES
(ID, ROLE, USER_ID)
VALUES
  (USER_ROLES_SEQ.NEXTVAL,
   'ROLE_ADMIN_INSTITUTIONAL',
   (SELECT USERS.ID
    FROM USERS
    WHERE USERS.USERNAME = 'ugentlocal2'));

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'ugentuser',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Universiteit Gent'),
   'UGent', 'User', 'michalk@mimuw.edu.pl', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

-- UW --

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'uwlocal',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Uniwersytet Warszawski'),
   'UW', 'Institutional admin', 'equatic-test@lists.ugent.be', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

INSERT INTO USER_ROLES
(ID, ROLE, USER_ID)
VALUES
  (USER_ROLES_SEQ.NEXTVAL,
   'ROLE_ADMIN_INSTITUTIONAL',
   (SELECT USERS.ID
    FROM USERS
    WHERE USERS.USERNAME = 'uwlocal'));

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'uwuser',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Uniwersytet Warszawski'),
   'UW', 'User', 'michalk@mimuw.edu.pl', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

-- SSO --

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'oktalocal',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Okta'),
   'SSO', 'Institutional admin', 'equatic-test@lists.ugent.be', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

INSERT INTO USER_ROLES
(ID, ROLE, USER_ID)
VALUES
  (USER_ROLES_SEQ.NEXTVAL,
   'ROLE_ADMIN_INSTITUTIONAL',
   (SELECT USERS.ID
    FROM USERS
    WHERE USERS.USERNAME = 'oktalocal'));

INSERT INTO USERS
(ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES
  (USERS_SEQ.NEXTVAL, 'michalk',
   (SELECT INSTITUTIONS.ID
    FROM INSTITUTIONS
    WHERE INSTITUTIONS.NAME = 'Okta'),
   'Michał', 'Kurzydłowski', 'michalk@mimuw.edu.pl', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

INSERT INTO ACADEMIC_YEARS (ACADEMIC_YEAR) VALUES ('2013-2014');
INSERT INTO ACADEMIC_YEARS (ACADEMIC_YEAR) VALUES ('2014-2015');
INSERT INTO ACADEMIC_YEARS (ACADEMIC_YEAR) VALUES ('2015-2016');

COMMIT;