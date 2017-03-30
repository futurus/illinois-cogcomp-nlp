package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.ERENerReader;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
/*import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;*/

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Properties;

public class ERENERTest {
    private static final String NAME = ERENERTest.class.getCanonicalName();

    public static void main(String[] args) throws IOException {
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

        TextAnnotation output = null;
        View nerEre = null;
        View ner = null;
        HashMap<Sentence, ArrayList<Constituent>> s2e = null;
        int nDoc = 5, i = 0;

        while (ereReader.hasNext() && i++ < nDoc) {
            output = ereReader.next();
            nerEre = output.getView(ViewNames.MENTION_ERE);
            s2e = new HashMap<> ();
            ArrayList<Constituent> l = null;

//            try {
//                NERAnnotator co = new NERAnnotator(ViewNames.NER_CONLL);
//                co.doInitialize();
//                co.addView(output);
//                System.out.println(output.getView(ViewNames.NER_CONLL));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            for (Constituent c : nerEre.getConstituents()) {
                Sentence s = output.getSentence(output.getSentenceId(c));

                if (s2e.containsKey(s)) {
                    l = s2e.get(s);

                } else {
                    l = new ArrayList<Constituent>();

                }
                l.add(c);
                s2e.put(s, l);
            }

            System.out.println(s2e.size());

            for (Sentence s : output.sentences()) {
                System.out.println("\"" + s + "\": ");

                if (s2e.get(s) != null) {
                    for (Constituent c : s2e.get(s)) {
                        System.out.print("\n\t'" + c.getSurfaceForm() + "'");
                        System.out.print("\n\t\t" + c.getAttribute("EntityMentionType"));
                        System.out.print("\n\t\t" + c.getAttribute("EntitySpecificity") + "\n");
                    }
                }

                System.out.println("------------------------------------");
            }


            System.out.println("------------------++++++------------------");
        }

//        helpful methods
//        TextAnnotationUtilities.printTextAnnotation(System.out, output);
    }
}
