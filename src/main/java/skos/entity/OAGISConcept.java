package skos.entity;

import org.semanticweb.skos.*;
import skos.SKOSBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lns16 on 7/27/2017.
 */
public class OAGISConcept {
    private SKOSDataFactory thisFactory = SKOSBuilder.factory;
    private SKOSConcept skosConcept;
    private List<SKOSObjectRelationAssertion> objRelAsserts = new ArrayList<>();
    private List<SKOSAnnotationAssertion> annAsserts = new ArrayList<>();
    private List<SKOSChange> allAsserts = new ArrayList<>();
    public List<String> altLabels = new ArrayList<>();
    private List<String> broaderConcepts = new ArrayList<>();
    private List<String> narrowerConcepts = new ArrayList<>();
    private List<String> definitions = new ArrayList<>();

    public String getPrefLabel() {
        return prefLabel;
    }

    private String prefLabel;

    public Boolean altLabelExists(String alt) {
        return altLabels.contains(alt);
    }

    public Boolean hierarchyExists(String con) {
        return (broaderConceptExists(con) || (narrowerConceptExists(con)));
    }

    public Boolean broaderConceptExists(String con) {
        return broaderConcepts.contains(con);
    }

    public Boolean narrowerConceptExists(String con) {
        return narrowerConcepts.contains(con);
    }

    public Boolean definitionExists(String def) {
        return definitions.contains(def);
    }

    public int broaderConceptsSize() { return broaderConcepts.size(); }

    public OAGISConcept(String prefLabel, String baseURI) throws SKOSCreationException{
        this.prefLabel = prefLabel;
        prefLabel = prefLabel.replace("Identifier", "ID");
        skosConcept = thisFactory.getSKOSConcept(URI.create(baseURI + prefLabel));
        this.addAnnotationRelation(Annotation.prefLabel, prefLabel);
    }

    public void addObjectRelation(ObjRel objProp, SKOSEntity entity2, String entity2Name){
        SKOSObjectRelationAssertion objRelAssert;
        switch (objProp){
            case inScheme:
                objRelAssert = thisFactory.getSKOSObjectRelationAssertion(skosConcept, thisFactory.getSKOSInSchemeProperty(), entity2);
                break;
            case broader:
                objRelAssert = thisFactory.getSKOSObjectRelationAssertion(skosConcept, thisFactory.getSKOSBroaderProperty(), entity2);
                broaderConcepts.add(entity2Name);
                break;
            case narrower:
                objRelAssert = thisFactory.getSKOSObjectRelationAssertion(skosConcept, thisFactory.getSKOSNarrowerProperty(), entity2);
                narrowerConcepts.add(entity2Name);
                break;
            case related:
                objRelAssert = thisFactory.getSKOSObjectRelationAssertion(skosConcept, thisFactory.getSKOSRelatedProperty(), entity2);
                break;
            case topConceptOf:
                objRelAssert = thisFactory.getSKOSObjectRelationAssertion(skosConcept, thisFactory.getSKOSTopConceptOfProperty(), entity2);
                break;
            default:
                throw new IllegalArgumentException("Invalid Object Relation");
        }
        objRelAsserts.add(objRelAssert);
    }

    public void addAnnotationRelation(Annotation annType, String text){
        SKOSAnnotation ann;
        switch (annType) {
            case prefLabel:
                ann = thisFactory.getSKOSAnnotation(thisFactory.getSKOSPrefLabelProperty().getURI(), text, "en");
                break;
            case altLabel:
                ann = thisFactory.getSKOSAnnotation(thisFactory.getSKOSAltLabelProperty().getURI(), text, "en");
                altLabels.add(text);
                break;
            case definition:
                ann = thisFactory.getSKOSAnnotation(thisFactory.getSKOSDefinitionObjectProperty().getURI(), text, "en");
                definitions.add(text);
                break;
            default:
                throw new IllegalArgumentException("Invalid Annotation");
        }
        annAsserts.add(thisFactory.getSKOSAnnotationAssertion(skosConcept, ann));
    }

    private SKOSEntityAssertion getEntityAssertion(){
        return thisFactory.getSKOSEntityAssertion(skosConcept);
    }

    public SKOSConcept getSKOSConcept(){
        return skosConcept;
    }

    public List<SKOSChange> getAllAsserts(SKOSDataset dataset){
        allAsserts.add(new AddAssertion(dataset, this.getEntityAssertion()));
        for (SKOSAnnotationAssertion ass : annAsserts) {
            allAsserts.add(new AddAssertion(dataset, ass));
        }
        for (SKOSObjectRelationAssertion ass : objRelAsserts) {
            allAsserts.add(new AddAssertion(dataset, ass));
        }
        return allAsserts;
    }
}