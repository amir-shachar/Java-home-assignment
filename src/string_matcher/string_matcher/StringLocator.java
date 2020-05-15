package string_matcher;

import javafx.util.Pair;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class StringLocator
{
    public static final int BATCH_SIZE = 1000;
    private static final String[] NAMES = {"james", "daniel", "george", "arthur", "charles", "richard"};

    public static void main(String[] args) throws IOException, InterruptedException
    {
        ArrayList<String> batches = divideToBatchesOfSize("http://norvig.com/big.txt");
        System.out.println("Division to batches is completed! \n");

        ExecutorService executor = Executors.newFixedThreadPool(batches.size() / 10);
        Map<String, List<Pair<Integer, Integer>>> resultMap = new HashMap<>();
        long startTime = System.nanoTime();
        runThreadsOnBatches(batches, executor, resultMap);
        startTime = System.nanoTime() - startTime;
        printResults(resultMap);
        System.out.println("operation string search took :" + startTime / 1000_000_000.0 + " seconds.");
    }

    private static void printResults(Map<String, List<Pair<Integer, Integer>>> resultMap)
    {
        String report = new StringMapAggregator(resultMap).createReport();
        System.out.println(report);
    }

    private static void runThreadsOnBatches(ArrayList<String> batches, ExecutorService executor, Map<String, List<Pair<Integer, Integer>>> resultMap) throws InterruptedException
    {
        for (int i = 0; i < batches.size(); i++)
        {
            executor.submit(findFirstNamesMatcher(batches.get(i), i, resultMap));
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
    }

    private static Scanner getScannerOfUrlText(String textUrl) throws IOException
    {
        URL url = new URL(textUrl);
        return new Scanner(url.openStream());
    }

    private static ArrayList<String> divideToBatchesOfSize(String textUrl) throws IOException
    {
        ArrayList<String> batches = new ArrayList<>();
        Scanner s = getScannerOfUrlText(textUrl);
        iterateTextLines(batches, s);
        return batches;
    }

    private static void iterateTextLines(ArrayList<String> batches, Scanner s)
    {
        StringBuilder lineBuilder = new StringBuilder();
        int lineCount = 0;
        while (s.hasNextLine())
        {
            lineBuilder.append(s.nextLine()).append("\n");
            lineCount++;
            if (lineCount == BATCH_SIZE)
            {
                batches.add(lineBuilder.toString());
                lineBuilder = new StringBuilder();
                lineCount = 0;
            }
        }
    }

    private static Runnable findFirstNamesMatcher(String batch, int i, Map<String, List<Pair<Integer, Integer>>> resultMap)
    {
        return new FirstNamesMatcher(batch, i, resultMap, NAMES);
    }
}
