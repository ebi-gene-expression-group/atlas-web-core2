SET search_path=atlas3dev;

CREATE TABLE rnaseq_bsln_ce_profiles
(
  experiment VARCHAR(255) NOT NULL,
  identifier VARCHAR(255) NOT NULL,
  ce_identifiers TEXT NOT NULL,
  CONSTRAINT rnaseq_bsln_ce_profiles_pkey
  PRIMARY KEY (experiment, identifier)
);
