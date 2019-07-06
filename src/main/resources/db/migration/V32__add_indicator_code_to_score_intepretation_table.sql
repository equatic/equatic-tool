DELETE FROM SCORE_INTERPRETATIONS;

ALTER TABLE SCORE_INTERPRETATIONS
  DROP CONSTRAINT SCORE_INTERP_INST_UK;

ALTER TABLE SCORE_INTERPRETATIONS
  ADD
  (
  INDICATOR_CODE VARCHAR2(100) NOT NULL,
  CONSTRAINT SCORE_INTERP_INST_INDICATOR_UK UNIQUE (INST_ID, INDICATOR_CODE)
  );