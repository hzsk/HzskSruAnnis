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
import java.net.URI;

/**
 * A segment in FCS advanced search.
 * This is HZSK version of segment, so it has starts and ends for different
 * timelines and stuff.
 * @author tpirinen
 */
public class AdvancedSearchResultSegment
        implements Comparable<AdvancedSearchResultSegment> {

    private String annotation;
    private String text;
    private boolean hitness;
    private double start;
    private double end;

    public AdvancedSearchResultSegment(String ann) {
        this.annotation = ann;
        this.start = -1;
        this.end = -2;
    }

    public AdvancedSearchResultSegment(String ann, boolean highlight) {
        this.annotation = ann;
        this.hitness = true;
        this.start = -1;
        this.end = -2;
    }
    public AdvancedSearchResultSegment(String ann, String text,
            double start, double end) {
        this.annotation = ann;
        this.text = text;
        this.start = start;
        this.end = end;
        this.hitness = false;
    }

    public AdvancedSearchResultSegment(String text, double start, double end) {
        this.annotation = text;
        this.text = text;
        this.start = start;
        this.end = end;
        this.hitness = false;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getText() {
        return text;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public boolean isHighlighted() {
        return hitness;
    }

    public void setHighlighted(boolean highlighted) {
        hitness = highlighted;
    }

    @Override
    public int compareTo(AdvancedSearchResultSegment rhs) {
        if (this.getStart() == rhs.getStart()) {
            if (this.getEnd() == rhs.getEnd()) {
                if (this.getAnnotation().equals(rhs.getAnnotation())) {
                    return this.getText().compareTo(rhs.getText());
                } else {
                    return
                        this.getAnnotation().compareTo(rhs.getAnnotation());
                }
            } else {
                if (this.getEnd() < rhs.getEnd()) {
                    return -1;
                } else if (this.getEnd() > rhs.getEnd()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } else {
            if (this.getStart() < rhs.getStart()) {
                return -1;
            } else if (this.getStart() > rhs.getStart()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}

