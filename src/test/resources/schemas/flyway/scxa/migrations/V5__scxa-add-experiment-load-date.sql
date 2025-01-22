ALTER TABLE scxa_experiment
ADD COLUMN load_date TIMESTAMP DEFAULT NOW();

UPDATE scxa_experiment
SET load_date = last_update;
