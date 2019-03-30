/**
 * @file HZSKSRUPrintlnHandler
 * @licence GPLv3
 * @author tpirinen
 *
 * Simple printer adapter for FCS Simple client.
 * Derived from FCSSimpleClient in svn.clarin.eu by IDS Mannheim.
 * Changed from loggers to stdout for basic command-line work.
 */

package de.uni_hamburg.corpora.hzsksru;

import java.util.List;
import java.net.URI;
import org.w3c.dom.Node;

import org.junit.Test;

import eu.clarin.sru.client.fcs.ClarinFCSClientBuilder;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription.ResourceInfo;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;
import eu.clarin.sru.client.fcs.DataView;
import eu.clarin.sru.client.fcs.DataViewAdvanced;
import eu.clarin.sru.client.fcs.DataViewGenericDOM;
import eu.clarin.sru.client.fcs.DataViewGenericString;
import eu.clarin.sru.client.fcs.DataViewHits;
import eu.clarin.sru.client.fcs.Resource;
import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUDefaultHandlerAdapter;
import eu.clarin.sru.client.SRUDiagnostic;
import eu.clarin.sru.client.SRUExplainRecordData;
import eu.clarin.sru.client.SRUExplainRecordData.ConfigInfo;
import eu.clarin.sru.client.SRUExplainRecordData.Schema;
import eu.clarin.sru.client.SRUExplainRequest;
import eu.clarin.sru.client.SRUExplainResponse;
import eu.clarin.sru.client.SRUExtraResponseData;
import eu.clarin.sru.client.SRURecord;
import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRURecordPacking;
import eu.clarin.sru.client.SRURecordXmlEscaping;
import eu.clarin.sru.client.SRUScanRequest;
import eu.clarin.sru.client.SRUScanResponse;
import eu.clarin.sru.client.SRUSearchRetrieveRequest;
import eu.clarin.sru.client.SRUSearchRetrieveResponse;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUSurrogateRecordData;
import eu.clarin.sru.client.SRUTerm;
import eu.clarin.sru.client.SRUWhereInList;


public class HZSKSRUPrintlnHandler extends SRUDefaultHandlerAdapter {

    @Override
    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException {
        for (SRUDiagnostic diagnostic : diagnostics) {
            System.out.println("onDiagnostics URI, details, message: " +
                    diagnostic.getURI() + "\n" +
                    diagnostic.getDetails() + "\n" + 
                    diagnostic.getMessage());
        }
    }

    @Override
    public void onRequestStatistics(int bytes, long millisTotal,
            long millisNetwork, long millisParsing) {
        System.out.println("onRequestStatistics(bytes, millis):\n"
                + bytes + ", " + millisTotal);
    }

    @Override
    public void onStartTerms() throws SRUClientException {
        System.out.println("onStartTerms()");
    }

    @Override
    public void onFinishTerms() throws SRUClientException {
        System.out.println("onFinishTerms()");
    }

    @Override
    public void onTerm(String value, int numberOfRecords,
            String displayTerm, SRUWhereInList whereInList)
            throws SRUClientException {
        System.out.println("onTerm(value, nRecords, dterm, where):\n"
                + value + "\n"
                + numberOfRecords + "\n"
                + displayTerm + "\n" 
                + whereInList);
    }

    @Override
    public void onStartRecords(int numberOfRecords,
            String resultSetId, int resultSetIdleTime)
            throws SRUClientException {
        System.out.println("onStartRecords(nRecords)" +
                numberOfRecords);
    }

    @Override
    public void onFinishRecords(int nextRecordPosition)
            throws SRUClientException {
        System.out.println("onFinishRecords(nextRecord) " +
                nextRecordPosition);
    }

    @Override
    public void onRecord(String identifier, int position,
            SRURecordData data) throws SRUClientException {
        System.out.println("onRecord(identifier, position, schema):\n"
               + identifier + "\n"
               + position + "\n"
               + data.getRecordSchema());
        if (ClarinFCSRecordData.RECORD_SCHEMA.equals(data.getRecordSchema())) {
            ClarinFCSRecordData record =
                    (ClarinFCSRecordData) data;
            HZSKSRUStuffPrinter.dumpResource(record.getResource());
        } else if (SRUExplainRecordData.RECORD_SCHEMA.equals(data.getRecordSchema())) {
            HZSKSRUStuffPrinter.dumpExplainRecordData(data);
        }
    }

    @Override
    public void onSurrogateRecord(String identifier, int position,
            SRUDiagnostic data) throws SRUClientException {
        System.out.println("onSurrogateRecord(identifier, position, uri, detail}, message)\n"
                + identifier + "\n" +
                position + "\n" + 
                data.getURI() + "\n" +
                data.getDetails() + "\n" +
                data.getMessage());
    }
}

