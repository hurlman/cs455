package cs455.hadoop.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Dictionary {

    // TODO: Config file?
    private final String AIRPORTS = "/data/supplementary/airports.csv";
    private final String CARRIERS = "/data/supplementary/carriers.csv";
    private final String PLANES = "/data/supplementary/plane-data.csv";

    private Map<String, String> carriers = new HashMap<>();
    private Map<String, Airport> airports = new HashMap<>();
    private Map<String, Plane> planes = new HashMap<>();

    private static final Dictionary INSTANCE = new Dictionary();

    private Dictionary() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static Dictionary getInstance() {
        return INSTANCE;
    }

    public void initialize(Configuration conf) throws IOException {

        //TODO: Fix this crap
        FileSystem fs = FileSystem.get(conf);
        FSDataInputStream inputStream = fs.open(new Path(AIRPORTS));
        inputStream.

        List<String> airportFile = Files.readAllLines(Paths.get(AIRPORTS));
        for (String data : airportFile) {
            Airport airport = new Airport(data);
            airports.put(airport.getIata(), airport);
        }
        List<String> carrierFile = Files.readAllLines(Paths.get(CARRIERS));
        for (String data : carrierFile) {
            String split[] = data.split("\\s*,\\s*");
            carriers.put(split[0], split[1]);
        }
        List<String> planeFile = Files.readAllLines(Paths.get(PLANES));
        planeFile.removeIf(x -> !x.contains(","));      // Remove incomplete records.
        for(String data : planeFile){
            Plane plane = new Plane(data);
            planes.put(plane.getTailnum(), plane);
        }
    }

    public String getCarrier(String code){
        return carriers.get(code);
    }

    public Airport getAirport(String iata){
        return airports.get(iata);
    }

    public Plane getPlane(String tailnum){
        return planes.get(tailnum);
    }
}
