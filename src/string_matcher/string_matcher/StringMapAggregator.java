package string_matcher;

import javafx.util.Pair;

import java.util.*;

public class StringMapAggregator
{
    private Map<String, List<Pair<Integer, Integer>>> map;

    public StringMapAggregator(Map<String, List<Pair<Integer, Integer>>> map)
    {
        this.map = Objects.requireNonNullElseGet(map, HashMap::new);
    }

    public void aggregate(Map<String, List<Pair<Integer, Integer>>> newMap)
    {
        for(String name: newMap.keySet())
        {
            List<Pair<Integer, Integer>> addition = newMap.get(name);
            if(map.keySet().contains(name))
            {
                map.get(name).addAll(addition);
            }
            else
            {
                map.put(name, addition);
            }
        }
    }

    public String createReport()
    {
        sortMap();
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : map.entrySet())
        {
            createReportForSingleWord(text, entry);
        }
        return text.toString();
    }

    private void createReportForSingleWord(StringBuilder text, Map.Entry<String, List<Pair<Integer, Integer>>> entry)
    {
        text.append('"').append(entry.getKey()).append('"').append(" appeared in text at:\n");
        for (Pair<Integer, Integer> indices : entry.getValue())
        {
            text.append("\tLine ").append(indices.getKey()).append(" at Charoffset ")
                    .append(indices.getValue()).append("\n");
        }
    }

    private void sortMap()
    {
        for (String name : map.keySet())
        {
            List<Pair<Integer, Integer>> list = map.get(name);
            list.sort(Comparator.comparingInt(Pair::getKey));
            map.put(name, list);
        }
    }
}
