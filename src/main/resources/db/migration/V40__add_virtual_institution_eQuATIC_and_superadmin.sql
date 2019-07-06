INSERT INTO INSTITUTIONS
(ID, PIC, ERASMUS_CODE, NAME, NAME_EN, DISPLAY_NAME, COUNTRY_CODE, IDP_ENTITY_ID, IDP_METADATA_URL, VIRTUAL)
VALUES (INSTITUTIONS_SEQ.NEXTVAL, NULL, NULL, 'eQuATIC', 'eQuATIC', 'eQuATIC', 'BE', NULL, NULL, 1);

INSERT INTO USERS (ID, USERNAME, INST_ID, FIRSTNAME, LASTNAME, EMAIL, PASSWORD, EMAIL_CONFIRMED, TOKEN, ACTIVATED)
VALUES (USERS_SEQ.NEXTVAL, 'superadmin',
        (SELECT INSTITUTIONS.ID FROM INSTITUTIONS WHERE INSTITUTIONS.NAME = 'eQuATIC'),
        'Super', 'Admin', 'paul.leys@ugent.be', '$2a$10$iElxa01A.JKJdQj6v61sJO7GOXmSYpwD2S92R9oCflvGyd7nB17ka', 1, NULL, 1);

INSERT INTO USER_ROLES (ID, ROLE, USER_ID)
VALUES (USER_ROLES_SEQ.NEXTVAL, 'ROLE_ADMIN_SUPER',
        (SELECT USERS.ID FROM USERS WHERE USERS.USERNAME = 'superadmin'
          and INST_ID = (SELECT INSTITUTIONS.ID FROM INSTITUTIONS WHERE INSTITUTIONS.NAME = 'eQuATIC')));