package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.IntPair;
import cs455.hadoop.types.KeyType;
import cs455.hadoop.util.Constants;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

/**
 * Reducer class.  Instead of writing in the reduce function, builds data structures
 * and stores output in memory.  The data is filtered by type into the appropriate map,
 * and sums and averages are calculated. The cleanup function sorts the maps by value
 * and writes out to context.
 */
public class AirlineReducer extends Reducer<KeyType, IntPair, KeyType, IntWritable> {

    private Map<KeyType, IntWritable> timeMap = new HashMap<>();
    private Map<KeyType, IntWritable> dayMap = new HashMap<>();
    private Map<KeyType, IntWritable> monthMap = new HashMap<>();
    private Map<KeyType, IntWritable> planeAgeMap = new HashMap<>();
    private Map<KeyType, IntWritable> planeModelMap = new HashMap<>();
    private Map<KeyType, IntWritable> planeMdlFltsMap = new HashMap<>();
    private Map<KeyType, IntWritable> planeNumberMap = new HashMap<>();
    private Map<KeyType, IntWritable> carrAvgMap = new HashMap<>();
    private Map<KeyType, IntWritable> carrTotMap = new HashMap<>();
    private Map<KeyType, IntWritable> carrMinMap = new HashMap<>();
    private Map<KeyType, IntWritable> cityMap = new HashMap<>();
    private List<Map<KeyType, IntWritable>> arptMap = new ArrayList<>();

    @Override
    protected void setup(Context context) {
        for (int i = 0; i < Constants.NUMBER_OF_YEARS; i++) {
            arptMap.add(new HashMap<>());
        }
    }

    @Override
    protected void reduce(KeyType key, Iterable<IntPair> values, Context context) {
        switch (FieldType.getFieldType(key.getCategory())) {
            case TIME_OF_DAY:
                timeMap.put(new KeyType(key), getAverage(values));
                break;
            case DAY_OF_WEEK:
                dayMap.put(new KeyType(key), getAverage(values));
                break;
            case MONTH_OF_YEAR:
                monthMap.put(new KeyType(key), getAverage(values));
                break;
            case CARRIER_AVG:
                carrAvgMap.put(new KeyType(key), getAverage(values));
                break;
            case PLANE_AGE:
                planeAgeMap.put(new KeyType(key), getAverage(values));
                break;
            case PLANE_MODEL:
                planeModelMap.put(new KeyType(key), getSum(values));
                break;
            case PLANE_NUMBER:
                planeNumberMap.put(new KeyType(key), getSum(values));
                break;
            case PLANE_MODEL_FLIGHTS:
                planeMdlFltsMap.put(new KeyType(key), getSum(values));
                break;
            case CARRIER_TOT:
                carrTotMap.put(new KeyType(key), getSum(values));
                break;
            case CARRIER_MIN:
                carrMinMap.put(new KeyType(key), getSum(values));
                break;
            case WEATHER_CITY:
                cityMap.put(new KeyType(key), getSum(values));
                break;
            case AIRPORT:
                arptMap.get(getYear(key) - Constants.STARTING_YEAR).put(
                        new KeyType(key), getSum(values));
                break;
        }
    }

    /**
     * Actually performs the writing of the output data to context.
     */
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        writeSortedOut(timeMap, context);
        writeSortedOut(dayMap, context);
        writeSortedOut(monthMap, context);
        writeSortedOut(planeAgeMap, context);
        writeSortedOut(planeModelMap, Constants.REPORT_COUNT, context);
        writeSortedOut(planeMdlFltsMap, Constants.REPORT_COUNT, context);
        writeSortedOut(planeNumberMap, Constants.REPORT_COUNT, context);
        writeSortedOut(carrAvgMap, Constants.REPORT_COUNT, context);
        writeSortedOut(carrTotMap, Constants.REPORT_COUNT, context);
        writeSortedOut(carrMinMap, Constants.REPORT_COUNT, context);
        writeSortedOut(cityMap, Constants.REPORT_COUNT, context);
        // Separate map for each year for airport flight totals.
        for (Map<KeyType, IntWritable> map : arptMap) {
            writeSortedOut(map, Constants.REPORT_COUNT, context);
        }
    }

    /**
     * Writes out the values of a sorted map to context.
     */
    private void writeSortedOut(Map<KeyType, IntWritable> unsortedMap, Context context)
            throws IOException, InterruptedException {
        Map<KeyType, IntWritable> sortedMap = sortDescending(unsortedMap);
        for (KeyType key : sortedMap.keySet()) {
            context.write(key, sortedMap.get(key));
        }
    }

    /**
     * Writes out a specified number of sorted map content to context.
     */
    private void writeSortedOut(Map<KeyType, IntWritable> unsortedMap, int count, Context context)
            throws IOException, InterruptedException {
        Map<KeyType, IntWritable> sortedMap = sortDescending(unsortedMap);
        int x = 0;
        for (KeyType key : sortedMap.keySet()) {
            if (x++ == count) break;
            context.write(key, sortedMap.get(key));
        }
    }

    /**
     * Totals sum of the first value in the IntPair.  Second value is ignored.
     */
    private IntWritable getSum(Iterable<IntPair> values) {
        int total = 0;
        for (IntPair val : values) {
            total += val.getFirst();
        }
        return new IntWritable(total);
    }

    /**
     * Calculates average using the running total from the first part of the IntPair
     * and the total count from the second part.
     */
    private IntWritable getAverage(Iterable<IntPair> values) {
        int totalDelay = 0;
        int numFlights = 0;
        for (IntPair val : values) {
            totalDelay += val.getFirst();
            numFlights += val.getSecond();
        }
        int averageDelay = totalDelay / numFlights;
        return new IntWritable(averageDelay);
    }

    private int getYear(KeyType key) {
        String value = key.getValue();
        try {
            return Integer.parseInt(value.substring(0, 4));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Sorts a map by value in descending order.  Ascending was not needed for this data.
     */
    private Map<KeyType, IntWritable> sortDescending(Map<KeyType, IntWritable> map) {
        List<Map.Entry<KeyType, IntWritable>> entries = new LinkedList<>(map.entrySet());

        entries.sort((x, y) -> y.getValue().compareTo(x.getValue()));

        Map<KeyType, IntWritable> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<KeyType, IntWritable> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
