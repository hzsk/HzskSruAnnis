/**
 * @file DBSearchResult.java
 * @copyright Hamburger Zentrum für Sprach Korpora.
 */

package de.uni_hamburg.corpora.sru;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;

/**
 * A single result from advanced search.
 * Holds the text span containing the match, the segmented matches and all
 * the child layers with their segmentations. It is noteworthy that not all
 * matches to query can be set on timeline in a way that FCS needs; we cannot
 * deduce matches' position in time timeline if it doesn't coincide with
 * existing segmentation point. The character position can always be used.
 * @author tpirinen
 */
public class AdvancedSearchResult {

    /* an advanced search result has number of segmented spans:
     * main text:  something something hit something hit something
     * highlights: ...................|---|.........|--- |........
     * child layers.
     */
    private AdvancedSearchResultSegment wholeText;
    private List<AdvancedSearchResultSegment> searchSplits;
    private Map<String, List<AdvancedSearchResultSegment>> childLayers;

    private String source;
    private String pid;
    private String page;
    private double start;
    private double end;
    private boolean valid = false;


    /** Create partial result from incomplete informations. */
    public AdvancedSearchResult() {
        childLayers = new HashMap<String, List<AdvancedSearchResultSegment>>();
    }

    /** Create a simple match in text with necessary informations. */
    public AdvancedSearchResult(AdvancedSearchResultSegment whole,
            List<AdvancedSearchResultSegment> highlights,
            String source, String pid, String page,
            double start, double end) {
        this.wholeText = whole;
        this.searchSplits = highlights;
        this.source = source;
        this.pid = pid;
        this.page = page;
        this.start = start;
        this.end = end;
        this.childLayers = new HashMap<String,
                 List<AdvancedSearchResultSegment>>();
        valid = true;
    }

    public void addChildLayer(String name,
            List<AdvancedSearchResultSegment> segments) {
        childLayers.put(name, segments);
    }

    public Map<String, List<AdvancedSearchResultSegment>>
           getChildLayers() {
        return childLayers;
    }

    public AdvancedSearchResultSegment getResultText() {
        return wholeText;
    }

    /**
     * @return the hit
     */
    public List<AdvancedSearchResultSegment> getResultHighlights() {
        return searchSplits;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @return the pid
     */
    public String getPID() {
        return pid;
    }

    /**
     * @return the page
     */
    public String getPage() {
        return page;
    }

    public double getStart() {
        return wholeText.getStart();
    }

    public double getEnd() {
        return wholeText.getEnd();
    }

    public void setPage(String page) {
        this.page = page;
        validate();
    }

    public void setSource(String source) {
        this.source = source;
        validate();
    }

    public void setPID(String pid) {
        this.pid = pid;
        validate();
    }

    public void setStart(int start) {
        this.start = start;
        validate();
    }

    public void setEnd(int end) {
        this.end = end;
        validate();
    }

    public void setWholeText(AdvancedSearchResultSegment whole) {
        this.wholeText = whole;
    }

    public void setHighlights(List<AdvancedSearchResultSegment> highlights) {
        this.searchSplits = highlights;
    }

    private void validate() {
        if ((page != null) && (source != null) && (end > -1) && (start > -1) &&
                (start <= end) && (pid != null) && (wholeText != null)) {
            this.valid = true;
        } else {
            this.valid = false;
        }
    }

    public static
        List<AdvancedSearchResultSegment>
        highlightSearch(AdvancedSearchResultSegment text,
            HZSKQuery query) {
        List<AdvancedSearchResultSegment> highlights = new
            ArrayList<AdvancedSearchResultSegment>();
        String fulltext = text.getText();
        // create a highlighter from query
        String matcherSearch = "";
        String delim = "";
        for (String s : query.getTextSearches()) {
            matcherSearch += delim;
            matcherSearch += s;
            delim = "|";
        }
        Pattern pattern = Pattern.compile(matcherSearch);
        Matcher matcher = pattern.matcher(fulltext);
        int previousEnd = 0;
        matcher.reset();
        while (matcher.find()) {
            if (previousEnd < matcher.start()) {
                AdvancedSearchResultSegment left = new
                    AdvancedSearchResultSegment(fulltext.substring(previousEnd,
                            matcher.start()), text.getStart() + previousEnd,
                            text.getStart() + previousEnd +
                            (matcher.start() - previousEnd));
                highlights.add(left);
            }
            AdvancedSearchResultSegment hit = new AdvancedSearchResultSegment(
                fulltext.substring(matcher.start(), matcher.end()),
                    text.getStart() + matcher.start(),
                    text.getStart() + matcher.start() +
                    (matcher.end() - matcher.start()));
            hit.setHighlighted(true);
            highlights.add(hit);
            previousEnd = matcher.end();
        }
        if (previousEnd < fulltext.length()) {
            AdvancedSearchResultSegment right = new AdvancedSearchResultSegment(
                    fulltext.substring(previousEnd),
                    text.getStart() + previousEnd, text.getEnd());
            highlights.add(right);
        }
        return highlights;
    }

    public static
        List<AdvancedSearchResultSegment>
        highlightSegments(AdvancedSearchResultSegment text,
            HZSKQuery query) {
        List<AdvancedSearchResultSegment> highlights = new
            ArrayList<AdvancedSearchResultSegment>();
        //XXX: todo
        highlights.add(text);
        return highlights;
    }


}


