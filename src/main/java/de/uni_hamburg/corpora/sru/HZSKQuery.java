/**
 * @file HZSKQuery.java
 * @copyright Hamburger Zentrum für Korpora
 *
 */
package de.uni_hamburg.corpora.sru;

import java.util.List;
import java.util.ArrayList;

import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.Modifier;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.CQLQueryParser.CQLQuery;
import eu.clarin.sru.server.fcs.Constants;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.FCSQueryParser;
import eu.clarin.sru.server.fcs.FCSQueryParser.FCSQuery;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.parser.Expression;
import eu.clarin.sru.server.fcs.parser.Operator;
import eu.clarin.sru.server.fcs.parser.QueryNode;
import eu.clarin.sru.server.fcs.parser.QuerySegment;
import eu.clarin.sru.server.fcs.parser.QuerySequence;
import eu.clarin.sru.server.fcs.ResourceInfo;
import eu.clarin.sru.server.fcs.SimpleEndpointSearchEngineBase;
import eu.clarin.sru.server.fcs.utils.SimpleEndpointDescription;
import eu.clarin.sru.server.fcs.utils.SimpleEndpointDescriptionParser;
import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnostic;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUQueryParserRegistry.Builder;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUScanResultSet;
import eu.clarin.sru.server.SRUSearchResultSet;
import eu.clarin.sru.server.SRUServerConfig;

/**
 * Relatively simple class for a parsed CQL/FCS query for HZSK.
 * I made it to act between search engine and its backends a bit, but it's
 * organically hacked together right now. Partially cause directly using CQL
 * AST's in DB connections or stuff didn't seem nice.
 * No it won't support multiple boolean operators on layers with arbitrary
 * nesting, you can have a query term per layer with an operator.
 * It'd be possible to support infinite layers with infinite queries but this
 * is purposedly under-engineered copy-pasta code.
 */
public class HZSKQuery {
    private static final String SUPPORTED_RELATION_CQL_1_1 = "scr";
    private static final String SUPPORTED_RELATION_CQL_1_2 = "=";
    private static final String SUPPORTED_RELATION_EXACT = "exact";
    private static final String INDEX_CQL_SERVERCHOICE = "cql.serverChoice";
    private static final String INDEX_FCS_WORDS = "words";
    // just one combotype over all searches
    enum ComboType {WHATEVER, AND, OR, NEAR};
    protected ComboType combine = ComboType.WHATEVER;
    protected List<String> textSearch;
    protected List<String> posSearch;
    protected List<String> lemmaSearch;
    protected String fcsString;
    protected String cqlString;
    private boolean regexSearch = false;
    private boolean usable = false;

    public HZSKQuery() {
        this.usable = false;
        textSearch = new ArrayList<String>();
        posSearch = new ArrayList<String>();
        lemmaSearch = new ArrayList<String>();
    }

    public void initialise(QueryNode query) throws SRUException {
        if (query instanceof QuerySegment) {
            QuerySegment segment = (QuerySegment) query;
            if ((segment.getMinOccurs() == 1) &&
                    (segment.getMaxOccurs() == 1)) {
                QueryNode child = segment.getExpression();
                if (child instanceof Expression) {
                    Expression expression = (Expression) child;
                    System.out.println("FCS DEBUG: " + expression);
                    // expression.getLayerIdentifier(),
                    // expression.getLayerQualifier()
                    // expression.getOperator() == Operator.
                    // expression.getRegexFlags()
                    if (expression.getLayerIdentifier().equals("text") &&
                            (expression.getLayerQualifier() == null)) {
                        setTextSearch(
                                expression.getRegexValue());
                    } else if (expression.getLayerIdentifier().equals("pos") &&
                            (expression.getLayerQualifier() == null)) {
                        setPosSearch(
                                expression.getRegexValue());
                    } else if (expression.getLayerIdentifier().equals("lemma") &&
                            (expression.getLayerQualifier() == null)) {
                        setLemmaSearch(
                                expression.getRegexValue());
                    } else {
                        throw new SRUException(
                                Constants.FCS_DIAGNOSTIC_GENERAL_QUERY_TOO_COMPLEX_CANNOT_PERFORM_QUERY,
                                "Endpoint does not support unqualified " +
                                "layer with id " +
                                expression.getLayerIdentifier());
                    }
                } else {
                    throw new SRUException(
                            Constants.FCS_DIAGNOSTIC_GENERAL_QUERY_TOO_COMPLEX_CANNOT_PERFORM_QUERY,
                            "Endpoint only supports simple expressions");
                }
            } else {
                throw new SRUException(
                        Constants.FCS_DIAGNOSTIC_GENERAL_QUERY_TOO_COMPLEX_CANNOT_PERFORM_QUERY,
                        "Endpoint only supports default occurances in segments");
            }
        } else if (query instanceof QuerySequence) {
            QuerySequence sequence = (QuerySequence) query;
            for (QueryNode child : sequence.getChildren()) {
                HZSKQuery childQuery = new HZSKQuery();
                childQuery.initialise(child);
                combineAnd(childQuery);
            }
        } else  {
            throw new SRUException(
                    Constants.FCS_DIAGNOSTIC_GENERAL_QUERY_TOO_COMPLEX_CANNOT_PERFORM_QUERY,
                    "Endpoint only supports some sequences or segment queries");
        }
    }

