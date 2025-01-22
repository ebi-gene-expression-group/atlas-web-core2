ALTER TABLE experiment ADD species VARCHAR(127) NULL;

UPDATE experiment
SET species = (SELECT organism FROM experiment_organism
               WHERE experiment.accession = experiment_organism.experiment);

DROP TABLE experiment_organism;
