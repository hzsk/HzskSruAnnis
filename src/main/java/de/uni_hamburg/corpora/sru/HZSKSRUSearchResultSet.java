/**
 * @file HZSKSRUSearchResultSet.java
 * @copyright Hamburger Zentrum für Sprach Korpora
 *      http://corpora.uni-hamburg.de
 */
package de.uni_hamburg.corpora.sru;

import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.SRUResultCountPrecision;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUDiagnostic;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUVersion;
import eu.clarin.sru.server.fcs.XMLStreamWriterHelper;
import eu.clarin.sru.server.fcs.AdvancedDataViewWriter;

import java.util.NoSuchElementException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * HZSK implementation of search results in FCS / SRU server.
 * Uses database backend, but could use something else just as well.
 * It's just an object to wrap DBSearchResults together with
 * SRUSearchResultSet.
 */
public class HZSKSRUSearchResultSet extends SRUSearchResultSet {

    private static final String FCS_NS = "http://clarin.eu/fcs/resource";
    private static final String CLARIN_FCS_RECORD_SCHEMA = FCS_NS;

    private int pos = -1;
    private AdvancedSearchResultSet advancedResult = null;
    private boolean fcs2 = false;
    private boolean sru12 = false;
    private boolean sru11 = false;
    private boolean sru10 = false;

    /** Create HZSK SRU search results passing diagnostics up. */
    public HZSKSRUSearchResultSet(SRUDiagnosticList diagnostics) {
        super(diagnostics);
    }

    /** Create a result set from db query results and relay diagnostics. */
    public HZSKSRUSearchResultSet(SRUDiagnosticList diagnostics,
            AdvancedSearchResultSet dbsr) {
        super(diagnostics);
        advancedResult = dbsr;
    }

    /** Create a result set from db query results and relay diagnostics. */
    public HZSKSRUSearchResultSet(SRUDiagnosticList diagnostics,
            AdvancedSearchResultSet dbsr,
            SRURequest req) {
        super(diagnostics);
        advancedResult = dbsr;
        if (req.isVersion(SRUVersion.VERSION_2_0)) {
            fcs2 = true;
        } else {
            fcs2 = false;
        }
    }

    /** @todo I'm sure db has me a TTL, but. */
    @Override
    public int getResultSetTTL() {
        return -1;
    }

    /**
     * Default implementation gives upper bound of result count.
     */
    @Override
    public SRUResultCountPrecision getResultCountPrecision() {
        return SRUResultCountPrecision.MAXIMUM;
    }


    /**
     * Returns CLARIN FCS record schema (probably for all methods).
     */
    @Override
    public String getRecordSchemaIdentifier() {
        return CLARIN_FCS_RECORD_SCHEMA;
    }





    /** Get size of db. */
    @Override
    public int getTotalRecordCount() {
        return advancedResult.getTotalLength();
    }

    /** Get size of db query results. */
    @Override
    public int getRecordCount() {
        return advancedResult.getLength();
    }

    /** Get current PID. */
    @Override
    public String getRecordIdentifier() {
        return advancedResult.getRecordAt(pos).getPID();
    }

    /** Advance to next result and return whether it exists.
     * @sideeffect advances DB pointer.
     */
    @Override
    public boolean nextRecord() {
        ++pos;
        if (pos < advancedResult.getLength()) {
            return true;
        } else {
            return false;
        }
    }


    /** No surrogate diagnostics. */
    @Override
    public SRUDiagnostic getSurrogateDiagnostic() {
        return null;
    }

