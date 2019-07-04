ALTER TABLE INSTITUTIONS ADD DISPLAY_NAME VARCHAR2(200);

UPDATE INSTITUTIONS SET DISPLAY_NAME = COALESCE(NAME_EN, NAME);

ALTER TABLE INSTITUTIONS MODIFY DISPLAY_NAME VARCHAR2(200) NOT NULL;
ALTER TABLE INSTITUTIONS ADD CONSTRAINT INST_DISPLAY_NAME_COUNTRY_UK UNIQUE (DISPLAY_NAME, COUNTRY_CODE);