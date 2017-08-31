package skos;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.skos.*;
import org.semanticweb.skosapibinding.SKOSManager;
import skos.dao.ACCDAO;
import skos.dao.ASCCPDAO;
import skos.dao.BCCPDAO;
import skos.entity.*;
import skos.resource.OAGISAcronyms;
import uk.ac.manchester.cs.skos.SKOSDatasetImpl;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by lns16 on 8/7/2017.
 */
public class SKOSBuilder {

    private static final String baseURI = "http://www.oagi.org/skos#";
    private static final String filePrefix = "file:///C:/Users/lns16/Documents/skosbuilder/";

    private SKOSManager manager;
    public static SKOSDataFactory factory;
    private SKOSDataset dataset;

    private ACCDAO accDao = new ACCDAO();

    private int accCount = 0;
    private int asccpCount = 0;
    private int bccpCount = 0;

    private Map<String, OAGISConcept> allConcepts = new HashMap<>();
    private List<String> allBccps = new ArrayList<>();
    private OAGISScheme topConceptScheme;

    public SKOSBuilder(String scheme) throws SKOSCreationException {
        manager = new SKOSManager();
        factory = manager.getSKOSDataFactory();
        dataset = manager.createSKOSDataset(URI.create(baseURI));
        createScheme(scheme);
    }

    private void createScheme(String name) throws SKOSCreationException {
        topConceptScheme = new OAGISScheme(name.replaceAll(" ", ""), baseURI);
        topConceptScheme.addAnnotationRelation(Annotation.title, name);
    }

    // creates concept and sets prefLabel
    private void createOAGISConcept(String name) throws SKOSCreationException {
        allConcepts.put(name, new OAGISConcept(name, baseURI));
    }

    private void createOAGISConcept(String name, String den, String termName, String def) throws SKOSCreationException {
        // prefLabel
        allConcepts.put(name, new OAGISConcept(name, baseURI)); // termName upper camel case (prefLabel)

        // altLabels
        setLabel(name, Annotation.altLabel, expandAcronym(termName)); // termName w/ acronyms expanded
        setLabel(name, Annotation.altLabel, den); // den
        setLabel(name, Annotation.altLabel, termName); // termName w/ spaces
        setLabel(name, Annotation.altLabel, expandAcronym(den)); // den w/ acronyms expanded

        // definition
        setLabel(name, Annotation.definition, def); // den w/ acronyms expanded
    }

    private void setScheme(String concept) {
        topConceptScheme.addObjectRelation(ObjRel.hasTopConcept, allConcepts.get(concept).getSKOSConcept());
        allConcepts.get(concept).addObjectRelation(ObjRel.topConceptOf, topConceptScheme.getScheme(), null);
        allConcepts.get(concept).addObjectRelation(ObjRel.inScheme, topConceptScheme.getScheme(), null);
    }

    public static void setHierarchy(String broad, String narrow, Map<String, OAGISConcept> allConcepts) {
        allConcepts.get(broad).addObjectRelation(ObjRel.narrower, allConcepts.get(narrow).getSKOSConcept(), narrow);
        allConcepts.get(narrow).addObjectRelation(ObjRel.broader, allConcepts.get(broad).getSKOSConcept(), broad);
    }

    // checks that label is not null
    //             altLabel is not equal to prefLabel
    //             altLabel doesn't already exist
    private void setLabel(String concept, Annotation annType, String label) {
        OAGISConcept con = allConcepts.get(concept);
        if (label != null) {
            switch (annType) {
                case altLabel:
                    if (!con.getPrefLabel().equals(label) && !con.altLabelExists(label)) {
                        allConcepts.get(concept).addAnnotationRelation(annType, label);
                    }
                    break;
                case definition:
                    if (!con.definitionExists(label)) {
                        allConcepts.get(concept).addAnnotationRelation(annType, label);
                    }
                    break;
                default:
                    allConcepts.get(concept).addAnnotationRelation(annType, label);
            }
        }
    }

    // returns name only if changed
    public static String expandAcronym(String name) {
        Boolean changed = false;
        for (String term : name.split("[ \\.]")) {
            if (OAGISAcronyms.BOD_ACRONYM_DICTIONARY.containsKey(term)) {
                name = name.replace(term, OAGISAcronyms.BOD_ACRONYM_DICTIONARY.get(term));
                changed = true;
            }
        }
        if (changed){
            return name;
        } else {
            return null;
        }
    }

