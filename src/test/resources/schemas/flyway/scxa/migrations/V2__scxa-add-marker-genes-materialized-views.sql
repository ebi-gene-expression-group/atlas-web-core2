-- Create table with top 5 marker genes per cluster for each k value
CREATE MATERIALIZED VIEW scxa_top_5_marker_genes_per_cluster AS
  (SELECT *
   FROM (SELECT ROW_NUMBER() OVER (PARTITION BY experiment_accession, k, cluster_id ORDER BY marker_probability ASC) AS r,
                markers.*
         FROM (SELECT * FROM scxa_marker_genes) markers) x
   WHERE x.r <= 5)
WITH DATA;

-- Create table with average expression per k and cluster ID for top 5 marker genes/cluster
CREATE MATERIALIZED VIEW scxa_marker_gene_stats AS
  (SELECT aggregated.experiment_accession,
          aggregated.gene_id,
          aggregated.k_where_marker,
          aggregated.cluster_id_where_marker,
          aggregated.cluster_id,
          aggregated.marker_p_value,
          avg(aggregated.expression_level) AS mean_expression,
          percentile_cont(0.5) WITHIN GROUP (
          ORDER BY aggregated.expression_level
          )                                AS median_expression
   FROM (SELECT analytics.experiment_accession,
                analytics.gene_id,
                clusters.cluster_id,
                markers.k                  AS k_where_marker,
                markers.cluster_id         AS cluster_id_where_marker,
                markers.marker_probability AS marker_p_value,
                analytics.expression_level
         FROM scxa_analytics AS analytics
                JOIN scxa_top_5_marker_genes_per_cluster AS markers
                  ON analytics.experiment_accession = markers.experiment_accession
                       AND analytics.gene_id = markers.gene_id
                JOIN scxa_cell_clusters AS clusters ON analytics.experiment_accession = clusters.experiment_accession
                                                         AND analytics.cell_id = clusters.cell_id
                                                         AND clusters.k = markers.k) AS aggregated
   GROUP BY aggregated.experiment_accession, aggregated.gene_id,
            aggregated.k_where_marker, aggregated.cluster_id_where_marker,
            aggregated.cluster_id,
            aggregated.marker_p_value)
WITH DATA;