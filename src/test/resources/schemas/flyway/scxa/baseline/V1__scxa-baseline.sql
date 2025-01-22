CREATE TABLE scxa_analytics
(
	experiment_accession VARCHAR(255) NOT NULL,
	gene_id VARCHAR(255) NOT NULL,
	cell_id VARCHAR(255) NOT NULL,
	expression_level DOUBLE PRECISION
) PARTITION BY LIST (experiment_accession);

CREATE TABLE scxa_cell_clusters
(
	experiment_accession VARCHAR(255) NOT NULL,
	cell_id VARCHAR(255) NOT NULL,
	k INTEGER NOT NULL,
	cluster_id INTEGER NOT NULL,
	CONSTRAINT scxa_cell_clusters_experiment_accession_cell_id_k_pk
		PRIMARY KEY (experiment_accession, k, cell_id)
);

CREATE TABLE scxa_experiment
(
	accession VARCHAR(255) NOT NULL,
	type VARCHAR(50) NOT NULL,
	species VARCHAR(255) NOT NULL,
	access_key CHAR(36) NOT NULL,
	private BOOLEAN DEFAULT TRUE,
	last_update TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
	pubmed_ids VARCHAR(255),
	title VARCHAR(500),
	dois VARCHAR(255)
);

CREATE TABLE scxa_marker_genes
(
	experiment_accession VARCHAR(255) NOT NULL,
	gene_id VARCHAR(255) NOT NULL,
	k INTEGER NOT NULL,
	cluster_id INTEGER NOT NULL,
	marker_probability DOUBLE PRECISION NOT NULL,
	CONSTRAINT scxa_marker_genes_experiment_accession_gene_id_k_pk
		PRIMARY KEY (experiment_accession, gene_id, k)
);

CREATE TABLE scxa_tsne
(
	experiment_accession VARCHAR(255) NOT NULL,
	cell_id VARCHAR(255) NOT NULL,
	x DOUBLE PRECISION,
	y DOUBLE PRECISION,
	perplexity INTEGER NOT NULL,
	CONSTRAINT scxa_tsne_experiment_accession_cell_id_perplexity_pk
		PRIMARY KEY (experiment_accession, cell_id, perplexity)
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

CREATE VIEW scxa_public_experiment AS
 SELECT accession, type, last_update
   FROM scxa_experiment
  WHERE private IS FALSE;