    // to traverse full syntax tree you ought to be recursive and stuff
    public void initialise(CQLNode query, int recursion) throws SRUException {
        if (query instanceof CQLTermNode) {
            final CQLTermNode root = (CQLTermNode) query;

            // XXX: this is temp hack until I figure out why FCS QL don't come
            // through
            if ((INDEX_CQL_SERVERCHOICE.equals(root.getIndex())
                    || INDEX_FCS_WORDS.equals(root.getIndex()))) {
                // pass
            } else {
                throw new SRUException(SRUConstants.SRU_UNSUPPORTED_INDEX,
                        root.getIndex(), "Index \"" + root.getIndex()
                        + "\" is not supported.");
            }

            // only allow "=" relation without any modifiers
            final CQLRelation relation = root.getRelation();
            final String baseRel = relation.getBase();
            if (!(SUPPORTED_RELATION_CQL_1_1.equals(baseRel)
                    || SUPPORTED_RELATION_CQL_1_2.equals(baseRel)
                    || SUPPORTED_RELATION_EXACT.equals(baseRel))) {
                throw new SRUException(SRUConstants.SRU_UNSUPPORTED_RELATION,
                        relation.getBase(), "Relation \""
                        + relation.getBase() + "\" is not supported.");
            }
            List<Modifier> modifiers = relation.getModifiers();
            if ((modifiers != null) && !modifiers.isEmpty()) {
                Modifier modifier = modifiers.get(0);
                throw new SRUException(
                        SRUConstants.SRU_UNSUPPORTED_RELATION_MODIFIER,
                        modifier.getValue(), "Relation modifier \""
                        + modifier.getValue() + "\" is not supported.");
            }

            // check term
            final String term = root.getTerm();
            if ((term == null) || term.isEmpty()) {
                throw new SRUException(SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
                        "An empty term is not supported.");
            }
            setTextSearch(term);
        } else if (query instanceof CQLBooleanNode) {
            if (recursion > 0) {
                throw new SRUException(SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
                        "Server currently does not support arbitrary nesting "
                        + "in search terms.");
            }
            String op = null;
            if (query instanceof CQLAndNode) {
                // I'm not gonna do full AST traversal recursion
                // FIXME THere's already something fishy here...
                CQLAndNode andNode = (CQLAndNode) query;
                CQLNode lhs = andNode.getLeftOperand();
                CQLNode rhs = andNode.getRightOperand();
                initialise(lhs, recursion + 1);
                HZSKQuery rightq = new HZSKQuery();
                rightq.initialise(rhs, recursion + 1);
                combineAnd(rightq);
            } else {
                throw new SRUException(
                        SRUConstants.SRU_UNSUPPORTED_BOOLEAN_OPERATOR,
                        "!AND", "Server only supports AND operator so far");
            }
        }
        if (!isUsable()) {
            throw new SRUException(SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
                "Server currently supportes term-only query "
                + "(CQL conformance level 0).");
        }
    }

