/**
 * @file HZSKSRUCommandlineClient.java
 * @licence GPLv3
 * @author tpirinen
 *
 * HZSK's SRU test client.
 * Derived from FCSSimpleClient in svn.clarin.eu by IDS Mannheim.
 * Changed some loggers to stdouts because annoying.
 */
package de.uni_hamburg.corpora.hzsksru;

import java.util.List;
import java.net.URI;
import org.w3c.dom.Node;


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
import eu.clarin.sru.client.SRUClient;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUSurrogateRecordData;
import eu.clarin.sru.client.SRUTerm;
import eu.clarin.sru.client.SRUVersion;
import eu.clarin.sru.client.SRUWhereInList;

/**
 * A test client for integration test.
 * I've tentatively wrapped it in junit stuff for mvn test but that's an ugly
 * hack.
 */
public class HZSKSRUCommandlineClient {

    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("initializing client ...");

            SRUClient client = new ClarinFCSClientBuilder()
                        .addDefaultDataViewParsers()
                        .unknownDataViewAsString()
                        .buildClient();

            try {
                System.out.println("performing 'explain' request ...");
                SRUExplainRequest request = new SRUExplainRequest(args[0]);
                request.setExtraRequestData("x-fcs-endpoint-description",
                        "true");
                request.setParseRecordDataEnabled(true);
                SRUExplainResponse response = client.explain(request);
                HZSKSRUStuffPrinter.printExplainResponse(response);
            } catch (SRUClientException e) {
                System.err.println("FATAL: during 'explain' request " +
                        e.getStackTrace()[0]);
            }

            try {
                System.out.println("performing 'scan' request ...");
                SRUScanRequest request = new SRUScanRequest(args[0]);
                request.setScanClause("fcs.resource = root");
                request.setExtraRequestData("x-clarin-resource-info", "true");
                SRUScanResponse response = client.scan(request);
                HZSKSRUStuffPrinter.printScanResponse(response);
            } catch (SRUClientException e) {
                System.err.println("FATAL: during 'scan' request " +
                        e.getStackTrace()[0]);
            }

            try {
                String query = args[1];
                for (int i = 2; i < args.length; i++) {
                    query += " " + args[i];
                }
                System.out.println("performing 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request = new
                    SRUSearchRetrieveRequest(args[0]);
                request.setQuery("fcs" /*SRUClientConstants.QUERY_TYPE_CQL*/,
                        query);
                request.setMaximumRecords(5);
                request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
                request.setRecordPacking(SRURecordPacking.PACKED);
                SRUSearchRetrieveResponse response = 
                    client.searchRetrieve(request);
                HZSKSRUStuffPrinter.printSearchResponse(response);
                System.out.println(response.getNumberOfRecords());
                System.out.println(response.getRecords().size());
                query = "[pos = 'NOUN']";
                request = new
                    SRUSearchRetrieveRequest(args[0]);
                request.setQuery("fcs",
                        query);
                request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
                request.setRecordPacking(SRURecordPacking.PACKED);
                request.setVersion(SRUVersion.VERSION_2_0);
                response = client.searchRetrieve(request);
                HZSKSRUStuffPrinter.printSearchResponse(response);
                if (response.hasDiagnostics()) {
                    for (SRUDiagnostic diag : response.getDiagnostics()) {
                        System.out.println("DIAGNOSTIC:"  + diag.getMessage());
                    }
                }
            } catch (SRUClientException e) {
                System.err.println("FATAL: during  'searchRetrieve' request " + 
                        e.getStackTrace()[0]);
                e.printStackTrace();
            }

            System.out.println("done");
            System.exit(0);
        } else {
            System.err.println("missing args");
            System.exit(2);
        }
    }
} // class TestSimpleClient
