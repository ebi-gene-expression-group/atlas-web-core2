package uk.ac.ebi.atlas.resource;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.ebi.atlas.commons.readers.MatrixMarketReader;
import uk.ac.ebi.atlas.commons.readers.TsvStreamer;
import uk.ac.ebi.atlas.commons.readers.XmlReader;
import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import uk.ac.ebi.atlas.commons.writers.TsvWriter;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.model.resource.Directory;
import uk.ac.ebi.atlas.model.resource.MatrixMarketFile;
import uk.ac.ebi.atlas.model.resource.TsvFile;
import uk.ac.ebi.atlas.model.resource.XmlFile;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Named
public class DataFileHub {
    protected final Path experimentsMageTabDirLocation;
    protected final Path experimentsAdminDirLocation;
    protected final Path experimentDesignDirPath;

    protected static final String EXPERIMENT_DESIGN_FILE_PATH_TEMPLATE = "ExpDesign-{0}.tsv";
    static final String OP_LOG_FILE_PATH_TEMPLATE = "{0}-op-log.tsv";

    protected static final String CONFIGURATION_FILE_PATH_TEMPLATE = "{0}/{0}-configuration.xml";
    static final String ANALYSIS_METHODS_FILE_PATH_TEMPLATE = "{0}/{0}-analysis-methods.tsv";
    protected static final String CONDENSED_SDRF_FILE_PATH_TEMPLATE = "{0}/{0}.condensed-sdrf.tsv";
    protected static final String SDRF_FILE_PATH_TEMPLATE = "{0}/{0}.sdrf.txt";
    protected static final String IDF_FILE_PATH_TEMPLATE = "{0}/{0}.idf.txt";
    protected static final String SUMMARY_PDF_FILE_PATH_TEMPLATE = "{0}/{0}{1}Summary_ExpressionAtlas{2}pdf";

    protected static final String PROTEOMICS_PARAMETER_FILE_PATH_TEMPLATE = "{0}/{0}.mqpar.xml";
    protected static final String PROTEOMICS_RAW_MAX_QUANT_PATH_TEMPLATE = "{0}/{0}-proteinGroups.txt";


    protected static final String PROTEOMICS_BASELINE_EXPRESSION_FILE_PATH_TEMPLATE = "{0}/{0}.tsv";
    protected static final String RNASEQ_BASELINE_FPKMS_FILE_PATH_TEMPLATE = "{0}/{0}-fpkms.tsv";
    protected static final String RNASEQ_BASELINE_TPMS_FILE_PATH_TEMPLATE = "{0}/{0}-tpms.tsv";
    protected static final String RNASEQ_BASELINE_TRANSCRIPTS_TPMS_FILE_PATH_TEMPLATE = "{0}/{0}-transcripts-tpms.tsv";

    protected static final String FACTORS_FILE_PATH_TEMPLATE = "{0}/{0}-factors.xml";
    protected static final String DIFFERENTIAL_ANALYTICS_FILE_PATH_TEMPLATE = "{0}/{0}-analytics.tsv";
    protected static final String DIFFERENTIAL_PERCENTILE_RANKS_FILE_PATH_TEMPLATE = "{0}/{0}-percentile-ranks.tsv";
    static final String DIFFERENTIAL_RAW_COUNTS_FILE_PATH_TEMPLATE = "{0}/{0}-raw-counts.tsv";
    static final String QC_DIRECTORY_PATH_TEMPLATE = "{0}/qc";
    static final String MICROARRAY_ANALYTICS_FILE_PATH_TEMPLATE = "{0}/{0}_{1}-analytics.tsv";
    static final String MICROARRAY_NORMALIZED_EXPRESSIONS_FILE_PATH_TEMPLATE =
            "{0}/{0}_{1}-normalized-expressions.tsv";
    static final String MICROARRAY_LOG_FOLD_CHANGES_FILE_PATH_TEMPLATE = "{0}/{0}_{1}-log-fold-changes.tsv";
    static final String COEXPRESSION_FILE_TEMPLATE = "{0}/{0}-coexpressions.tsv.gz";

