/**
 * @file HZSKSRUScanResult.java
 * @copyright Hamburger Zentrum für Sprach Korpora
 */
package de.uni_hamburg.corpora.sru;

import eu.clarin.sru.server.SRUScanResultSet;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUDiagnosticList;

import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * HZSK implementation of SRU scan results.
 * Uses some hard-coded strings.
 *
 * @fixme Don't know what to implement really, docs say don't do scan...
 * @author tpirinen
 */
public class HZSKSRUScanResultSet extends SRUScanResultSet {

    private boolean beforeFirst = true;

    /** Create scan results passing diagnostics upwards. */
    public HZSKSRUScanResultSet(SRUDiagnosticList diagnostics) {
        super(diagnostics);
    }

    ///  Implement these methods:    
    /// getWhereInList(), getNumberOfRecords(), getDisplayTerm(), getValue(), 
    // and getNextTerm()

    @Override
    public boolean nextTerm() throws SRUException {
        if (beforeFirst) {
            beforeFirst = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getValue() {
        return "hamatac";
    }

    @Override
    public int getNumberOfRecords() {
        return 1;
    }

    @Override
    public String getDisplayTerm() {
        return "Ham attack corpus [DEBUG: hard-coded ScanResultSet]";
    }

    @Override
    public WhereInList getWhereInList() {
        return SRUScanResultSet.WhereInList.ONLY;
    }


    @Override
    public boolean hasExtraTermData() {
        return false;
    }


    /*
    public void writeExtraTermData(XMLStreamWriter writer)
     */

} // abstract class SRUScanResult
