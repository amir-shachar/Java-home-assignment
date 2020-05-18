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
    private static List<Future> futures = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
    {
        ArrayList<String> batches = divideToBatchesOfSize("http://norvig.com/big.txt");

        ExecutorService executor = Executors.newFixedThreadPool(batches.size() / 10);
        runThreadsOnBatches(batches, executor);

        printResults();
    }

    private static void printResults() throws ExecutionException, InterruptedException
    {
        StringMapAggregator aggregator = new StringMapAggregator(null);
        for(Future<Map<String, List<Pair<Integer, Integer>>>> future : futures)
        {
            aggregator.aggregate(future.get());
        }
        String report = aggregator.createReport();
        System.out.println(report);
    }

    private static void runThreadsOnBatches(ArrayList<String> batches, ExecutorService executor) throws InterruptedException
    {
        for (int i = 0; i < batches.size(); i++)
        {
            futures.add(executor.submit(findFirstNamesMatcher(batches.get(i), i)));
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
        batches.add(lineBuilder.toString());
    }

    private static Callable findFirstNamesMatcher(String batch, int i)
    {
        return new FirstNamesMatcher(batch, i, NAMES);
    }
}
