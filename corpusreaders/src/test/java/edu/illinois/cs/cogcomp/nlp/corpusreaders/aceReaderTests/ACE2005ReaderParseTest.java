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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class ACE2005ReaderParseTest {

    public static final String ACE2005CORPUS = "src/test/resources/ACE_2005/data/English";

    @Test
    public void test2005Dataset() throws Exception {
        String corpusHomeDir = ACE2005CORPUS;
        ACEReader reader = new ACEReader(corpusHomeDir, false);
        testReaderParse(reader, corpusHomeDir, 60);
    }

    private void testReaderParse(ACEReader reader, String corpusHomeDir, int numberOfDocs)
            throws XMLException {
        int numDocs = 0;
        ReadACEAnnotation.is2004mode = reader.Is2004Mode();
        String corpusIdGold = reader.Is2004Mode() ? "ACE2004" : "ACE2005";
        StringBuilder sectionJSON = new StringBuilder();

        sectionJSON.append("{");
        sectionJSON.append("\n\t\"documents\": [");

        StringBuilder docJSON = new StringBuilder();

        while (reader.hasNext()) {
            TextAnnotation doc = reader.next();

            SpanLabelView coreferenceView = (SpanLabelView) doc.getView(ViewNames.MENTION_ACE);
//            CoreferenceView coreferenceView = (CoreferenceView) doc.getView(ViewNames.COREF_HEAD);
            HashMap<Sentence, ArrayList<Constituent>> s2e = new HashMap<>();
            String[] attrs = new String[]{"EntityType", "EntityMentionType", "EntityMentionLDCType", "EntitySubtype"};

            for (Constituent c : coreferenceView.getConstituents()) {
                if (!c.getAttribute("EntityType").equals("PER")
                        || !c.getAttribute("EntitySubtype").equals("Individual"))
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

            docJSON.append("\n\t\t{");
            docJSON.append("\n\t\t\t\"documentId\": \"" + doc.getId() + "\", ");
            docJSON.append("\n\t\t\t\"sentences\": [");

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
                    senJSON.append("\n\t\t\t\t\t\"contituents\": [");

                    StringBuilder consJSON = new StringBuilder();
                    for (Constituent c : list) {
                        consJSON.append("\n\t\t\t\t\t\t{");
                        consJSON.append("\n\t\t\t\t\t\t\t\"constituent\": \"" + c.getSurfaceForm().replace("\"", "'") + "\",");
                        consJSON.append("\n\t\t\t\t\t\t\t\"startCharOffset\": " + c.getStartCharOffset() + ",");
                        consJSON.append("\n\t\t\t\t\t\t\t\"endCharOffset\": " + c.getEndCharOffset() + ",");

                        for (String attr : attrs) { //c.getAttributeKeys()
                            consJSON.append("\n\t\t\t\t\t\t\t\"" + attr + "\": \"" + c.getAttribute(attr) + "\", ");
                        }
                        consJSON.deleteCharAt(consJSON.lastIndexOf(","));
                        consJSON.append("\n\t\t\t\t\t\t},");
                    }
                    consJSON.deleteCharAt(consJSON.lastIndexOf(","));
                    senJSON.append(consJSON);
                    senJSON.append("\n\t\t\t\t\t]");
                    senJSON.append("\n\t\t\t\t},");
                }

//                System.out.println("\n\n------------------------------------\n");
            }
            if (senJSON.length() > 0 && senJSON.lastIndexOf(",") > -1) {
                senJSON.deleteCharAt(senJSON.lastIndexOf(","));
            }
            docJSON.append(senJSON);
            docJSON.append("\n\t\t\t]");
            docJSON.append("\n\t\t},");

//            System.out.println(docJSON);

            // VU
//            TextAnnotationUtilities.printTextAnnotation(System.out, doc);

            /*ACEDocumentAnnotation annotation =
                    ReadACEAnnotation
                            .readDocument(corpusHomeDir + File.separatorChar + doc.getId());*/

            /*Set<String> documentViews = doc.getAvailableViews();
            assertTrue(documentViews.contains(ViewNames.TOKENS));
            assertTrue(documentViews.contains(ViewNames.MENTION_ACE));
            assertTrue(documentViews.contains(ViewNames.COREF_HEAD));
            assertTrue(documentViews.contains(ViewNames.COREF_EXTENT));*/

//            SpanLabelView entityView = (SpanLabelView) doc.getView(ViewNames.MENTION_ACE);
//            CoreferenceView coreferenceView = (CoreferenceView) doc.getView(ViewNames.COREF_HEAD);
//            CoreferenceView coreferenceExtentView = (CoreferenceView) doc.getView(ViewNames.COREF_EXTENT);

            /*int relationMentions = 0;
            for (ACERelation relation : annotation.relationList) {
                relationMentions += relation.relationMentionList.size();
            }
            assertEquals(entityView.getRelations().size(), relationMentions);*/

            // Sort entityMention annotation based on their extent starts
            /*Collections.sort(entityMentionList, new Comparator<ACEEntityMention>() {
                @Override
                public int compare(ACEEntityMention o1, ACEEntityMention o2) {
                    return Integer.compare(o1.extentStart, o2.extentStart);
                }
            });*/

            numDocs++;
            if (numDocs == numberOfDocs) break;
        }
        if (docJSON.length() > 0 && docJSON.lastIndexOf(",") > -1) {
            docJSON.deleteCharAt(docJSON.lastIndexOf(","));
        }
        sectionJSON.append(docJSON);
        sectionJSON.append("\n\t]");
        sectionJSON.append("\n}");
        System.out.println(sectionJSON);
    }
}
