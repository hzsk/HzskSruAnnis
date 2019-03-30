/**
 * @file HZSKSRUSearchResultSet.java
 * @copyright Hamburger Zentrum für Sprach Korpora 
 *      http://corpora.uni-hamburg.de
 */
package de.uni_hamburg.corpora.sru;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A very simple converter class. */
public class STTS2UDConverter {

    private static final Map<String, String> STTS2UD;
    private static final Map<String, List<String>> UD2STTS;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        // long
        aMap.put("ADJA", "ADJ");
        aMap.put("ADJD", "ADJ");
        aMap.put("ADV", "ADV");
        aMap.put("APPR", "ADP");
        aMap.put("APPRPART", "ADP"); // XXX: + 
        aMap.put("APPO", "ADP");
        aMap.put("APZR", "ADP");
        aMap.put("ART", "DET");
        aMap.put("CARD", "NUM");
        aMap.put("FM", "X");
        aMap.put("ITJ", "INTJ");
        aMap.put("KOUI", "SCONJ");
        aMap.put("KOUS", "SCONJ");
        aMap.put("KON", "CONJ");
        aMap.put("KOKOM", "SCONJ");
        aMap.put("NN", "NOUN");
        aMap.put("NE", "PROPN");
        aMap.put("PDS", "PRON");
        aMap.put("PDAT", "PRON");
        aMap.put("PIS", "PRON");
        aMap.put("PIAT", "PRON");
        aMap.put("PIDAT", "PRON");
        aMap.put("PPER", "PRON");
        aMap.put("PPOSS", "PRON");
        aMap.put("PRELS", "PRON");
        aMap.put("PPOSAT", "PRON");
        aMap.put("PRELAT", "PRON");
        aMap.put("PRF", "PRON");
        aMap.put("PWS", "PRON");
        aMap.put("PWAT", "PRON");
        aMap.put("PWAV", "PRON");
        aMap.put("PAV", "ADV");
        aMap.put("PTKZU", "PART");
        aMap.put("PTKNEG", "PART");
        aMap.put("PTKVZ", "PART");
        aMap.put("PTKANT", "PART");
        aMap.put("PTKA", "PART");
        aMap.put("TRUNC", "X");
        aMap.put("VVFIN", "VERB");
        aMap.put("VVIMP", "VERB");
        aMap.put("VVINF", "VERB");
        aMap.put("VVIZU", "VERB");
        aMap.put("VVPP", "VERB");
        aMap.put("VAFIN", "VERB");
        aMap.put("VAPP", "VERB");
        aMap.put("VMFIN", "VERB");
        aMap.put("VMPP", "VERB");
        aMap.put("XY", "X");
        aMap.put("$,", "PUNCT");
        aMap.put("$.", "PUNCT");
        aMap.put("$(", "PUNCT");
        // short
        aMap.put("A", "ADJ");
        aMap.put("N", "NOUN");
        aMap.put("V", "VERB");
        aMap.put("P", "PRON");
        aMap.put("AP", "ADP");
        aMap.put("PTK", "PART");
        STTS2UD = Collections.unmodifiableMap(aMap);
        Map<String, List<String>> bMap = new HashMap<String, List<String>>();
        bMap.put("ADJ", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("ADJA", "ADJD", "A"))));
        bMap.put("ADV", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("ADV", "PAV"))));
        bMap.put("ADP", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("APPR", "APPRPART", "APPO", "APZR", 
                            "AP"))));
        bMap.put("DET", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("ART"))));
        bMap.put("NUM", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("CARD"))));
        bMap.put("X", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("FM", "TRUNC", "XY"))));
        bMap.put("INTJ", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("ITJ"))));
        bMap.put("SCONJ", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("KOUI", "KOUS", "KOKOM"))));
        bMap.put("CONJ", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("KON"))));
        bMap.put("NOUN", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("NN", "N"))));
        bMap.put("PROPN", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("NE"))));
        bMap.put("PRON", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("PPER", "PPOSS", "PRELS", "PPOSAT",
                            "PRELAT", "PRF", "PWS", "PWAV", "PWAT", "P"))));
        bMap.put("PART", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("PTKZU", "PTKNEG", "PTKVZ", "PTKANT", 
                            "PTKA", "PTK"))));
        bMap.put("VERB", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("VVFIN", "VVIMP", "VVINF", "VVIZU",
                            "VVPP", "VAFIN", "VMFIN", "VMPP", "V"))));
        bMap.put("PUNCT", Collections.unmodifiableList(
                    new ArrayList<String>(
                        Arrays.asList("$(","$,", "$."))));
        UD2STTS = Collections.unmodifiableMap(bMap);

    }

    public static String fromSTTS(String STTS) {
        return STTS2UD.get(STTS);
    }

    public static List<String> toSTTS(String UD) {
        return UD2STTS.get(UD);
    }
}
