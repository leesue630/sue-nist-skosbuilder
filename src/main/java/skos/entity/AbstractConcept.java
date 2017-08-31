package skos.entity;

/**
 * Created by lns16 on 8/23/2017.
 */
public abstract class AbstractConcept {

    private String termName;
    private String den;
    private String definition;

    public String getDefinition() { return definition; }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

}
