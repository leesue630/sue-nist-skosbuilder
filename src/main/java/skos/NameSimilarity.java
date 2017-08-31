package skos;

import org.apache.commons.lang.StringUtils;
import skos.entity.OAGISConcept;

import java.util.*;

/**
 * Created by lns16 on 8/28/2017.
 */
public class NameSimilarity {

    public static void checkAllConceptNames(List<String> allBccps, Map<String, OAGISConcept> allConcepts) {
        for (String con1 : allBccps) {
            for (String con2 : allBccps) {
                String similar = processNameLengths(con1, con2);

                if (!similar.equals("") && (!allConcepts.get(con1).hierarchyExists(con2))) {
                    switch (similar) {
                        case "n":
                            System.out.println(con2 + " broader of: " + con1);
                            SKOSBuilder.setHierarchy(con2, con1, allConcepts);
                            break;
                        case "b":
                            System.out.println(con1 + " broader of: " + con2);
                            SKOSBuilder.setHierarchy(con1, con2, allConcepts);
                            break;
                    }
                }

            }
        }
    }

    private static String processNameLengths(String s1, String s2) {
        if (StringUtils.getLevenshteinDistance(s1, s2) > 14) {
            return "";
        }

        int longSNum;

        if (s1.equals(s2)) {
            return "";
        }

        List<String> tokens1 = new ArrayList<>(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(s1)));
        List<String> tokens2 = new ArrayList<>(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(s2)));
        if (tokens1.size() == tokens2.size()) {
            return "";
        }
        if (tokens1.size() > tokens2.size()) {
            longSNum = 1;
            return similar(s1, s2, tokens1, tokens2, longSNum);
        } else {
            longSNum = 2;
            return similar(s2, s1, tokens2, tokens1, longSNum);
        }
    }

    private static String similar(String longS, String shortS, List<String> longTokens, List<String> shortTokens, int longSNum) {
        if (longTokens.size() - shortTokens.size() == 1 && longS.matches(".*" + shortS + "$")) {
            switch (longSNum) {
                case 1:
                    return "n";
                case 2:
                    return "b";
                default:
                    System.err.println("ERROR");
            }
        }
        if (shortTokens.size() == 1) {
            return "";
        }
        String endOfShort = listToString(shortTokens.subList(1, shortTokens.size()));

        if (longS.matches("^" + shortTokens.get(0) + "[A-Z].*" + endOfShort + "$")) {
            if (StringUtils.splitByCharacterTypeCamelCase(longS.replace(shortTokens.get(0), "").replace(endOfShort, "")).length > 1) { // multiple words inside
                return "";
            } else {
                switch (longSNum) {
                    case 1:
                        return "n";
                    case 2:
                        return "b";
                    default:
                        System.err.println("ERROR");
                }
            }
        }
        return "";
    }

    private static String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }
        return sb.toString();
    }
}
