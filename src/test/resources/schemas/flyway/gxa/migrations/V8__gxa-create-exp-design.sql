CREATE TABLE exp_design_column
(
    experiment_accession VARCHAR(255) CHECK (experiment_accession LIKE 'E-%') NOT NULL,
    column_name VARCHAR(255) NOT NULL,
    sample_type VARCHAR(255) CHECK (sample_type IN ('characteristic', 'factor')) NOT NULL,
    column_order INTEGER NOT NULL,
    id serial NOT NULL
        CONSTRAINT exp_design_column_sequence_pk
            PRIMARY KEY,
    UNIQUE (experiment_accession, column_name, sample_type)
);

CREATE TABLE exp_design
(
    sample VARCHAR(255) NOT NULL,
    array_design VARCHAR(255),
    annot_value VARCHAR(255) NOT NULL,
    annot_ont_URI VARCHAR(255),
    exp_design_column_id INTEGER REFERENCES exp_design_column(id) ON DELETE CASCADE,
    UNIQUE (sample, exp_design_column_id)
);