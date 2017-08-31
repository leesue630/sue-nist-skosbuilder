package skos.entity;

/**
 * Created by lns16 on 7/27/2017.
 */
public class ACC extends AbstractConcept {

    private Long basedAccId;

    private Boolean validOCT;

    public Boolean hasValidOCT() {
        return validOCT;
    }

    public void setValidOCT(Boolean validOCT) {
        this.validOCT = validOCT;
    }

    public Long getBasedAccId() {
        return basedAccId;
    }

    public void setBasedAccId(Long basedAccId) {
        this.basedAccId = basedAccId;
    }



}
