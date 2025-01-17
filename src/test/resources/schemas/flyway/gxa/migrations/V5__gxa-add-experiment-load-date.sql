ALTER TABLE experiment
ADD COLUMN load_date TIMESTAMP DEFAULT NOW();

UPDATE experiment
SET load_date = last_update;