    public void initialise(SRURequest request) throws SRUException {
        System.out.println("DEBUG query type: " + request.getQueryType());
        if (request.isQueryType(Constants.FCS_QUERY_TYPE_FCS)) {
            /*
             * Got a FCS query (SRU 2.0).
             * Translate to a DB search string
             */
            final FCSQuery query = request.getQuery(FCSQuery.class);
            fcsString = query.getParsedQuery().toString();
            System.out.println("FCS DEBUG: " + fcsString);
            initialise(query.getParsedQuery());
        } else if (request.isQueryType(Constants.FCS_QUERY_TYPE_CQL)) {
            /*
             * Got a CQL query (either SRU 1.1 or higher).
             * Translate to a DB search string
             */
            final CQLQuery query = request.getQuery(CQLQuery.class);
            cqlString = query.getParsedQuery().toCQL();
            System.out.println("CQL DEBUG: " + cqlString);
            initialise(query.getParsedQuery(), 0);
        } else {
            /*
             * Got something else we don't support. Send error ...
             */
            throw new SRUException(
                    SRUConstants.SRU_CANNOT_PROCESS_QUERY_REASON_UNKNOWN,
                    "Queries with queryType '" +
                            request.getQueryType() +
                    "' are not supported by this HZSK FCS Endpoint.");
        }
    }

    public HZSKQuery(String textSearch) {
        this.textSearch = new ArrayList<String>();
        posSearch = new ArrayList<String>();
        lemmaSearch = new ArrayList<String>();
        this.textSearch.add(textSearch);
        this.usable = true;
    }

    public HZSKQuery(String layer, String layerSearch) {
        textSearch = new ArrayList<String>();
        posSearch = new ArrayList<String>();
        lemmaSearch = new ArrayList<String>();
        if (layer.equals("pos")) {
            this.posSearch.add(layerSearch);
            this.usable = true;
        } else if (layer.equals("lemma")) {
            this.lemmaSearch.add(layerSearch);
            this.usable = true;
        } else if (layer.equals("text")) {
            this.textSearch.add(layerSearch);
            this.usable = true;
        } else {
            this.usable = false;
        }
    }

    // @fixme there's no sanity checks here just overwriting
    public void combineOr(HZSKQuery rhs) {
        textSearch.addAll(rhs.textSearch);
        posSearch.addAll(rhs.posSearch);
        lemmaSearch.addAll(rhs.lemmaSearch);
        combine = ComboType.OR;
        usable = true;
    }

    public void combineAnd(HZSKQuery rhs) {
        textSearch.addAll(rhs.textSearch);
        posSearch.addAll(rhs.posSearch);
        lemmaSearch.addAll(rhs.lemmaSearch);
        combine = ComboType.AND;
        usable = true;
    }

    public boolean isComplex() {
        if (combine != ComboType.WHATEVER) {
            return true;
        }
        if ((hasTextSearch() && (hasPosSearch() || hasLemmaSearch()))) {
            // TEXT plus something
            return true;
        }
        if ((textSearch.size() > 1) || (lemmaSearch.size() > 1) ||
            (posSearch.size() > 1)) {
            return true;
        }
        return false;
    }

    public boolean hasTextSearch() {
        if (this.usable) {
            return this.textSearch.size() > 0;
        } else {
            return false;
        }
    }

    public boolean hasNonTextSearch() {
        if (this.usable) {
            return hasPosSearch() || hasLemmaSearch();
        } else {
            return false;
        }
    }

    public boolean hasPosSearch() {
        if (this.usable) {
            return this.posSearch.size() > 0;
        } else {
            return false;
        }
    }

    public boolean hasLemmaSearch() {
        if (usable) {
            return lemmaSearch.size() > 0;
        } else {
            return false;
        }
    }
    public void setTextSearch(String search) {
        textSearch.clear();
        textSearch.add(search);
        usable = true;
    }

    public void setPosSearch(String search) {
        posSearch.clear();
        posSearch.add(search);
        usable = true;
    }

    public void setLemmaSearch(String search) {
        lemmaSearch.clear();
        lemmaSearch.add(search);
        usable = true;
    }

    public boolean isUsable() {
        if (!usable) {
            return false;
        }
        if (!hasTextSearch() && !hasLemmaSearch() && !hasPosSearch()) {
            return false;
        }
        return true;
    }

    public String getTextSearch() {
        return textSearch.get(0);
    }

    public List<String> getTextSearches() {
        return textSearch;
    }

    public String getPosSearch() {
        return posSearch.get(0);
    }

    public List<String> getPosSearches() {
        return posSearch;
    }

    public String getLemmaSearch() {
        return lemmaSearch.get(0);
    }

    public List<String> getLemmaSearches() {
        return lemmaSearch;
    }

    public HZSKQuery.ComboType getCombinator() {
        return combine;
    }


}
