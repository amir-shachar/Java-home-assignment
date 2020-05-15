package string_matcher;

import javafx.util.Pair;

import java.util.*;

import static string_matcher.StringLocator.BATCH_SIZE;

public class FirstNamesMatcher implements Runnable
{
    private final String[] PATTERNS;
    private final static int DECIMAL_BASE = 256;
    private final int PRIME = 2 * 3 * 5 * 7 * 11 + 1;
    private Map<String, List<Pair<Integer, Integer>>> resultMap;
    private final String batch;
    private String currentLine;
    private int batchNumber;
    private int baseHash;

    public FirstNamesMatcher(String batch, int i, Map<String, List<Pair<Integer, Integer>>> resultMap,
                             String[] array)
    {
        this.batchNumber = i;
        this.resultMap = resultMap;
        this.batch = batch;
        this.PATTERNS = array;
    }

    @Override
    public void run()
    {
        int lineNumber = 0;
        Scanner scan = new Scanner(batch);

        while (scan.hasNextLine())
        {
            currentLine = scan.nextLine().toLowerCase();
            searchLineForAllPatterns(lineNumber);
            lineNumber++;
        }
    }

    private void searchLineForAllPatterns(int lineNumber)
    {
        for (String name : PATTERNS)
        {
            baseHash = calculateBaseHash(name.length());
            if (currentLine.length() >= name.length())
                rabinKarpSearch(name, lineNumber);
        }
    }

    private void rabinKarpSearch(String pattern, int lineNumber)
    {
        int patternLen = pattern.length();
        int windowHash = calculateHashOfLength(currentLine, patternLen);
        int patternHash = calculateHashOfLength(pattern, patternLen);

        for (int i = 0; i <= currentLine.length() - patternLen; i++)
        {
            if (isAMatch(pattern, windowHash, patternHash, i))
            {
                putMatchInMap(pattern, lineNumber, i);
            }
            windowHash = updateTextRollingHash(patternLen, i, windowHash);
        }
    }

    private boolean isAMatch(String pattern, int textHash, int patternHash, int i)
    {
        return textHash == patternHash &&
                getMatchLength(pattern, i) == pattern.length();
    }

    private int updateTextRollingHash(int patternLen, int currentIndex, int windowHash)
    {
        if (currentIndex < currentLine.length() - patternLen)
        {
            windowHash = updateWindowHash(patternLen, currentIndex, windowHash);
            if (windowHash < 0)
                windowHash += PRIME;
        }
        return windowHash;
    }

    private int updateWindowHash(int patternLen, int currentIndex, int windowHash)
    {
        return (DECIMAL_BASE * (windowHash - currentLine.charAt(currentIndex) * baseHash)
                + currentLine.charAt(currentIndex + patternLen)) % PRIME;
    }

    private void putMatchInMap(String pat, int lineNumber, int i)
    {
        if (resultMap.containsKey(pat))
        {
            addToExistingList(pat, lineNumber, i);
        }
        else
        {
            createNewMatchList(pat, lineNumber, i);
        }
    }

    private void createNewMatchList(String pat, int lineNumber, int i)
    {
        List<Pair<Integer, Integer>> list = new ArrayList<>();
        list.add(new Pair<>(BATCH_SIZE * batchNumber + lineNumber, i));
        resultMap.put(pat, Collections.synchronizedList(list));
    }

    private void addToExistingList(String pat, int lineNumber, int i)
    {
        List<Pair<Integer, Integer>> pairList = Collections.synchronizedList(resultMap.get(pat));
        synchronized (pairList)
        {
            pairList.add(new Pair<>(BATCH_SIZE * batchNumber + lineNumber, i));
            resultMap.put(pat, pairList);
        }
    }

    private int getMatchLength(String pattern, int startIndex)
    {
        int i = 0;
        while (i < pattern.length() &&
                currentLine.charAt(startIndex + i) == pattern.charAt(i))
        {
            i++;
        }
        return i;
    }

    private int calculateHashOfLength(String txt, int patternLen)
    {
        int textHash = 0;
        for (int i = 0; i < patternLen; i++)
        {
            textHash = (DECIMAL_BASE * textHash + txt.charAt(i)) % PRIME;
        }
        return textHash;
    }

    private int calculateBaseHash(int patternLen)
    {
        int baseHash = 1;
        for (int i = 0; i < patternLen - 1; i++)
        {
            baseHash = (baseHash * DECIMAL_BASE) % PRIME;
        }
        return baseHash;
    }
}