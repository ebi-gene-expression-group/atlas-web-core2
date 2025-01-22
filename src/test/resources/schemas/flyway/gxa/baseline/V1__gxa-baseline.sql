CREATE TABLE experiment
(
	accession VARCHAR(255),
	type VARCHAR(50),
	access_key CHAR(36),
	private VARCHAR(1),
	last_update TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
	pubmed_ids VARCHAR(255),
	title VARCHAR(500),
	dois VARCHAR(255)
);

CREATE TABLE experiment_organism
(
	organism VARCHAR(255),
	experiment VARCHAR(255),
	bioentity_organism VARCHAR(1020)
);

CREATE TABLE rnaseq_bsln_ce_profiles
(
	experiment VARCHAR(255),
	identifier VARCHAR(255),
	ce_identifiers TEXT
);

CREATE TABLE arraydesign
(
  accession VARCHAR(255) NOT NULL
    CONSTRAINT arraydesign_pkey
    PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE designelement_mapping
(
  designelement VARCHAR(255) NOT NULL,
  identifier VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL,
  arraydesign VARCHAR(255) NOT NULL
    CONSTRAINT fk_dem_arraydesign
    REFERENCES arraydesign
    ON DELETE CASCADE
);

CREATE VIEW public_experiment AS
	SELECT accession, type, last_update
	FROM experiment WHERE private='F';
