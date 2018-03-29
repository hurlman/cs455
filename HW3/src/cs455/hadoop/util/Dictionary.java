package cs455.hadoop.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dictionary {

    // TODO: Config file?
    private final static String AIRPORTS = "/data/supplementary/airports.csv";
    private final static String CARRIERS = "/data/supplementary/carriers.csv";
    private final static String PLANES = "/data/supplementary/plane-data.csv";

    private Map<String, String> carriers = new ConcurrentHashMap<>();
    private Map<String, Airport> airports = new ConcurrentHashMap<>();
    private Map<String, Plane> planes = new ConcurrentHashMap<>();

    public void initialize(Configuration conf) throws IOException {

        FileSystem fs = FileSystem.get(conf);
        FSDataInputStream ais = fs.open(new Path(AIRPORTS));
        List<String> airportFile = IOUtils.readLines(ais);
        for (String data : airportFile) {
            Airport airport = new Airport(data);
            airports.put(airport.getIata().toLowerCase().replace("\"",""), airport);
        }
        ais.close();

        FSDataInputStream cis = fs.open(new Path(CARRIERS));
        List<String> carrierFile = IOUtils.readLines(cis);
        for (String data : carrierFile) {
            String split[] = data.split("\\s*,\\s*");
            carriers.put(split[0].toLowerCase().replace("\"",""), split[1]);
        }
        cis.close();

        FSDataInputStream pis = fs.open(new Path(PLANES));
        List<String> planeFile = IOUtils.readLines(pis);
        planeFile.removeIf(x -> !x.contains(","));      // Remove incomplete records.
        for(String data : planeFile){
            Plane plane = new Plane(data);
            planes.put(plane.getTailnum().toLowerCase().replace("\"",""), plane);
        }
        pis.close();
        fs.close();
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
