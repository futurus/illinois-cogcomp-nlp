/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 * <p>
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReaderTests;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.XMLException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.documentReader.ReadACEAnnotation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ACE2005 {

    private static final String FILENAME = "test.json";

    public static final String ACE2005CORPUS = "src/test/resources/ACE_2005/data/English";

    public static HashMap<Sentence, ArrayList<Constituent>> sen2Entities(TextAnnotation doc,
                                                                         SpanLabelView coreferenceView,
                                                                         String entityType,
                                                                         String entitySubtype) {
        HashMap<Sentence, ArrayList<Constituent>> s2e = new HashMap<>();

        for (Constituent c : coreferenceView.getConstituents()) {
            if (!c.getAttribute("EntityType").equals(entityType)
                    || !c.getAttribute("EntitySubtype").equals(entitySubtype))
                continue;

            Sentence s = doc.getSentence(doc.getSentenceId(c));
            ArrayList<Constituent> l = null;

            if (s2e.containsKey(s)) {
                l = s2e.get(s);

            } else {
                l = new ArrayList<Constituent>();

            }
            l.add(c);
            s2e.put(s, l);
        }

        return s2e;
    }

    public static String allPairs(ArrayList<Constituent> list, String[] attrs) {
        StringBuilder consJSON = new StringBuilder();

        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i; j < list.size(); j++) {
                if (!list.get(i).getSurfaceForm().equals(list.get(j).getSurfaceForm())) {
                    consJSON.append("\n\t\t\t\t\t\t{");
                    consJSON.append("\n\t\t\t\t\t\t\t\"constituent1\": {");
                    consJSON.append("\n\t\t\t\t\t\t\t\t\"surfaceForm\": \"" + list.get(i).getSurfaceForm().replace("\"", "'") + "\",");
                    consJSON.append("\n\t\t\t\t\t\t\t\t\"startCharOffset\": " + list.get(i).getStartCharOffset() + ",");
                    consJSON.append("\n\t\t\t\t\t\t\t\t\"endCharOffset\": " + list.get(i).getEndCharOffset() + ",");

                    for (String attr : attrs) { //c.getAttributeKeys()
                        consJSON.append("\n\t\t\t\t\t\t\t\t\"" + attr + "\": \"" + list.get(i).getAttribute(attr) + "\", ");
                    }
                    consJSON.deleteCharAt(consJSON.lastIndexOf(","));
                    consJSON.append("\n\t\t\t\t\t\t\t},");

                    consJSON.append("\n\t\t\t\t\t\t\t\"constituent2\": {");
                    consJSON.append("\n\t\t\t\t\t\t\t\t\"surfaceForm\": \"" + list.get(j).getSurfaceForm().replace("\"", "'") + "\",");
                    consJSON.append("\n\t\t\t\t\t\t\t\t\"startCharOffset\": " + list.get(j).getStartCharOffset() + ",");
                    consJSON.append("\n\t\t\t\t\t\t\t\t\"endCharOffset\": " + list.get(j).getEndCharOffset() + ",");

                    for (String attr : attrs) { //c.getAttributeKeys()
                        consJSON.append("\n\t\t\t\t\t\t\t\t\"" + attr + "\": \"" + list.get(j).getAttribute(attr) + "\", ");
                    }
                    consJSON.deleteCharAt(consJSON.lastIndexOf(","));
                    consJSON.append("\n\t\t\t\t\t\t\t},");
                    consJSON.append("\n\t\t\t\t\t\t\t\"compatibility\": \"true\"");
                    consJSON.append("\n\t\t\t\t\t\t},");
                }
            }
        }
        if (consJSON.length() > 0 && consJSON.lastIndexOf(",") > -1) {
            consJSON.deleteCharAt(consJSON.lastIndexOf(","));
        }

        return consJSON.toString();
    }

    public static String sentenceJSON(TextAnnotation doc, HashMap<Sentence, ArrayList<Constituent>> s2e, String[] attrs) {

        StringBuilder senJSON = new StringBuilder();
        for (Sentence s : doc.sentences()) {
            ArrayList<Constituent> list = s2e.get(s);

            if (list != null && list.size() >= 2) {
                Collections.sort(list, new Comparator<Constituent>() {
                    @Override
                    public int compare(Constituent o1, Constituent o2) {
                        int o1h = Integer.parseInt(o1.getAttribute("EntityHeadStartCharOffset"));
                        int o2h = Integer.parseInt(o2.getAttribute("EntityHeadStartCharOffset"));
                        return Integer.compare(o1h, o2h);
                    }
                });

                senJSON.append("\n\t\t\t\t{");
                senJSON.append("\n\t\t\t\t\t\"sentence\": \"" + s.getText().replace("\"", "'") + "\",");
                senJSON.append("\n\t\t\t\t\t\"sentenceId\": " + s.getSentenceId() + ",");
                senJSON.append("\n\t\t\t\t\t\"pairs\": [");
                senJSON.append(allPairs(list, attrs));
                senJSON.append("\n\t\t\t\t\t]");
                senJSON.append("\n\t\t\t\t},");
            }

        }
        if (senJSON.length() > 0 && senJSON.lastIndexOf(",") > -1) {
            senJSON.deleteCharAt(senJSON.lastIndexOf(","));
        }

        return senJSON.toString();
    }

    public static String docJSON(ACEReader reader, int numberOfDocs) {
        int numDocs = 0;
        StringBuilder docJSON = new StringBuilder();

        while (reader.hasNext()) {
            TextAnnotation doc = reader.next();

            SpanLabelView coreferenceView = (SpanLabelView) doc.getView(ViewNames.MENTION_ACE);

            String[] attrs = new String[]{"EntityMentionType", "EntityMentionLDCType"};

            HashMap<Sentence, ArrayList<Constituent>> s2e = sen2Entities(doc, coreferenceView, "PER", "Individual");

            docJSON.append("\n\t\t{");
            docJSON.append("\n\t\t\t\"documentId\": \"" + doc.getId() + "\", ");
            docJSON.append("\n\t\t\t\"sentences\": [");
            docJSON.append(sentenceJSON(doc, s2e, attrs));
            docJSON.append("\n\t\t\t]");
            docJSON.append("\n\t\t},");

            numDocs++;
            if (numDocs == numberOfDocs) break;
        }
        if (docJSON.length() > 0 && docJSON.lastIndexOf(",") > -1) {
            docJSON.deleteCharAt(docJSON.lastIndexOf(","));
        }

        return docJSON.toString();
    }

    private static void testReaderParse(ACEReader reader,
                                        String corpusHomeDir,
                                        int numberOfDocs) throws XMLException {
        ReadACEAnnotation.is2004mode = reader.Is2004Mode();
        String corpusIdGold = reader.Is2004Mode() ? "ACE2004" : "ACE2005";
        StringBuilder sectionJSON = new StringBuilder();

        sectionJSON.append("{");
        sectionJSON.append("\n\t\"EntityType\": \"PER\",");
        sectionJSON.append("\n\t\"EntitySubtype\": \"Individual\",");
        sectionJSON.append("\n\t\"documents\": [");
        sectionJSON.append(docJSON(reader, numberOfDocs));
        sectionJSON.append("\n\t]");
        sectionJSON.append("\n}");
//        System.out.println(sectionJSON);

        // Output
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(FILENAME);
            bw = new BufferedWriter(fw);
            bw.write(sectionJSON.toString());
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String corpusHomeDir = ACE2005CORPUS;
        ACEReader reader = new ACEReader(corpusHomeDir, false);
        testReaderParse(reader, corpusHomeDir, 9); //60
    }
}
