ALTER TABLE experiment
RENAME COLUMN private TO private_old;

ALTER TABLE experiment
ADD COLUMN private BOOLEAN;

UPDATE experiment
SET private = TRUE
WHERE
 private_old = 'T';

UPDATE experiment
SET private = FALSE
WHERE
 private_old = 'F';

DROP VIEW public_experiment;

ALTER TABLE experiment
DROP COLUMN private_old;

CREATE VIEW public_experiment AS
 SELECT accession, type, last_update
 FROM experiment WHERE private=FALSE;
