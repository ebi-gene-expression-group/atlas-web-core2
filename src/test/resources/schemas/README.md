# Expression Atlas schemas

Schemas for the database and SolrCloud servers used by [Expression Atlas](https://www.ebi.ac.uk/gxa) and [Single Cell
Expression Atlas](https://www.ebi.ac.uk/gxa/sc).

This is a central repository used by the web application to bootstrap the embedded database and Solr server in the
testing environment, and to initialise a fresh (PostgreSQL) database and SolrCloud cluster to be used by either flavour
of Expression Atlas.

In the `db` directory there is a `shared-schema.sql` file that is needed by both Expression Atlas and Single Cell
Expression Atlas, used to display array designs and design elements of a gene in the [bioentity information
card](https://github.com/ebi-gene-expression-group/atlas-bioentity-information).

## Migrations

We use [Flyway](https://flywaydb.org/) to version-control both single cell and bulk Expression Atlas databases. To
apply new migrations to a database, either from `gxa` or `scxa`, applied to bulk Expression Atlas and Single Cell
Expression Atlas, respectively, run the following command (fill in the variables as needed):
```bash
flyway migrate -url=jdbc:postgresql://${HOST}:5432/${DB} -user=${USER} -password=${PASSWORD} -locations=filesystem:`pwd`
```

The [`info`](https://flywaydb.org/documentation/command/info) and
[`repair`](https://flywaydb.org/documentation/command/repair) commands are helpful to troubleshoot issues (e.g.
checksum mismatches).

### How to use it without having to deal with dependencies

You can use flyway as installed in the `quay.io/ebigxa/atlas-schemas-base` container
(this is the same container used to run the tests). You will need:

- A checkout of this repo
- Mount the location of the flyway/[gxa|scxa] directory inside the container
- Specify the database connection

This is achieved like this (make sure to replace db_host and db_name):

```
# go to the root directory of this repo
export APP=gxa
export DB_CONNECTION_URL=jdbc:postgresql://db_host:5432/db_name
export USER=<db_user>
export PASSWORD=<db_password>
# Make sure you fill in the above ^^^^^^
export LOCATION_IN_CONTAINER=/flyway/$APP
docker run -it -v $( pwd )/flyway/$APP:$LOCATION_IN_CONTAINER \
  quay.io/ebigxa/atlas-schemas-base \
  flyway migrate -url=$DB_CONNECTION_URL -user=${USER} -password=${PASSWORD} -locations=filesystem:$LOCATION_IN_CONTAINER
```

Note that changes in this repo don't require changes in that container unless that
we need a new flyway version.

## Common queries

### Select all experiments for a gene

```
SELECT
    experiment_accession
FROM
    scxa_cell_group_marker_genes m
    JOIN scxa_cell_group g ON m.cell_group_id=g.id
    JOIN experiment e ON g.experiment_accession = e.accession
WHERE
    e.private=FALSE AND
    m.gene_id=':gene_id'
GROUP BY experiment_accession
```

### Select clusters for significant genes

```
SELECT
    variable as k,
    value as cluster_id
FROM
    scxa_cell_group g
    JON scxa_cell_group_marker_genes m ON g.id = m.cell_group_id
WHERE(
    m.gene_id=':gene_id'
    AND marker_probability < :threshold AND
    g.experiment_accession=':experiment_accession' AND (
      g.variable=':preferred_K' OR
      m.marker_probability IN (
        SELECT
          MIN(marker_probability)
        FROM
          scxa_cell_group,
          scxa_cell_group_marker_genes
        WHERE
          experiment_accession = ':experiment_accession' AND
          gene_id=':gene_id'
        )
    )
)
```

### Select coordinates with expression of a given gene

```
SELECT
    c.cell_id,
    c.x,
    c.y,
    a.expression_level
FROM
    scxa_coords c  
    LEFT JOIN scxa_analytics a ON
        c.experiment_accession=a.experiment_accession AND
        c.cell_id=a.cell_id AND
        a.gene_id=':gene_id'
WHERE
    c.experiment_accession=':experiment_accession' AND
    c.method=':method' AND
    c.parameterisation->0->>'perplexity'=':perplexity'
```

### Select coordinates with a cell group membership

```
  SELECT
    c.cell_id,
    c.x,
    c.y,
    g.value as cluster_id
FROM
    scxa_cell_group g
    JOIN scxa_cell_group_membership m ON
        g.id = m.cell_group_id AND
        g.experiment_accession = ':experiment_accession' AND
        g.variable = ':variable'
    RIGHT JOIN scxa_coords c ON
        m.cell_id=c.cell_id AND
        m.experiment_accession=c.experiment_accession
    WHERE
        c.method=':method' AND
        c.parameterisation->0->>'perplexity'=':perplexity' AND
        c.experiment_accession=':experiment_accession'
```

Starting from the cell group and right-joining to get the coordinates ensures we get coordinates for all cells, even those without cell group memberships. The experiment accession does appear to be required in both parts of the above query.

### Summary stats for cell group markers

```
SELECT
  g.experiment_accession,
  m.gene_id,
  g.variable as k_where_marker,
  h.value as cluster_id_where_marker,
  g.value as cluster_id,
  m.marker_probability as marker_p_value,
  s.mean_expression, s.median_expression
FROM
 	scxa_cell_group_marker_gene_stats s
  JOIN scxa_cell_group g on s.cell_group_id=g.id
  JOIN scxa_cell_group_marker_genes m ON s.marker_id=m.id
  JOIN scxa_cell_group h ON m.cell_group_id = h.id
WHERE
  g.experiment_accession=':experiment_accession' and
  m.marker_probability < 0.05 and
  g.variable = ':k' and
  expression_type=0
ORDER BY m.marker_probability
```

Note: The marker stats table references the cell group table twice, once directly, once indirectly via the marker table (which records the precise grouping in which a gene was a marker).

### Find cell groupings with significant markers

```
SELECT DISTINCT
  h.variable as k_where_marker
FROM
  scxa_cell_group_marker_genes m,
  JOIN scxa_cell_group h ON m.cell_group_id = h.id
WHERE
  h.experiment_accession=':experiment_accession'
  and m.marker_probability < 0.05
ORDER BY k_where_marker ASC