    protected static final String REACTOME_PATHWAYS_FILE_PATH_TEMPLATE = "{0}/{0}.{1}.reactome.gsea.tsv";

    // Single cell files
    protected static final String SINGLE_CELL_MATRIX_MARKET_TPMS_FILE_PATH_TEMPLATE = "{0}/{0}.expression_tpm.mtx";
    protected static final String SINGLE_CELL_MATRIX_MARKET_TPMS_GENE_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_TPMS_FILE_PATH_TEMPLATE + "_rows";
    protected static final String SINGLE_CELL_MATRIX_MARKET_TPMS_CELL_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_TPMS_FILE_PATH_TEMPLATE + "_cols";

    private static final String SINGLE_CELL_MATRIX_MARKET_RAW_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE =
            "{0}/{0}.aggregated_counts.mtx";
    private static final String SINGLE_CELL_MATRIX_MARKET_RAW_AGGREGATED_COUNTS_GENE_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_RAW_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE + "_rows";
    private static final String SINGLE_CELL_MATRIX_MARKET_RAW_AGGREGATED_COUNTS_CELL_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_RAW_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE + "_cols";

    private static final String SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE =
            "{0}/{0}.aggregated_filtered_normalised_counts.mtx";
    private static final String SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_GENE_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE + "_rows";
    private static final String SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_CELL_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE + "_cols";

    private static final String SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE =
            "{0}/{0}.aggregated_filtered_counts.mtx";
    private static final String SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_GENE_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE + "_rows";
    private static final String SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_CELL_IDS_FILE_PATH_TEMPLATE =
            SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE + "_cols";

    protected static final String SINGLE_CELL_T_SNE_PLOT_FILE_PATH_TEMPLATE = "{0}/{0}.tsne_perp_{1}.tsv";
    protected static final String SINGLE_CELL_MARKER_GENES_FILE_PATH_TEMPLATE = "{0}/{0}.marker_genes_{1}.tsv";
    protected static final String SINGLE_CELL_CLUSTERS_FILE_PATH_TEMPLATE = "{0}/{0}.clusters.tsv";
    protected static final String SINGLE_CELL_SOFTWARE_USED_FILE_PATH_TEMPLATE = "{0}/{0}.software.tsv";
    protected static final String SINGLE_CELL_HDF5_PATH_TEMPLATE = "{0}/{0}.project.h5ad";

    @Inject
    public DataFileHub(Path experimentsDirPath, Path experimentDesignDirPath) {
        this.experimentsMageTabDirLocation = experimentsDirPath.resolve("magetab");
        this.experimentsAdminDirLocation = experimentsDirPath.resolve("admin");
        this.experimentDesignDirPath = experimentDesignDirPath;
    }

    public Path getExperimentMageTabDirLocation() {
        return experimentsMageTabDirLocation;
    }

    public ExperimentFiles getExperimentFiles(String experimentAccession) {
        return new ExperimentFiles(experimentAccession);
    }

    public BaselineExperimentFiles getBaselineExperimentFiles(String experimentAccession) {
        return new BaselineExperimentFiles(experimentAccession);
    }

    public DifferentialExperimentFiles getDifferentialExperimentFiles(String experimentAccession) {
        return new DifferentialExperimentFiles(experimentAccession);
    }

    public RnaSeqBaselineExperimentFiles getRnaSeqBaselineExperimentFiles(String experimentAccession) {
        return new RnaSeqBaselineExperimentFiles(experimentAccession);
    }

    public ProteomicsBaselineExperimentFiles getProteomicsBaselineExperimentFiles(String experimentAccession) {
        return new ProteomicsBaselineExperimentFiles(experimentAccession);
    }

    public BulkDifferentialExperimentFiles getBulkDifferentialExperimentFiles(String experimentAccession) {
        return new BulkDifferentialExperimentFiles(experimentAccession);
    }

    public MicroarrayExperimentFiles getMicroarrayExperimentFiles(String experimentAccession, String arrayDesign) {
        return new MicroarrayExperimentFiles(experimentAccession, arrayDesign);
    }

