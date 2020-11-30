package search;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        String fileName = getFileName(args);
        String[] records = getRecords(fileName);
        HashMap<String, ArrayList<Integer>> index = createInvertedIndex(records);
        dispatch(records, index);
    }

    public static HashMap<String, ArrayList<Integer>> createInvertedIndex(String[] records) {
        HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
        for (int i = 0; i < records.length; i++) {
            String[] parts = records[i].split("\\s+");
            for (String part : parts) {
                if (invertedIndex.containsKey(part.toUpperCase())) {
                    ArrayList<Integer> value = invertedIndex.get(part.toUpperCase());
                    value.add(i);
                    invertedIndex.put(part.toUpperCase(), value);
                } else {
                    ArrayList<Integer> newValue = new ArrayList<>();
                    newValue.add(i);
                    invertedIndex.put(part.toUpperCase(), newValue);
                }
            }
        }
        return invertedIndex;
    }

    public static String[] getRecords(String fileName) {
        try {
            String fileData = readFileAsString(fileName);
            return fileData.split("\n");
        } catch (IOException e) {
            System.out.println("Error: Cannot read file: " + e.getMessage());
        }
        return new String[] {""};
    }

    public static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public static String getFileName(String[] args) {
        String in = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--data")) {
                try {
                    return args[i + 1];
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        return in;
    }
    
    public static void printRecords(String[] records) {
        for (int i = 0; i < records.length; i++) {
            System.out.println(records[i]);
        }
    }

    public static String getSearchResult(String[] records, HashMap<String, ArrayList<Integer>> invertedIndex, String query, String strategy) {
        StringBuilder result = new StringBuilder();
        String[] queryParts = query.split("\\s+");

        HashSet<Integer> totalFound = new HashSet<>();
        for (String currentQuery : queryParts) {
            ArrayList<Integer> queryResult = invertedIndex.getOrDefault(currentQuery.toUpperCase(), new ArrayList<>());
            HashSet<Integer> foundInCurrentQuery = new HashSet<>(queryResult);
            if (strategy.equalsIgnoreCase("ANY")) {
                totalFound.addAll(queryResult);
            } else if (strategy.equalsIgnoreCase("ALL")) {
                if (foundInCurrentQuery.isEmpty()) {
                    totalFound = new HashSet<>();
                    break;
                } else {
                    if (totalFound.isEmpty()) {
                        totalFound = foundInCurrentQuery;
                    } else {
                        totalFound.retainAll(foundInCurrentQuery);
                    }
                }
            } else {
                totalFound.addAll(queryResult);
            }
        }

        if (strategy.equalsIgnoreCase("NONE")) {
            for (int i = 0; i < records.length; i++) {
                if (!totalFound.contains(i)) {
                    result.append(records[i] + "\n");
                }
            }
        } else {
            for (int elem : totalFound) {
                result.append(records[elem] + "\n");
            }
        }
        return result.toString();
    }

    public static boolean checkStrategyInput(String strategy) {
        HashSet<String> availableStrategies = new HashSet<>();
        availableStrategies.add("ALL");
        availableStrategies.add("ANY");
        availableStrategies.add("NONE");
        if (availableStrategies.contains(strategy.toUpperCase())) {
            return true;
        }
        return false;
    }
    
    public static void findRecords(String[] records, HashMap<String, ArrayList<Integer>> invertedIndex) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select a matching strategy: ALL, ANY, NONE");
        String strategy = scanner.nextLine();
        if (!checkStrategyInput(strategy)) {
            System.out.println("Unknown strategy");
            return;
        }

        String query = scanner.nextLine();

        String result = getSearchResult(records, invertedIndex, query, strategy);
        
        if (result.length() == 0) {
            System.out.println("No matching records found.");
        } else {
            System.out.println("Found records:");
            System.out.println(result);
        }
        
    }
    
    public static void showMenu() {
        System.out.println("=== Menu ===");
        System.out.println("1. Find a record");
        System.out.println("2. Print all records");
        System.out.println("0. Exit");
    }
    
    public static void dispatch(String[] records, HashMap<String, ArrayList<Integer>> index) {
        Scanner scanner = new Scanner(System.in); 
        boolean isRunning = true;
        while (isRunning) {
            showMenu();
            int action = scanner.nextInt();
            switch(action) {
                case 1:
                    findRecords(records, index);
                    break;
                case 2:
                    printRecords(records);
                    break;
                case 0:
                    isRunning = false;
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Incorrect option! Try again.");
                    break;
            }
        }
    }
}