    private String compareRoleOfAccIdConceptNames(String str1, String str2) {
        if (str1.contains(str2)) { // checks if str1 is narrower than str2
            return "n"; // str1 is narrower than str2
        } else if (str2.contains(str1)) { // checks if str2 is narrower than str1
            return "b"; // str1 is broader than str2
        }
//            System.err.println(str1 + " has role_of_acc_id of " + str2);
//            String tab = new String(new char[11 - str1.length() / 4]).replace("\0", "\t");
//            anomalies += str1 + tab + str2 + "\n";
        return "";
    }

    private void createAccConcepts() throws SQLException, SKOSCreationException {
        Collection<ACC> accList = accDao.findAcc();

        System.out.println("DEBUG: ACC");

        for (ACC acc : accList) {
            createAccConcept(acc, acc.getTermName().replace(" ", ""));
        }
    }

    private void createAccConcept(ACC acc, String accName) throws SKOSCreationException, SQLException {
        OAGISConcept checkIfExists = allConcepts.get(accName);
        String den = acc.getDen();
        String def = acc.getDefinition();
        ACC basedAcc = null;
        if (acc.getBasedAccId() != null){
            basedAcc = accDao.findAccByAccId(acc.getBasedAccId());
            if (!basedAcc.hasValidOCT()){
                basedAcc = null;
            }
        }

        if (checkIfExists != null) { // concept already exists
            // add altLabels, broader terms, definitions if they don't exist and aren't null
            setLabel(accName, Annotation.altLabel, den); // altLabel
            setLabel(accName, Annotation.altLabel, expandAcronym(den)); // altLabel w/ acronyms expanded
            setLabel(accName, Annotation.definition, def); // definition

            if (basedAcc != null) { // broader concept
                String basedAccName = basedAcc.getTermName().replace(" ", "");
                if (!checkIfExists.broaderConceptExists(basedAccName)) {
                    OAGISConcept broadExists = allConcepts.get(basedAccName);
                    if (broadExists == null) {
                        createAccConcept(basedAcc, basedAccName);
                    }
                    setHierarchy(basedAccName, accName, allConcepts);
                }
            }
        } else { // concept does not exist
            accCount++;
            System.out.println("DEBUG #" + accCount + ": " + acc.getTermName() + " created");
            createOAGISConcept(accName, den, acc.getTermName(), def); // sets prefLabel and 2 altLabels

            if (basedAcc != null) { // broader
                String basedAccName = basedAcc.getTermName().replace(" ", "");
                OAGISConcept broadExists = allConcepts.get(basedAccName);
                if (broadExists == null) {
                    createAccConcept(basedAcc, basedAccName);
                }
                setHierarchy(basedAccName, accName, allConcepts);
            }
        }
    }

    private void createAsccpConcepts() throws SQLException, SKOSCreationException {
        ASCCPDAO asccpDao = new ASCCPDAO();
        Collection<ASCCP> asccpList = asccpDao.findAsccp();

        System.out.println("DEBUG: ASCCP");

        for (ASCCP asccp : asccpList) {
            createAsccpConcept(asccp, asccp.getTermName().replace(" ", ""));
        }
    }

    private void createAsccpConcept(ASCCP asccp, String asccpName) throws SKOSCreationException, SQLException {
        OAGISConcept checkIfExists = allConcepts.get(asccpName);
        String den = asccp.getDen();
        String def = asccp.getDefinition();
        String roleOfAccName = null;
        if (asccp.getRoleOfAccId() != null){
            ACC roleOfAcc = accDao.findAccByAccId(asccp.getRoleOfAccId());
            if (roleOfAcc.hasValidOCT()){
                roleOfAccName = roleOfAcc.getTermName().replace(" ", "");
            }
        }
        if (checkIfExists != null) {
            // add altLabels, broader terms, definitions if they don't exist and aren't null
            setLabel(asccpName, Annotation.altLabel, den); // altLabel
            setLabel(asccpName, Annotation.altLabel, expandAcronym(den)); // altLabel w/ acronyms expanded
            setLabel(asccpName, Annotation.definition, def); // definition

            if (roleOfAccName != null && !checkIfExists.hierarchyExists(roleOfAccName) && !asccpName.equals(roleOfAccName)) { // broader concept
                switch (compareRoleOfAccIdConceptNames(asccpName, roleOfAccName)) {
                    case "n":
                        setHierarchy(roleOfAccName, asccpName, allConcepts);
                        break;
                    case "b":
                        setHierarchy(asccpName, roleOfAccName, allConcepts);
                        break;
                    default:
                        setHierarchy(roleOfAccName, asccpName, allConcepts);
                }
            }

        } else {
            asccpCount++;
            System.out.println("DEBUG #" + asccpCount + ": " + asccp.getTermName() + " created");
            createOAGISConcept(asccpName, den, asccp.getTermName(), def);

            if (roleOfAccName != null) { // broader concept
                switch (compareRoleOfAccIdConceptNames(asccpName, roleOfAccName)) {
                    case "n":
                        setHierarchy(roleOfAccName, asccpName, allConcepts);
                        break;
                    case "b":
                        setHierarchy(asccpName, roleOfAccName, allConcepts);
                        break;
                    default:
                        setHierarchy(roleOfAccName, asccpName, allConcepts);
                }
            }
        }
    }