    public SingleCellExperimentFiles getSingleCellExperimentFiles(String experimentAccession) {
        return new SingleCellExperimentFiles(experimentAccession);
    }

    public class ExperimentFiles {
        public final AtlasResource<TsvStreamer> analysisMethods;
        public final AtlasResource<XmlReader> configuration;
        public final AtlasResource<TsvStreamer> condensedSdrf;
        public final AtlasResource<TsvStreamer> sdrf;
        public final AtlasResource<TsvStreamer> idf;
        public final AtlasResource<Set<Path>> qcFolder;
        public final AtlasResource<TsvStreamer> experimentDesign;
        public final AtlasResource<TsvWriter> experimentDesignWrite;
        public final AtlasResource<TsvStreamer> adminOpLog;
        public final AtlasResource<TsvWriter> adminOpLogWrite;
        public final AtlasResource<Set<Path>> summaryPdf;

        ExperimentFiles(String experimentAccession) {
            analysisMethods =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation, ANALYSIS_METHODS_FILE_PATH_TEMPLATE, experimentAccession);
            configuration =
                    new XmlFile.ReadOnly(
                            experimentsMageTabDirLocation, CONFIGURATION_FILE_PATH_TEMPLATE, experimentAccession);
            condensedSdrf =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation, CONDENSED_SDRF_FILE_PATH_TEMPLATE, experimentAccession);

