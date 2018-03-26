package cs455.hadoop.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {

    // TODO: Config file?
    private final String AIRPORTS = "/data/supplementary/airports.csv";
    private final String CARRIERS = "/data/supplementary/carriers.csv";
    private final String PLANES = "/data/supplementary/plane-data.csv";

    public Map<String, String> carriers = new HashMap<>();
    public Map<String, Airport> airports = new HashMap<>();
    public Map<String, Plane> planes = new HashMap<>();

    public void initialize(Configuration conf) throws IOException {

        //TODO: Fix this crap
        FileSystem fs = FileSystem.get(conf);
        FSDataInputStream ais = fs.open(new Path(AIRPORTS));
        List<String> airportFile = IOUtils.readLines(ais);
        for (String data : airportFile) {
            Airport airport = new Airport(data);
            airports.put(airport.getIata(), airport);
        }
        ais.close();

        FSDataInputStream cis = fs.open(new Path(CARRIERS));
        List<String> carrierFile = IOUtils.readLines(cis);
        for (String data : carrierFile) {
            String split[] = data.split("\\s*,\\s*");
            carriers.put(split[0], split[1]);
        }
        cis.close();

        FSDataInputStream pis = fs.open(new Path(PLANES));
        List<String> planeFile = IOUtils.readLines(pis);
        planeFile.removeIf(x -> !x.contains(","));      // Remove incomplete records.
        for(String data : planeFile){
            Plane plane = new Plane(data);
            planes.put(plane.getTailnum(), plane);
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
