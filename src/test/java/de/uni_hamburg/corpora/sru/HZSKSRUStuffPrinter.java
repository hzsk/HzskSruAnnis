/**
 * @file HZSKSRUStuffPrinter.java
 * @licence GPLv3
 * @author tpirinen
 *
 * HZSK's SRU test client.
 * Derived from FCSSimpleClient in svn.clarin.eu by IDS Mannheim.
 * For printing SRU and FCS structs on stdout.
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
import eu.clarin.sru.client.SRUWhereInList;

public class HZSKSRUStuffPrinter {

    public static void printExplainResponse(SRUExplainResponse response) {
        System.out.println("displaying results of 'explain' request ...");
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                System.out.println("Diagnostics(uri, msg, det):\n" +
                        diagnostic.getURI() + "\n" +
                        diagnostic.getMessage() + "\n" +
                        diagnostic.getDetails());
            }
        }
        if (response.hasRecord()) {
            SRURecord record = response.getRecord();
            System.out.println("schema = " + record.getRecordSchema());
            if (record.isRecordSchema(SRUExplainRecordData.RECORD_SCHEMA)) {
                dumpExplainRecordData(record.getRecordData());
            }
            if (record.hasExtraRecordData()) {
                System.out.println("extraRecordInfo" + 
                        record.getExtraRecordData());
            }
        }
        if (response.hasExtraResponseData()) {
            for (SRUExtraResponseData data : response.getExtraResponseData()) {
                if (data instanceof ClarinFCSEndpointDescription) {
                    dumpEndpointDescription(
                            (ClarinFCSEndpointDescription) data);
                } else {
                    System.out.println("extraResponseData " + 
                            data.getRootElement() + " class " +
                            data.getClass().getName());
                }

            }
        }
    }


    public static void printScanResponse(SRUScanResponse response) {
        System.out.println("displaying results of 'scan' request ...");
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                System.out.println("Diagnostics(uri, msg, det):\n" +
                        diagnostic.getURI() + "\n" +
                        diagnostic.getMessage() + "\n" +
                        diagnostic.getDetails());
            }
        }
        if (response.hasTerms()) {
            for (SRUTerm term : response.getTerms()) {
                System.out.println("(value, numberOfRecords, displayTerm):\n" +
                            term.getValue() + "\n" +
                            term.getNumberOfRecords() + "\n" +
                            term.getDisplayTerm());
            }
        } else {
            System.out.println("no terms");
        }
    }


    public static void printSearchResponse(SRUSearchRetrieveResponse response) {
        System.out.println("results of 'searchRetrieve' request ...");
        System.out.println("numberOfRecords, nextResultPosition\n " +
                response.getNumberOfRecords() + "\n" + 
                response.getNextRecordPosition());
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                System.out.println("Diagnostics(uri, msg, det):\n" +
                        diagnostic.getURI() + "\n" +
                        diagnostic.getMessage() + "\n" +
                        diagnostic.getDetails());
            }
        }
        if (response.hasRecords()) {
            for (SRURecord record : response.getRecords()) {
                System.out.println("schema, identifier, position\n" + 
                        record.getRecordSchema() + "\n" +
                        record.getRecordIdentifier() + "\n" +
                        record.getRecordPosition());
                if (record.isRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA)) {
                    ClarinFCSRecordData rd =
                            (ClarinFCSRecordData) record.getRecordData();
                    dumpResource(rd.getResource());
                } else if (record.isRecordSchema(
                            SRUSurrogateRecordData.RECORD_SCHEMA)) {
                    SRUSurrogateRecordData r =
                            (SRUSurrogateRecordData) record.getRecordData();
                    System.out.println("SURROGATE DIAGNOSTIC: " +
                            "uri, message, detail\n" +
                                r.getURI() + "\n" +
                                r.getMessage() + "\n" +
                                r.getDetails());
                } else {
                    System.out.println("UNSUPPORTED SCHEMA: " +
                            record.getRecordSchema());
                }
            }
        } else {
            System.out.println("no results");
        }
    }


    public static void dumpExplainRecordData(SRURecordData recordData) {
        if (SRUExplainRecordData.RECORD_SCHEMA.equals(
                    recordData.getRecordSchema())) {
            SRUExplainRecordData data = (SRUExplainRecordData) recordData;
            System.out.println("host, port, database\n" + 
                    data.getServerInfo().getHost() + "\n" +
                    data.getServerInfo().getPort() + "\n" + 
                    data.getServerInfo().getDatabase());
            List<Schema> schemaInfo = data.getSchemaInfo();
            if (schemaInfo != null) {
                for (Schema schema : schemaInfo) {
                    System.out.println("DEBUG:: schema: identifier, name, " +
                            "location, sort, retrieve\n" +
                            schema.getIdentifier() + "\n" +
                            schema.getName() + "\n" +
                            schema.getLocation() + "\n" +
                            schema.getSort() + "\n" +
                            schema.getRetrieve());
                }
            }
            ConfigInfo configInfo = data.getConfigInfo();
            if (configInfo != null) {
                if (configInfo.getDefaults() != null) {
                    System.out.println("DEBUG:: configInfo/default" + "\n" +
                            configInfo.getDefaults());
                }
                if (configInfo.getSettings() != null) {
                    System.out.println("DEBUG:: configInfo/setting" + "\n" +
                            configInfo.getSettings());
                }
                if (configInfo.getSupports() != null) {
                    System.out.println("DEBUG:: configInfo/supports" + "\n" +
                            configInfo.getSupports());
                }
            }
        }
    }


    public static void dumpResource(Resource resource) {
        System.out.println("CLARIN-FCS: pid, ref" + "\n" +
                resource.getPid() + "\n" +
                resource.getRef());
        if (resource.hasDataViews()) {
            dumpDataView("CLARIN-FCS: ", resource.getDataViews());
        }
        if (resource.hasResourceFragments()) {
            for (Resource.ResourceFragment fragment :
                    resource.getResourceFragments()) {
                System.out.println("DEBUG:: CLARIN-FCS: " + 
                        "ResourceFragment: pid, ref" + "\n" +
                        fragment.getPid() + "\n" +
                        fragment.getRef());
                if (fragment.hasDataViews()) {
                    dumpDataView("CLARIN-FCS: ResourceFragment/",
                            fragment.getDataViews());
                }
            }
        }
    }


    private static void dumpEndpointDescription(
            ClarinFCSEndpointDescription ed) {
        System.out.println("dumping <EndpointDescription> (version)" + "\n" +
                ed.getVersion());
        for (URI capability : ed.getCapabilities()) {
            System.out.println("  capability:" + capability);
        } // for
        for (ClarinFCSEndpointDescription.DataView dataView :
            ed.getSupportedDataViews()) {
            System.out.println("  supportedDataView: id, type, policy" + "\n" +
                    dataView.getIdentifier() + "\n" +
                    dataView.getMimeType() + "\n" +
                    dataView.getDeliveryPolicy());
        } // for
        for (ClarinFCSEndpointDescription.Layer layer :
            ed.getSupportedLayers()) {
            System.out.println("  supportedLayer: id, result-id, " +
                    "layer-type, encoding, qualifier, " +
                    "alt-value-info, alt-value-info-uri" + "\n" +
                    layer.getIdentifier() + "\n" +
                    layer.getResultId() + "\n" +
                    layer.getLayerType() + "\n" +
                    layer.getEncoding() + "\n" +
                    layer.getQualifier() + "\n" +
                    layer.getAltValueInfo() + "\n" +
                    layer.getAltValueInfoURI());
        }
        dumpResourceInfo(ed.getResources(), 1, "  ");
    }


    private static void dumpResourceInfo(List<ResourceInfo> ris, int depth,
            String indent) {
        for (ResourceInfo ri : ris) {
            System.out.println("{}[depth] <ResourceInfo>" + "\n" +
                    indent + "\n" +
                    depth);
            System.out.println("{}    pid" + "\n" +
                    indent + "\n" +
                    ri.getPid());
            System.out.println("{}    title: {}" + "\n" +
                    indent + "\n" +
                    ri.getTitle());
            if (ri.getDescription() != null) {
                System.out.println("{}    description: {}" + "\n" +
                        indent + "\n" +
                        ri.getDescription());
            }
            if (ri.getLandingPageURI() != null) {
                System.out.println("{}    landingPageURI: {}" + "\n" +
                        indent + "\n" +
                        ri.getLandingPageURI());
            }
            for (ClarinFCSEndpointDescription.DataView dv :
                ri.getAvailableDataViews()) {
                System.out.println("{}    available dataviews: type, policy"
                        + "\n" +
                        indent + "\n" +
                        dv.getMimeType() + "\n" +
                        dv.getDeliveryPolicy());
            }
            for (ClarinFCSEndpointDescription.Layer l :
                ri.getAvailableLayers()) {
                System.out.println("{}    available layers: result-id," +
                        " layer-type" + "\n" +
                        indent + "\n" +
                        l.getResultId() + "\n" +
                        l.getLayerType());
            }
            if (ri.hasSubResources()) {
                dumpResourceInfo(ri.getSubResources(),
                        depth + 1,
                        indent + "  ");
            }
        }
    }


    private static void dumpDataView(String s, List<DataView> dataviews) {
        for (DataView dataview : dataviews) {
            System.out.println("{}DataView: type, pid, ref" + "\n" +
                    s + "\n" +
                    dataview.getMimeType() + "\n" +
                    dataview.getPid() + "\n" +
                    dataview.getRef());
            if (dataview instanceof DataViewGenericDOM) {
                final DataViewGenericDOM view = (DataViewGenericDOM) dataview;
                final Node root = view.getDocument().getFirstChild();
                System.out.println("{}DataView (generic dom): root element " +
                        "<{}> / {}" + "\n" +
                        s + "\n" +
                        root.getNodeName() + "\n" +
                        root.getOwnerDocument().hashCode());
            } else if (dataview instanceof DataViewGenericString) {
                final DataViewGenericString view =
                        (DataViewGenericString) dataview;
                System.out.println("{}DataView (generic string): data" + "\n" +
                        s + "\n" +
                        view.getContent());
            } else if (dataview instanceof DataViewHits) {
                final DataViewHits hits = (DataViewHits) dataview;
                System.out.println("{}DataView: {}" + "\n" +
                        s + "\n" +
                        addHitHighlights(hits));
            } else if (dataview instanceof DataViewAdvanced) {
                final DataViewAdvanced adv = (DataViewAdvanced) dataview;
                System.out.println("{}DataView: unit" + "\n" +
                        s + "\n" +
                        adv.getUnit());
                for (DataViewAdvanced.Layer layer : adv.getLayers()) {
                    System.out.println("{}DataView: Layer: id" + "\n" +
                            s + "\n" +
                            layer.getId());
                    for (DataViewAdvanced.Span span : layer.getSpans()) {
                        System.out.println("{}DataView:   Span: " +
                                "start, end, content" + "\n" +
                                s + "\n" +
                                span.getStartOffset() + "\n" +
                                span.getEndOffset() + "\n" +
                                span.getContent());
                    }
                }
            } else {
                System.out.println("{}DataView: cannot display " +
                        "contents of unexpected class '{}'" + "\n" +
                        s + "\n" +
                        dataview.getClass().getName());
            }
        }
    }


    private static String addHitHighlights(DataViewHits hits) {
        StringBuilder sb = new StringBuilder(hits.getText());
        int corr = 0;
        for (int i = 0; i < hits.getHitCount(); i++) {
            int[] offsets = hits.getHitOffsets(i);
            sb.insert(offsets[0] + corr, "[");
            corr += 1;
            sb.insert(offsets[1] + corr, "]");
            corr += 1;
        }
        return sb.toString();
    }
}
