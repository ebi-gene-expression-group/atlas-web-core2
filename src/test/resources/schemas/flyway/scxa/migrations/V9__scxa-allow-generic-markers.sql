
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