    private void createBccpConcepts() throws SQLException, SKOSCreationException {
        BCCPDAO bccpDao = new BCCPDAO();
        Collection<BCCP> bccpList = bccpDao.findBccp();

        System.out.println("DEBUG: BCCP");

        for (BCCP bccp : bccpList) {
            createBccpConcept(bccp, bccp.getTermName().replace(" ", ""));
        }
    }

    private void createBccpConcept(BCCP bccp, String bccpName) throws SKOSCreationException {
        OAGISConcept checkIfExists = allConcepts.get(bccpName);
        String den = bccp.getDen();
        String def = bccp.getDefinition();

        if (checkIfExists != null) {
            setLabel(bccpName, Annotation.altLabel, den); // altLabel
            setLabel(bccpName, Annotation.altLabel, expandAcronym(den)); // altLabel w/ acronyms expanded
            setLabel(bccpName, Annotation.definition, def); // definition

        } else {
            bccpCount++;
            System.out.println("DEBUG #" + bccpCount + ": " + bccpName + " created");
            createOAGISConcept(bccpName, den, bccp.getTermName(), def); // set prefLabel (upper camel case), den, propterm w/ spaces, den w/ acronyms
            allBccps.add(bccpName);
        }

    }

    private void setTopConcepts() {
        for (OAGISConcept concept : allConcepts.values()) {
            if (concept.broaderConceptsSize() == 0) {
                setScheme(concept.getPrefLabel());
            }
        }
    }

    private void applyChanges() throws SKOSChangeException {
        List<SKOSChange> allAsserts = new ArrayList<>();

        allAsserts.addAll(topConceptScheme.getAllAsserts(dataset));

        for (Map.Entry<String, OAGISConcept> concept : allConcepts.entrySet()) {
            allAsserts.addAll(concept.getValue().getAllAsserts(dataset));
        }
        manager.applyChanges(allAsserts);
    }

    private void buildSKOS() throws SQLException, SKOSChangeException {
        try {
            accDao.openConnection();
            createAccConcepts();
            createAsccpConcepts();
            accDao.closeQuietly();

            createBccpConcepts();

            setTopConcepts();
            applyChanges();

            createTurtleOntology("baseoagis.concepts.ttl");

        } catch (SKOSCreationException e) {
            e.printStackTrace();
        }
    }

    private void buildSKOSWithNameSorting() throws SQLException, SKOSChangeException {
        try {
            accDao.openConnection();
            createAccConcepts();
            createAsccpConcepts();
            accDao.closeQuietly();

            createBccpConcepts();

            long startTime = System.currentTimeMillis();
            NameSimilarity.checkAllConceptNames(allBccps, allConcepts);
            System.out.println("Checking Concept Names : " + (System.currentTimeMillis() - startTime) + " milliseconds");

            setTopConcepts();
            applyChanges();

            createTurtleOntology("oagisWithNameSorting.concepts.ttl");

        } catch (SKOSCreationException e) {
            e.printStackTrace();
        }
    }

    private void createTurtleOntology(String fileName) {
        SKOSDatasetImpl setImpl = (SKOSDatasetImpl) dataset;
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try {
            man.saveOntology(setImpl.getAsOWLOntology(), new TurtleDocumentFormat(), IRI.create(URI.create(filePrefix + fileName)));
        } catch (OWLOntologyStorageException e) {
            System.err.println("OWLOntologyStorageException");
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
                try {
                    long startTime = System.currentTimeMillis();
                    SKOSBuilder sb = new SKOSBuilder("OAGIS Top Concept Scheme");

                    sb.buildSKOSWithNameSorting();

                    long endTime = System.currentTimeMillis();
                    System.out.println("Entire program took " + (endTime - startTime) + " milliseconds");
                } catch (SKOSCreationException | SKOSChangeException e) {
                    e.printStackTrace();
                }
    }
}