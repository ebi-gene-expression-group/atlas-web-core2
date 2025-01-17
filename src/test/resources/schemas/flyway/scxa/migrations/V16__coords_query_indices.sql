CREATE INDEX scxa_cell_group_membership_cell_id 
    ON scxa_cell_group_membership(cell_id);

CREATE INDEX scxa_coords_experiment_accession_method 
    ON scxa_coords(experiment_accession, method);
