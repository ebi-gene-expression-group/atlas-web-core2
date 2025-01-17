package uk.ac.ebi.atlas.model.arraydesign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArrayDesignDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayDesignDao.class);
    private static final RowMapper<ArrayDesign> ARRAY_DESIGN_ROW_MAPPER =
            (rs, rowNum) -> ArrayDesign.create(rs.getString("accession"), rs.getString("name"));

    private final JdbcTemplate jdbcTemplate;

    public ArrayDesignDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_DESIGNELEMENT_WHERE_IDENTIFIER =
            "SELECT designelement FROM designelement_mapping WHERE identifier=?";
    @Cacheable("designElementsByGeneId")
    public List<String> getDesignElements(String geneIdentifier) {
        LOGGER.debug("Fetching design elements for identifier {}...", geneIdentifier);
        return jdbcTemplate.queryForList(SELECT_DESIGNELEMENT_WHERE_IDENTIFIER, String.class, geneIdentifier);
    }

    private static final String SELECT_ARRAYDESIGN_WHERE_ACCESSION =
            "SELECT * FROM arraydesign WHERE accession=?";
    @Cacheable("arrayDesignByAccession")
    public ArrayDesign getArrayDesign(String accession) {
        try {
            LOGGER.debug("Fetching array design for accession {}", accession);
            return jdbcTemplate.queryForObject(SELECT_ARRAYDESIGN_WHERE_ACCESSION, ARRAY_DESIGN_ROW_MAPPER, accession);
        } catch (IncorrectResultSizeDataAccessException e) {
            LOGGER.warn("Array design of accession {} could not be found", accession);
            return ArrayDesign.create(accession);
        }
    }
}
