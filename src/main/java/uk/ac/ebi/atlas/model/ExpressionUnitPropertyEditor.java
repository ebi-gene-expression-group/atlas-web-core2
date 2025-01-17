package uk.ac.ebi.atlas.model;

import java.beans.PropertyEditorSupport;

public class ExpressionUnitPropertyEditor extends PropertyEditorSupport {
   @Override
    public void setAsText(String text)  {
        if (text.equalsIgnoreCase("relative abundance")) {
            setValue(ExpressionUnit.Absolute.Protein.RA);
        } else {
            setValue(ExpressionUnit.Absolute.Protein.PPB);
        }
    }
}
