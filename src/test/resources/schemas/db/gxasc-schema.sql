SET search_path = atlas3dev;

CREATE TABLE scxa_marker_genes
(
    gene_id              VARCHAR(255) NOT NULL,
    experiment_accession VARCHAR(255) NOT NULL,
    k                    INTEGER      NOT NULL,
    cluster_id           INTEGER      NOT NULL,
    marker_probability   DOUBLE PRECISION,
    CONSTRAINT marker_genes_pkey
        PRIMARY KEY (gene_id, experiment_accession, k, cluster_id)
);

CREATE TABLE scxa_tsne
(
    experiment_accession VARCHAR(255) NOT NULL,
    cell_id              VARCHAR(255) NOT NULL,
    x                    DOUBLE PRECISION,
    y                    DOUBLE PRECISION,
    perplexity           INTEGER      NOT NULL,
    CONSTRAINT scxa_tsne_experiment_accession_cell_id_perplexity_pk
        PRIMARY KEY (experiment_accession, cell_id, perplexity)
);

CREATE TABLE scxa_analytics
(
    experiment_accession VARCHAR(255) NOT NULL,
    gene_id              VARCHAR(255) NOT NULL,
    cell_id              VARCHAR(255) NOT NULL,
    expression_level     DOUBLE PRECISION,
    CONSTRAINT scxa_analytics_gene_id_experiment_accession_cell_id_pk
        PRIMARY KEY (gene_id, experiment_accession, cell_id)
);

CREATE TABLE scxa_cell_clusters
(
    experiment_accession VARCHAR(255) NOT NULL,
    cell_id              VARCHAR(255) NOT NULL,
    k                    INTEGER      NOT NULL,
    cluster_id           INTEGER      NOT NULL,
    CONSTRAINT scxa_cell_clusters_experiment_accession_k_cell_id_pk
        PRIMARY KEY (experiment_accession, k, cell_id)
);

-- This table replaces the materialised view used in the Postgres DB
CREATE TABLE scxa_marker_gene_stats
(
    experiment_accession    VARCHAR(255)     NOT NULL,
    gene_id                 VARCHAR(255)     NOT NULL,
    k_where_marker          INTEGER          NOT NULL,
    cluster_id_where_marker INTEGER          NOT NULL,
    cluster_id              INTEGER          NOT NULL,
    marker_p_value          DOUBLE PRECISION NOT NULL,
    mean_expression         DOUBLE PRECISION NOT NULL,
    median_expression       DOUBLE PRECISION NOT NULL,
    CONSTRAINT marker_gene_stats_pkey
        PRIMARY KEY (experiment_accession, gene_id, k_where_marker, cluster_id)
);

CREATE TABLE collections
(
    coll_id     VARCHAR(255) NOT NULL
        CONSTRAINT collections_pk
            PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    icon        bytea
);

CREATE TABLE experiment2collection
(
    exp_acc VARCHAR NOT NULL
        CONSTRAINT experiment2collection_experiment_accession_fk
            REFERENCES experiment
            ON DELETE CASCADE,
    coll_id VARCHAR NOT NULL
        CONSTRAINT experiment2collection_collections_coll_id_fk
            REFERENCES collections
            ON DELETE CASCADE,
    CONSTRAINT experiment2collection_pk
        PRIMARY KEY (exp_acc, coll_id)
);
-- Marker genes new scxa group tables
--------------------------------------------------------------------------------------------
CREATE TABLE scxa_cell_group
(
  id SERIAL not null PRIMARY KEY,
  experiment_accession VARCHAR(255) NOT NULL references experiment(accession) ON DELETE CASCADE,
  variable VARCHAR(255) NOT NULL,
  value VARCHAR(255) NOT NULL,
  UNIQUE (experiment_accession, variable, value)
);

CREATE TABLE scxa_cell_group_membership
(
  experiment_accession VARCHAR(255) NOT NULL,
  cell_id VARCHAR(255) NOT NULL,
  cell_group_id integer references scxa_cell_group(id) ON DELETE CASCADE,
  CONSTRAINT scxa_cell_groups_experiment_accession_cell_id_cell_group_id_pk
    PRIMARY KEY (experiment_accession, cell_id, cell_group_id)
);

CREATE TABLE scxa_cell_group_marker_genes
(
  id SERIAL not null PRIMARY KEY,
  gene_id VARCHAR(255) NOT NULL,
  cell_group_id integer references scxa_cell_group(id) ON DELETE CASCADE,
  marker_probability DOUBLE PRECISION,
  UNIQUE (gene_id, cell_group_id)
);

-- Create a table from data prepared during bundling, replacing the previously used materialised view

CREATE TABLE scxa_cell_group_marker_gene_stats
(
  gene_id VARCHAR(255) NOT NULL,
  cell_group_id integer references scxa_cell_group(id) ON DELETE CASCADE,
  marker_id integer references scxa_cell_group_marker_genes(id) ON DELETE CASCADE,
  expression_type smallint NOT NULL,
  mean_expression DOUBLE PRECISION NOT NULL,
  median_expression DOUBLE PRECISION NOT NULL,
  UNIQUE (gene_id, cell_group_id, marker_id, expression_type)
);
