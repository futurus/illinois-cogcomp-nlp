package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;

public class ERENERTest {
    private static final String NAME = ERENERTest.class.getCanonicalName();

    public static void main(String[] args) {
        ERENerReader ereReader = null;
        boolean addNominalMentions = true;
        String dataPath = "src/test/resources/ERE_NER/";

        try {
            ereReader = new ERENerReader("LDC2016E31", dataPath, addNominalMentions);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: " + NAME
                    + ": couldn't instantiate ERENerReader with corpus dir '" + dataPath + ": "
                    + e.getMessage());
        }

        TextAnnotation output = ereReader.next();
        View nerEre = null;
        if (addNominalMentions) {
            assert (output.hasView(ViewNames.MENTION_ERE));
            nerEre = output.getView(ViewNames.MENTION_ERE);
        } else {
            assert (output.hasView(ViewNames.NER_ERE));
            nerEre = output.getView(ViewNames.NER_ERE);
        }

        for (Constituent c : nerEre.getConstituents()) {
            System.out.println(c.getSurfaceForm());

            for (String k : c.getAttributeKeys()) {
                System.out.print(k + ": " + c.getAttribute(k) + "\n");
            }
            System.out.println("------------------------------------");
        }

//        helpful methods
//        TextAnnotationUtilities.printTextAnnotation(System.out, output);
    }
}
