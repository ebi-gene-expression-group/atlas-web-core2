-- Create a table holding info on a dimension reduction, most of which was previously in scxa_coords 

CREATE TABLE scxa_dimension_reduction
(
    id SERIAL not null PRIMARY KEY,
    experiment_accession VARCHAR(255) NOT NULL,
    method VARCHAR(255) NOT NULL,
    parameterisation JSONB DEFAULT NULL,
    priority SMALLINT DEFAULT 0 NOT NULL,
    UNIQUE (experiment_accession, method, parameterisation)  
);

CREATE INDEX scxa_dimred_experiment_accession_method_param_priority ON scxa_dimension_reduction(experiment_accession, method, parameterisation, priority);
CREATE INDEX scxa_dimred_id ON scxa_dimension_reduction(id);

-- Populate with data from the old table

INSERT INTO scxa_dimension_reduction (experiment_accession, method, parameterisation) 
  SELECT DISTINCT experiment_accession, method, parameterisation FROM scxa_coords;  

-- Add new column

ALTER TABLE scxa_coords
  ADD COLUMN dimension_reduction_id SMALLINT REFERENCES scxa_dimension_reduction(id) ON DELETE CASCADE;

-- Add the index for the new foreign key on the coords table

CREATE INDEX scxa_coords_dimension_reduction_id ON scxa_coords(dimension_reduction_id);

-- Add the correct references to the new table

UPDATE scxa_coords 
SET dimension_reduction_id = sdr.id
FROM scxa_dimension_reduction AS sdr
WHERE 
  scxa_coords.experiment_accession = sdr.experiment_accession AND
  scxa_coords.method = sdr.method AND
  scxa_coords.parameterisation = sdr.parameterisation
;

-- Remove those columns from scxa_coords

ALTER TABLE scxa_coords
  DROP COLUMN experiment_accession,
  DROP COLUMN method,
  DROP COLUMN parameterisation;

-- Make capitalisation explicit for dimension reductions

UPDATE scxa_dimension_reduction SET method = 't-SNE' WHERE method = 'tsne';
UPDATE scxa_dimension_reduction SET method = 'UMAP' WHERE method = 'umap';

-- Add an expression units column on experiemnt

ALTER TABLE experiment
    ADD COLUMN expression_unit VARCHAR(10) NOT NULL DEFAULT 'CPM';

CREATE INDEX experiment_expression_units
    ON experiment(expression_unit);
