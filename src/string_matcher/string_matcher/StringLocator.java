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
    private static String textUrl = "http://norvig.com/big.txt";

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException
    {
        executeSearchInBatches();
        printResults();
    }

    private static void executeSearchInBatches() throws InterruptedException, IOException
    {
        ExecutorService executor = Executors.newFixedThreadPool( 10);
        multiThreadSearchBatches(executor);
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
    }

    private static void multiThreadSearchBatches(ExecutorService executor) throws IOException
    {
        Scanner s = getScannerOfUrlText(textUrl);
        int batchNumber = 0;
        while (s.hasNextLine() )
        {
            makeBatchSearch(s, executor, batchNumber++);
        }
        s.close();
    }

    private static void makeBatchSearch(Scanner s, ExecutorService executor, int batchNumber)
    {
        StringBuilder lineBuilder = new StringBuilder();
        int line =0;
        while (s.hasNextLine() && line < BATCH_SIZE)
        {
            lineBuilder.append(s.nextLine()).append("\n");
            line++;
        }
        searchInNewThread(lineBuilder, executor, batchNumber);
    }

    private static void searchInNewThread(StringBuilder lineBuilder, ExecutorService executor, int batchNumber)
    {
        futures.add(executor.submit(findFirstNamesMatcher(lineBuilder.toString(), batchNumber)));
    }

    private static void printResults() throws ExecutionException, InterruptedException
    {
        StringMapAggregator aggregator = aggregateMapsFromFutures();
        String report = aggregator.createReport();
        System.out.println(report);
    }

    private static StringMapAggregator aggregateMapsFromFutures() throws InterruptedException, ExecutionException
    {
        StringMapAggregator aggregator = new StringMapAggregator(null);
        for(Future<Map<String, List<Pair<Integer, Integer>>>> future : futures)
        {
            aggregator.aggregate(future.get());
        }
        return aggregator;
    }

    private static Scanner getScannerOfUrlText(String textUrl) throws IOException
    {
        URL url = new URL(textUrl);
        return new Scanner(url.openStream());
    }

    private static Callable findFirstNamesMatcher(String batch, int i)
    {
        return new FirstNamesMatcher(batch, i, NAMES);
    }
}
