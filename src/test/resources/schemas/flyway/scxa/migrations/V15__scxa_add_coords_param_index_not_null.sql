ALTER TABLE scxa_coords DROP COLUMN parameterisation;
ALTER TABLE scxa_coords RENAME COLUMN json_parameterisation TO parameterisation;

ALTER TABLE scxa_coords ALTER COLUMN parameterisation SET NOT NULL;

CREATE INDEX scxa_coords_parameterisation ON scxa_coords using gin(parameterisation);
