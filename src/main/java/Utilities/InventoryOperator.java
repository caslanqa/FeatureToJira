package Utilities;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryOperator {
    private static String getFeatureLine(List<String> fileAsList) {
        return fileAsList.stream().filter(line->line.trim().startsWith("Feature:")).collect(Collectors.toList()).toString();
    }

    public static boolean getMessage(int size) {
        return true;
    }

    private static List<Integer> getTitleIndexes(List<String> fileAsList){
        List<Integer> temp = new ArrayList<>();
        for (int i = 1; i < fileAsList.size(); i++) {
            if(fileAsList.get(i).startsWith("Scenario"))
                temp.add(i);
        }
        return temp;
    }

    private static List<List<String>> getScenarioList(List<String> fileAsList){
        List<Integer> indxs = getTitleIndexes(fileAsList);
        List<List<String>> scenarioList = new ArrayList<>();
        for (int i = 0; i < indxs.size(); i++) {
            List<String> scenario = new ArrayList<>();
            int toIndx = i+1 < indxs.size()?indxs.get(i+1):fileAsList.size();
            for (int j = indxs.get(i); j < toIndx ; j++) {
                scenario.add(fileAsList.get(j));
            }
            scenarioList.add(scenario);
        }
        return scenarioList;
    }

    public static List<List<String>> multiplyWithExamples(List<String> fileAsList){
        List<List<String>> lists = getScenarioList(fileAsList);
        List<List<String>> result = new ArrayList<>();
        result.add(Arrays.asList(fileAsList.get(0)));
        for (int i = 0; i < lists.size(); i++) {
            List<String> scenario = lists.get(i);
            List<String> examples = new ArrayList<>();
            if(scenario.get(0).startsWith("Scenario Outline")){
                List<Map<String, String>> exampleList = new ArrayList<>();

                for (int j = 0; j < scenario.size(); j++) {
                    if(scenario.get(j).startsWith("Examples")){
                        examples = scenario.subList(j,scenario.size());
                        scenario = scenario.subList(0,j);
                        break;
                    }
                }

                List<String> header = getTextList(examples.get(1));
                for (int k = 2; k < examples.size(); k++) {
                    List<String> line = getTextList(examples.get(k));
                    Map<String, String> exampleLine = new HashMap<>();
                    for (int z = 0; z < line.size(); z++) {
                        exampleLine.put(header.get(z), line.get(z));
                    }
                    exampleList.add(exampleLine);
                }

                int iter = 1;

                for(Map<String, String> ex : exampleList){
                    List<String> scenarioTemp = new ArrayList<>(scenario);
                    for(String x : ex.keySet()){
                        for (int a = 1; a < scenario.size() ; a++) {
                            String str = "<"+x+">";
                            if (scenarioTemp.get(a).contains(str)) {
                                scenarioTemp.set(a, scenarioTemp.get(a).replace(str, ex.get(x)));
                            }
                        }
                    }
                    scenarioTemp.set(0,scenarioTemp.get(0).replace(" Outline","").concat(" || Example = "+(iter++)));
                    result.add(scenarioTemp);
                }
            }else {
                result.add(lists.get(i));
            }


        }
        return result;
    }

    private static List<String> getTextList(String str){
        return Arrays.stream(str.trim().split("\\|")).map(x->x.trim()).filter(x->!x.isEmpty()).collect(Collectors.toList());
    }

    private static List<String> getSummaries(List<String> fileAsList){
        List<List<String>> lists = multiplyWithExamples(fileAsList);
        List<String> summaries = new ArrayList<>();
        for (int i = 1; i <lists.size() ; i++) {
            String summary = (lists.get(0).get(0).trim()).concat(" => ").concat(lists.get(i).get(0).trim().split(":").length<2?"":lists.get(i).get(0).trim().split(":")[1].trim());
            summaries.add(summary);
        }
        return summaries;
    }
}
