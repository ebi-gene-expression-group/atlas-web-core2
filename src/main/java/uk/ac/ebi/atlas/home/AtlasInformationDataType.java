package uk.ac.ebi.atlas.home;

public enum AtlasInformationDataType {
  ENSEMBL("ensembl"),
  EG("ensembl_genomes"),
  WBPS("wormbase_parasite"),
  EFO("efo"),
  EFO_URL("efo_url");

  private final String id;

  AtlasInformationDataType(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
