CREATE TABLE scxa_coords
(
    experiment_accession VARCHAR(255) NOT NULL,
    method VARCHAR(255) NOT NULL,
    cell_id VARCHAR(255) NOT NULL,
    x DOUBLE PRECISION,
    y DOUBLE PRECISION,
    parameterisation VARCHAR(255) NOT NULL,
    CONSTRAINT scxa_coords_experiment_accession_method_cell_id_params_pk
        PRIMARY KEY (experiment_accession, method, cell_id, parameterisation)
);

CREATE INDEX scxa_coords_experiment_accession_method_param ON scxa_coords(experiment_accession, method, parameterisation)
