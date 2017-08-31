package skos.entity;

import org.semanticweb.skos.*;
import skos.SKOSBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lns16 on 7/27/2017.
 */
public class OAGISScheme {
    private SKOSDataFactory thisFactory = SKOSBuilder.factory;
    private SKOSConceptScheme thisScheme;
    private List<SKOSObjectRelationAssertion> objRelAsserts = new ArrayList<>();
    private List<SKOSAnnotationAssertion> annAsserts = new ArrayList<>();
    private List<SKOSChange> allAsserts = new ArrayList<>();

    public OAGISScheme(String title, String baseURI) throws SKOSCreationException {
        thisScheme = thisFactory.getSKOSConceptScheme(URI.create(baseURI + title));
    }

    public void addObjectRelation(ObjRel objProp, SKOSEntity entity2){
        SKOSObjectRelationAssertion objRelAssert;
        switch (objProp){
            case hasTopConcept:
                objRelAssert = thisFactory.getSKOSObjectRelationAssertion(thisScheme, thisFactory.getSKOSHasTopConceptProperty(), entity2);
                break;
            default:
                throw new IllegalArgumentException("Invalid Object Relation");
        }
        objRelAsserts.add(objRelAssert);
    }

    public void addAnnotationRelation(Annotation annType, String text){
        SKOSAnnotation ann;
        switch (annType) {
            case title:
                ann = thisFactory.getSKOSAnnotation(URI.create("http://purl.org/dc/terms/title"), text, "en");
                break;
            default:
                throw new IllegalArgumentException("Invalid Annotation");
        }
        annAsserts.add(thisFactory.getSKOSAnnotationAssertion(thisScheme, ann));
    }

    private SKOSEntityAssertion getEntityAssertion(){
        return thisFactory.getSKOSEntityAssertion(thisScheme);
    }

    public SKOSConceptScheme getScheme(){
        return thisScheme;
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
