
CREATE INDEX scxa_cell_group_marker_gene_stats_expression_type 
    ON scxa_cell_group_marker_gene_stats(expression_type);

CREATE INDEX scxa_cell_group_variable 
    ON scxa_cell_group(variable);

CREATE INDEX scxa_cell_group_membership_cell_group_id 
    ON scxa_cell_group_membership(cell_group_id);