    /** Write hits and kwic dataview of current match.
     * Will do something advanced in future.
     */
    @Override
    public void writeRecord(XMLStreamWriter writer)
            throws XMLStreamException {
        final AdvancedSearchResult rec =
            advancedResult.getRecordAt(pos);
        // advanced_
        XMLStreamWriterHelper.writeStartResource(writer, rec.getPID(),
                rec.getPage());
        XMLStreamWriterHelper.writeStartResourceFragment(writer, null,
                null);
        AdvancedDataViewWriter helper = new AdvancedDataViewWriter(
                AdvancedDataViewWriter.Unit.TIMESTAMP);
        URI layerId =
            URI.create("http://corpora.uni-hamburg.de/Layers/orth1");
        List<AdvancedSearchResultSegment> highlights =
            rec.getResultHighlights();
        for (AdvancedSearchResultSegment seg : highlights) {
            if (seg.isHighlighted()) {
                helper.addSpan(layerId, Math.round(seg.getStart()),
                        Math.round(seg.getEnd()), seg.getText(), 1);
            } else {
                helper.addSpan(layerId, Math.round(seg.getStart()),
                        Math.round(seg.getEnd()), seg.getText());
            }
        }
        for(Map.Entry<String, List<AdvancedSearchResultSegment>> entry :
                rec.getChildLayers().entrySet()) {
            String name = entry.getKey();
            List<AdvancedSearchResultSegment> segments =
                entry.getValue();
            URI layer = null;
            try {
                // words and stuff
                if (name.equals("pos")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/pos1");
                    // pos supplements??
                } else if (name.equals("pos-sup")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/pos2");
                    // C=???
                } else if (name.equals("morph")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/msd1");
                    // C=???
                } else if (name.equals("c")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/x-c");
                } else if (name.equals("lemma")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/lemma1");
                // events and stuff
                } else if (name.equals("pho")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/phon1");
                } else if (name.equals("disfluency")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/x-disfluency1");
                } else if (name.equals("token")) {
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/x-word1");
                // something unexpected
                } else {
                    System.out.println("MISSING layer name: " + name);
                    layer = new
                        URI("http://corpora.uni-hamburg.de/Layers/unknown1");
                }
            } catch (URISyntaxException use) {
                System.out.println("Some config error with HZSK and URIs: " +
                        use.getStackTrace());
            }
            AdvancedSearchResultSegment previousSegments = null;
            for (AdvancedSearchResultSegment segment : segments) {
                if (previousSegments == null) {
                    previousSegments = segment;
                }
                else if ((previousSegments.getStart() < 0) ||
                        (previousSegments.getEnd() < 0)) {
                    // FIXME: should interpolate;
                    previousSegments = segment;
                    continue;
                }
                else if (((int)Math.round(previousSegments.getStart()) ==
                            (int)Math.round(segment.getStart())) &&
                        ((int)Math.round(previousSegments.getEnd()) ==
                         (int)Math.round(segment.getEnd()))) {
                    previousSegments.setText(previousSegments.getText() +
                            "||" + segment.getText());
                    previousSegments.setAnnotation(
                            previousSegments.getAnnotation() + "||" +
                            segment.getAnnotation());
                } else {
                    if (name.equals("pos") || (name.equals("pos-sup"))) {
                       helper.addSpan(layer,
                                Math.round(previousSegments.getStart()),
                                Math.round(previousSegments.getEnd()),
                                STTS2UDConverter.fromSTTS(
                                    previousSegments.getAnnotation()),
                                previousSegments.getAnnotation());
                    } else {
                        helper.addSpan(layer,
                                Math.round(previousSegments.getStart()),
                                Math.round(previousSegments.getEnd()),
                                previousSegments.getAnnotation());
                    }
                    previousSegments = segment;
                }
            }
            if (previousSegments != null) {
                    if ((previousSegments.getStart() > 0) &&
                        (previousSegments.getEnd() > 0)) {
                    if (name.equals("pos") || (name.equals("pos-sup"))) {
                       helper.addSpan(layer,
                                Math.round(previousSegments.getStart()),
                                Math.round(previousSegments.getEnd()),
                                STTS2UDConverter.fromSTTS(
                                    previousSegments.getAnnotation()),
                                previousSegments.getAnnotation());
                    } else {
                        helper.addSpan(layer,
                                Math.round(previousSegments.getStart()),
                                Math.round(previousSegments.getEnd()),
                                previousSegments.getAnnotation());
                    }
               }
            }
        }
        helper.writeHitsDataView(writer, layerId);
        helper.writeAdvancedDataView(writer);
        XMLStreamWriterHelper.writeEndResourceFragment(writer);
        XMLStreamWriterHelper.writeEndResource(writer);
    }

    /** Has none. */
    public boolean hasExtraRecordData() {
        return false;
    }

    /* public void writeExtraRecordData(XMLStreamWriter writer); */


} // class HZSKSRUSearchResultSet
