package skos.resource;

import java.util.HashMap;

/**
 * Created by lns16 on 7/27/2017.
 */
public class OAGISAcronyms {

    public static final HashMap<String, String> BOD_ACRONYM_DICTIONARY;

    static {
        BOD_ACRONYM_DICTIONARY = new HashMap<String, String>() {
            {
                put("ABIE", "Aggregate Business Information Entity"); //check
                put("ASN", "Advanced Shipment Notice");
                put("BOD", "Business Object Document");
                put("BOM", "Bill of Materials");
                put("BSR", "Business Service Request");
                put("CFA", "Chartered Financial Analyst"); //check
                put("CHK", "Check");
                put("CMMS", "Computerized Maintenance Management System");
                put("CNC", "Computer Numerical Control");
                put("CRM", "Customer Relationship Management");
                put("CSM", "Component Supplier Management");
                put("DHL", "Carrier like UPS");
                put("ERP", "Enterprise Resource Planning");
                put("HRMS", "Human Resource Management System");
                put("ISBN", "International Standard Book Number");
                put("LIMS", "Laboratory Information Management System");
                put("LTL", "Less Than Truck Load");
                put("MRP", "Material Resource Planning");
                put("OAGIS", "Open Applications Group Integration Specification");
                put("PDM", "Product Data Management");
                put("RFID", "Radio Frequency Identifier");
                put("RFQ", "Request for Quote");
                put("SSCC", "Serial Shipping Container Code");
                put("UCC", "Uniform Code Council");
                put("UOM", "Unit of Measure");
                put("VRML", "Virtual Reality Markup Language");
                put("WIP", "Work In Progress");
            }
        };
    }

}