            sdrf =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation, SDRF_FILE_PATH_TEMPLATE, experimentAccession);

            idf =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation, IDF_FILE_PATH_TEMPLATE, experimentAccession);
            qcFolder =
                    new Directory(
                            experimentsMageTabDirLocation, QC_DIRECTORY_PATH_TEMPLATE, experimentAccession);

            experimentDesign =
                    new TsvFile.ReadOnly(
                            experimentDesignDirPath,
                            EXPERIMENT_DESIGN_FILE_PATH_TEMPLATE,
                            experimentAccession);
            experimentDesignWrite =
                    new TsvFile.Overwrite(
                            experimentDesignDirPath,
                            EXPERIMENT_DESIGN_FILE_PATH_TEMPLATE,
                            experimentAccession);

            adminOpLog =
                    new TsvFile.ReadOnly(experimentsAdminDirLocation, OP_LOG_FILE_PATH_TEMPLATE, experimentAccession);
            adminOpLogWrite =
                    new TsvFile.Overwrite(experimentsAdminDirLocation, OP_LOG_FILE_PATH_TEMPLATE, experimentAccession);

            summaryPdf = retrieveSummaryPdfFilePath(experimentAccession, SUMMARY_PDF_FILE_PATH_TEMPLATE);
        }
        // Retrieves summary pdf files with - and _
        private AtlasResource<Set<Path>> retrieveSummaryPdfFilePath(String experimentAccession, String filePathTemplate) {
            var summaryPdfPathTemplate =
                    experimentsMageTabDirLocation.resolve(
                            MessageFormat.format(filePathTemplate, experimentAccession, "(\\S+)", "(\\S+)"));

            var summaryPdfPathFileRegex = Pattern.compile(summaryPdfPathTemplate.getFileName().toString());

            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(summaryPdfPathTemplate.getParent())) {
                for (Path filePath : dirStream) {
                    var matcher = summaryPdfPathFileRegex.matcher(filePath.getFileName().toString());
                    if (matcher.matches()) {
                        return new Directory(experimentsMageTabDirLocation, filePathTemplate, experimentAccession, matcher.group(1), matcher.group(2));
                    }
                }

            } catch (IOException e) {
                // log warning, the set will be empty, the caller decides what to do
            }
            return new Directory(experimentsMageTabDirLocation, filePathTemplate, experimentAccession, "");
        }
    }

    public class BaselineExperimentFiles {
        public final AtlasResource<XmlReader> factors;
        public final AtlasResource<CSVReader> coexpressions;

        BaselineExperimentFiles(String experimentAccession) {
            factors =
                    new XmlFile.ReadOnly(
                            experimentsMageTabDirLocation, FACTORS_FILE_PATH_TEMPLATE, experimentAccession);
            coexpressions =
                    new TsvFile.ReadCompressed(
                            experimentsMageTabDirLocation, COEXPRESSION_FILE_TEMPLATE, experimentAccession);
        }
    }

    public class DifferentialExperimentFiles {
        public final AtlasResource<ObjectInputStream<String[]>> percentileRanks;

        DifferentialExperimentFiles(String experimentAccession) {
            percentileRanks =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            DIFFERENTIAL_PERCENTILE_RANKS_FILE_PATH_TEMPLATE,
                            experimentAccession);
        }

        public AtlasResource<TsvStreamer> reactomePathwaysFiles(String experimentAccession, String comparison) {
            return new TsvFile.ReadOnly(
                    experimentsMageTabDirLocation,
                    REACTOME_PATHWAYS_FILE_PATH_TEMPLATE,
                    experimentAccession,
                    comparison);
        }
    }

    public class RnaSeqBaselineExperimentFiles {
        public final ExperimentFiles experimentFiles;
        public final BaselineExperimentFiles baselineExperimentFiles;

        public final AtlasResource<ObjectInputStream<String[]>> fpkms;
        public final AtlasResource<ObjectInputStream<String[]>> tpms;
        public final AtlasResource<ObjectInputStream<String[]>> transcriptsTpms;

        RnaSeqBaselineExperimentFiles(String experimentAccession) {
            experimentFiles = new ExperimentFiles(experimentAccession);
            baselineExperimentFiles = new BaselineExperimentFiles(experimentAccession);

            fpkms =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            RNASEQ_BASELINE_FPKMS_FILE_PATH_TEMPLATE,
                            experimentAccession);
            tpms =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            RNASEQ_BASELINE_TPMS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            transcriptsTpms =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            RNASEQ_BASELINE_TRANSCRIPTS_TPMS_FILE_PATH_TEMPLATE,
                            experimentAccession);
            }

        public AtlasResource<ObjectInputStream<String[]>> dataFile(ExpressionUnit.Absolute.Rna unit) {
            switch (unit) {
                case FPKM:
                    return fpkms;
                case TPM:
                    return tpms;
                default:
                    throw new IllegalArgumentException(String.format("No file for: %s", unit));
            }
        }

        public ImmutableList<ExpressionUnit.Absolute.Rna> dataFiles() {
            ImmutableList.Builder<ExpressionUnit.Absolute.Rna> b = ImmutableList.builder();
            if (tpms.exists()) {
                b.add(ExpressionUnit.Absolute.Rna.TPM);
            }
            if (fpkms.exists()) {
                b.add(ExpressionUnit.Absolute.Rna.FPKM);
            }
            return b.build();
        }
    }

    public class ProteomicsBaselineExperimentFiles {
        public final ExperimentFiles experimentFiles;
        public final BaselineExperimentFiles baselineExperimentFiles;

        public final AtlasResource<ObjectInputStream<String[]>> main;

        ProteomicsBaselineExperimentFiles(String experimentAccession) {
            experimentFiles = new ExperimentFiles(experimentAccession);
            baselineExperimentFiles = new BaselineExperimentFiles(experimentAccession);

            main =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            PROTEOMICS_BASELINE_EXPRESSION_FILE_PATH_TEMPLATE,
                            experimentAccession);
            }
    }

    public class BulkDifferentialExperimentFiles {
        public final ExperimentFiles experimentFiles;
        public final DifferentialExperimentFiles differentialExperimentFiles;

        public final AtlasResource<ObjectInputStream<String[]>> analytics;
        public final AtlasResource<TsvStreamer> rawCounts;
        public final AtlasResource<ObjectInputStream<String[]>> parameterFile;
        public final AtlasResource<ObjectInputStream<String[]>> rawMaxQuant;

        BulkDifferentialExperimentFiles(String experimentAccession) {
            experimentFiles = new ExperimentFiles(experimentAccession);
            differentialExperimentFiles = new DifferentialExperimentFiles(experimentAccession);

            analytics =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            DIFFERENTIAL_ANALYTICS_FILE_PATH_TEMPLATE,
                            experimentAccession);
            rawCounts =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            DIFFERENTIAL_RAW_COUNTS_FILE_PATH_TEMPLATE,
                            experimentAccession);
            parameterFile =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            PROTEOMICS_PARAMETER_FILE_PATH_TEMPLATE,
                            experimentAccession);
            rawMaxQuant =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            PROTEOMICS_RAW_MAX_QUANT_PATH_TEMPLATE,

                            experimentAccession);
        }
    }

    public class MicroarrayExperimentFiles {
        public final ExperimentFiles experimentFiles;
        public final DifferentialExperimentFiles differentialExperimentFiles;

        public final AtlasResource<ObjectInputStream<String[]>> analytics;
        public final AtlasResource<TsvStreamer> normalizedExpressions;    // Microarray 1-colour specific
        public final AtlasResource<TsvStreamer> logFoldChanges;           // Microarray 2-colour specific

        MicroarrayExperimentFiles(String experimentAccession, String arrayDesign) {
            experimentFiles = new ExperimentFiles(experimentAccession);
            differentialExperimentFiles = new DifferentialExperimentFiles(experimentAccession);

            analytics =
                    new TsvFile.ReadAsStream(
                            experimentsMageTabDirLocation,
                            MICROARRAY_ANALYTICS_FILE_PATH_TEMPLATE,
                            experimentAccession,
                            arrayDesign);

            normalizedExpressions =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            MICROARRAY_NORMALIZED_EXPRESSIONS_FILE_PATH_TEMPLATE,
                            experimentAccession,
                            arrayDesign);

            logFoldChanges =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            MICROARRAY_LOG_FOLD_CHANGES_FILE_PATH_TEMPLATE,
                            experimentAccession,
                            arrayDesign);
        }
    }

    public class SingleCellExperimentFiles {
        public final ExperimentFiles experimentFiles;

        public final AtlasResource<TsvStreamer> softwareUsed;
        public final AtlasResource<MatrixMarketReader> tpmsMatrix;
        public final AtlasResource<TsvStreamer> geneIdsTsv;
        public final AtlasResource<TsvStreamer> cellIdsTsv;
        public final AtlasResource<MatrixMarketReader> filteredCountsMatrix;
        public final AtlasResource<TsvStreamer> filteredCountsGeneIdsTsv;
        public final AtlasResource<TsvStreamer> filteredCountsCellIdsTsv;
        public final AtlasResource<MatrixMarketReader> normalisedCountsMatrix;
        public final AtlasResource<TsvStreamer> normalisedCountsGeneIdsTsv;
        public final AtlasResource<TsvStreamer> normalisedCountsCellIdsTsv;
        public final AtlasResource<TsvStreamer> clustersTsv;
        public final Map<Integer, AtlasResource<TsvStreamer>> tSnePlotTsvs;
        public final Map<String, AtlasResource<TsvStreamer>> markerGeneTsvs;
        public final AtlasResource<TsvStreamer>  projectHdf5;

        SingleCellExperimentFiles(String experimentAccession) {
            experimentFiles = new ExperimentFiles(experimentAccession);

            softwareUsed = new TsvFile.ReadOnly(
                    experimentsMageTabDirLocation,
                    SINGLE_CELL_SOFTWARE_USED_FILE_PATH_TEMPLATE,
                    experimentAccession);

            clustersTsv = new TsvFile.ReadOnly(
                    experimentsMageTabDirLocation,
                    SINGLE_CELL_CLUSTERS_FILE_PATH_TEMPLATE,
                    experimentAccession);

            tpmsMatrix =
                    new MatrixMarketFile(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_TPMS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            geneIdsTsv =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_TPMS_GENE_IDS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            cellIdsTsv =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_TPMS_CELL_IDS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            filteredCountsMatrix =
                    new MatrixMarketFile(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE,
                            experimentAccession);
            filteredCountsGeneIdsTsv =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_GENE_IDS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            filteredCountsCellIdsTsv =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_FILTERED_AGGREGATED_COUNTS_CELL_IDS_FILE_PATH_TEMPLATE,
                            experimentAccession);


            normalisedCountsMatrix =
                    new MatrixMarketFile(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_FILE_PATH_TEMPLATE,
                            experimentAccession);
            normalisedCountsGeneIdsTsv =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_GENE_IDS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            normalisedCountsCellIdsTsv =
                    new TsvFile.ReadOnly(
                            experimentsMageTabDirLocation,
                            SINGLE_CELL_MATRIX_MARKET_NORMALISED_AGGREGATED_COUNTS_CELL_IDS_FILE_PATH_TEMPLATE,
                            experimentAccession);

            tSnePlotTsvs = retrieveIntegersFromFileNames(experimentAccession, SINGLE_CELL_T_SNE_PLOT_FILE_PATH_TEMPLATE).stream()
                    .collect(
                            Collectors.toMap(
                                    perplexity -> perplexity,
                                    perplexity -> new TsvFile.ReadOnly(
                                            experimentsMageTabDirLocation,
                                            SINGLE_CELL_T_SNE_PLOT_FILE_PATH_TEMPLATE,
                                            experimentAccession,
                                            perplexity.toString())));

            markerGeneTsvs = retrieveStringsFromFileNames(experimentAccession, SINGLE_CELL_MARKER_GENES_FILE_PATH_TEMPLATE).stream()
                    .collect(
                            Collectors.toMap(
                                    k -> k,
                                    k -> new TsvFile.ReadOnly(
                                            experimentsMageTabDirLocation,
                                            SINGLE_CELL_MARKER_GENES_FILE_PATH_TEMPLATE,
                                            experimentAccession,
                                            k.toString())));
            projectHdf5 = new TsvFile.ReadOnly(
                    experimentsMageTabDirLocation,
                    SINGLE_CELL_HDF5_PATH_TEMPLATE,
                    experimentAccession);
        }

//        public AtlasResource<MatrixMarketReader> dataFile(ExpressionUnit.Absolute.Rna unit) {
//            switch(unit) {
//                case TPM:
//                    return tpms;
//                default:
//                    throw new RuntimeException("No file for " + unit);
//            }
//        }
        // Retrieves cell type marker gene files with - and _
        private Set<String> retrieveStringsFromFileNames(String experimentAccession, String filePathTemplate) {
            var markerGeneFilePathTemplate =
                    experimentsMageTabDirLocation.resolve(
                            MessageFormat.format(filePathTemplate, experimentAccession, "(\\S+)"));

            var markerGeneTsvFileRegex = Pattern.compile(markerGeneFilePathTemplate.getFileName().toString());

            ImmutableSet.Builder<String> stringValues = ImmutableSet.builder();
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(markerGeneFilePathTemplate.getParent())) {

                for (Path filePath : dirStream) {
                    Matcher matcher = markerGeneTsvFileRegex.matcher(filePath.getFileName().toString());
                    if (matcher.matches()) {
                        stringValues.add(matcher.group(1));
                    }
                }

            } catch (IOException e) {
                // log warning, the set will be empty, the caller decides what to do
            }
            return stringValues.build();
        }

        // Retrieves k or perplexity values from single cell file names
        private Set<Integer> retrieveIntegersFromFileNames(String experimentAccession, String filePathTemplate) {
            var tSnePlotFilePathTemplate =
                    experimentsMageTabDirLocation.resolve(
                            MessageFormat.format(filePathTemplate, experimentAccession, "(\\d+)"));

            var tSnePlotTsvFileRegex = Pattern.compile(tSnePlotFilePathTemplate.getFileName().toString());

            ImmutableSet.Builder<Integer> integerValues = ImmutableSet.builder();
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(tSnePlotFilePathTemplate.getParent())) {

                for (Path filePath : dirStream) {
                    Matcher matcher = tSnePlotTsvFileRegex.matcher(filePath.getFileName().toString());
                    if (matcher.matches()) {
                        integerValues.add(Integer.parseInt(matcher.group(1)));
                    }
                }

            } catch (IOException e) {
                // log warning, the set will be empty, the caller decides what to do
            }
            return integerValues.build();
        }
    }
}
