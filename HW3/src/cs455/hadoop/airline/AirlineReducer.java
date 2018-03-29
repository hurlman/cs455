package cs455.hadoop.airline;

import cs455.hadoop.types.FieldType;
import cs455.hadoop.types.KeyType;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class AirlineReducer extends Reducer<KeyType, IntWritable, KeyType, IntWritable> {

    private final static int REPORT_COUNT = 10;

    private Map<KeyType, IntWritable> timeMap = new HashMap<>();
    private Map<KeyType, IntWritable> dayMap = new HashMap<>();
    private Map<KeyType, IntWritable> monthMap = new HashMap<>();
    private Map<KeyType, IntWritable> planeMap = new HashMap<>();
    private Map<KeyType, IntWritable> carrAvgMap = new HashMap<>();
    private Map<KeyType, IntWritable> carrTotMap = new HashMap<>();
    private Map<KeyType, IntWritable> cityMap = new HashMap<>();
    private Map<KeyType, IntWritable> arptMap = new HashMap<>();

    @Override
    protected void reduce(KeyType key, Iterable<IntWritable> values, Context context) {
        switch (FieldType.getFieldType(key.getCategory())) {
            case TIME_OF_DAY:
                timeMap.put(key, getAverage(values));
                break;
            case DAY_OF_WEEK:
                dayMap.put(key, getAverage(values));
                break;
            case MONTH_OF_YEAR:
                monthMap.put(key, getAverage(values));
                break;
            case PLANE:
                planeMap.put(key, getAverage(values));
                break;
            case CARRIER_AVG:
                carrAvgMap.put(key, getAverage(values));
                break;
            case CARRIER_TOT:
                carrTotMap.put(key, getSum(values));
                break;
            case WEATHER_CITY:
                cityMap.put(key, getSum(values));
                break;
            case AIRPORT:
                arptMap.put(key, getSum(values));
                break;
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        writeSortedOut(timeMap, context);
        writeSortedOut(dayMap, context);
        writeSortedOut(monthMap, context);
        writeSortedOut(planeMap, context);
        writeSortedOut(carrAvgMap, REPORT_COUNT, context);
        writeSortedOut(carrTotMap, REPORT_COUNT, context);
        writeSortedOut(cityMap, REPORT_COUNT , context);
        writeSortedOut(arptMap, REPORT_COUNT, context);
    }

    private void writeSortedOut(Map<KeyType, IntWritable> unsortedMap, Context context)
            throws IOException, InterruptedException {
        Map<KeyType, IntWritable> sortedMap = sortDescending(unsortedMap);
        for (KeyType key : sortedMap.keySet()) {
            context.write(key, sortedMap.get(key));
        }
    }

    private void writeSortedOut(Map<KeyType, IntWritable> unsortedMap, int count, Context context)
            throws IOException, InterruptedException {
        Map<KeyType, IntWritable> sortedMap = sortDescending(unsortedMap);
        int x = 0;
        for (KeyType key : sortedMap.keySet()) {
            if (x++ == count) break;
            context.write(key, sortedMap.get(key));
        }
    }

    private IntWritable getSum(Iterable<IntWritable> values) {
        int total = 0;
        for (IntWritable val : values) {
            total += val.get();
        }
        return new IntWritable(total);
    }

    private IntWritable getAverage(Iterable<IntWritable> values) {
        int totalDelay = 0;
        int numFlights = 0;
        for (IntWritable val : values) {
            totalDelay += val.get();
            numFlights++;
        }
        int averageDelay = totalDelay / numFlights;
        return new IntWritable(averageDelay);
    }

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
