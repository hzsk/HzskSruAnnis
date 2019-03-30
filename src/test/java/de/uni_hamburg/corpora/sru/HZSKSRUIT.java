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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import java.util.List;
import java.net.URI;
import org.w3c.dom.Node;

import org.junit.Test;
import org.junit.Before;

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
import eu.clarin.sru.client.SRUClientConstants;
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
 * An integration test set for HZSK SRU.
 * Run it with mvn verify after deploying. Also, set the host to corpora
 * for final deployment, now we use localhost for development.
 */
public class HZSKSRUIT {

    SRUClient client;
    String host;
    String annishost;

    @Before
    public void setUp() {
        this.client = new ClarinFCSClientBuilder()
                        .addDefaultDataViewParsers()
                        .unknownDataViewAsString()
                        .buildClient();
        // XXX: change to deployment target
        this.host = "http://corpora.uni-hamburg.de:8080/HZSKsru";
        this.annishost = "http://annis.corpora.uni-hamburg.de:8080/HZSKsru";
    }


    @Test
    public void explain() throws SRUClientException {
        SRUExplainRequest request = new SRUExplainRequest(host);
        request.setExtraRequestData("x-fcs-endpoint-description",
                "true");
        request.setParseRecordDataEnabled(true);
        SRUExplainResponse response = client.explain(request);
        assertThat("FAIL: basic explain should not respond diagnostics",
                response.hasDiagnostics(), is(false));
    }

    @Test
    public void scan() throws SRUClientException {
        SRUScanRequest request = new SRUScanRequest(host);
        request.setScanClause("fcs.resource = root");
        request.setExtraRequestData("x-clarin-resource-info", "true");
        SRUScanResponse response = client.scan(request);
        // NB: IDS Mannheim also returns diagnostics...
        assertThat("FAIL: scan response should not have diagnostics?",
                response.hasDiagnostics(), is(false));
        assertThat("FAIL: scan response should contain terms",
                response.hasTerms(), is(true));
    }

    @Test
    public void searchRetrieve() throws SRUClientException {
        String query = "Käse";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery("fcs" /*SRUClientConstants.QUERY_TYPE_CQL*/,
                query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diag : response.getDiagnostics()) {
                System.out.println("DIAGNOSTIC:"  + diag.getMessage());
            }
        }
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than five" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(5)));
    }

    @Test
    public void searchRetrieveCQL() throws SRUClientException {
        String query = "Käse";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery(SRUClientConstants.QUERY_TYPE_CQL,
                query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than five" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(5)));
    }

    @Test
    public void searchRetrieveMaxFive() throws SRUClientException {
        String query = "Käse";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery("fcs" /*SRUClientConstants.QUERY_TYPE_CQL*/,
                query);
        request.setMaximumRecords(5);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should get 5 records with maximum included " +
                "(Change the query \"" + query + "\"" +
                " if not using term that has 5 hits)",
                response.getRecords().size(), is(5));
    }

    @Test
    public void searchRetrieveFromPOS() throws SRUClientException {
        String query = "[pos = 'NOUN']";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery("fcs",
                query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        request.setVersion(SRUVersion.VERSION_2_0);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diag : response.getDiagnostics()) {
                System.out.println("DIAGNOSTIC:"  + diag.getMessage());
            }
        }
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than five" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(5)));
    }

    @Test
    public void searchRetrieveFromLemma() throws SRUClientException {
        String query = "[lemma = 'Käse']";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery("fcs",
                query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        request.setVersion(SRUVersion.VERSION_2_0);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diag : response.getDiagnostics()) {
                System.out.println("DIAGNOSTIC:"  + diag.getMessage());
            }
        }
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than five" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(5)));
    }

    @Test
    public void searchRetrieveAnd() throws SRUClientException {
        String query = "Käse AND rechts";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery("fcs" /*SRUClientConstants.QUERY_TYPE_CQL*/,
                query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diag : response.getDiagnostics()) {
                System.out.println("DIAGNOSTIC:"  + diag.getMessage());
            }
        }
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than five" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(5)));
    }

    @Test
    public void searchRetrieveLemmaAndText() throws SRUClientException {
        String query = "'Käse' [lemma = 'Käse']";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(host);
        request.setQuery("fcs",
                query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        request.setVersion(SRUVersion.VERSION_2_0);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diag : response.getDiagnostics()) {
                System.out.println("DIAGNOSTIC:"  + diag.getMessage());
            }
        }
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than five" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(5)));
    }

    @Test
    public void searchAnnis() throws SRUClientException {
        // a5.hausa.news
        String query = "Friedrich";
        SRUSearchRetrieveRequest request = new
            SRUSearchRetrieveRequest(annishost);
        request.setQuery("fcs", query);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
        SRUSearchRetrieveResponse response = client.searchRetrieve(request);
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diag : response.getDiagnostics()) {
                System.out.println("DIAGNOSTIC:"  + diag.getMessage() +
                        "\n  details:" + diag.getDetails());
            }
        }
        assertThat("FAIL: search retrieve should not have diagnostics",
                response.hasDiagnostics(), is(false));
        assertNotNull("FAIL: search retrieve should have results",
                response.getRecords());
        assertThat("FAIL: we should have more than zero" +
                "(fix test if we don't)",
                response.getRecords().size(), is(greaterThan(0)));
    }

} //
