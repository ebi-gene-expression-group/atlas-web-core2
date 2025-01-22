package uk.ac.ebi.atlas.model.resource;

public class XmlFileConfigurationException extends RuntimeException {
    public XmlFileConfigurationException(String message) {
        super(message);
    }

    public XmlFileConfigurationException(Exception cause) {
        super(cause);
    }
}
