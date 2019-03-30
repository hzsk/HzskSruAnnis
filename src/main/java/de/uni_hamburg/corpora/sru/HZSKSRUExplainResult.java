/**
 * @file HZSKSRUExplainResult.java
 * @copyright Hamburger Zentrum für Sprach Korpora
 */
package de.uni_hamburg.corpora.sru;

import eu.clarin.sru.server.SRUExplainResult;
import eu.clarin.sru.server.SRUDiagnosticList;

/**
 * HZSK implementation for FCS / SRU explain results.
 * Wraps infos from DB to SRUExplainResult if needed, however, currently the
 * EndPoint just uses simple description.
 */
public class HZSKSRUExplainResult extends SRUExplainResult {

    private DBDescriptionResult dBresult;

    /** Create explain result passing diagnostics up. */
    public HZSKSRUExplainResult(SRUDiagnosticList diagnostics) {
        super(diagnostics);
    }

    public HZSKSRUExplainResult(SRUDiagnosticList diagnostics, 
            DBDescriptionResult dbr) {
        super(diagnostics);
        dBresult = dbr;
    }

} // class HZSKSRUExplainResult
