package cs455.hadoop.util;

public class Airport {
    private String _iata;
    private String _airport;
    private String _city;
    private String _state;
    private String _country;
    private String _lat;
    private String _long;

    public Airport(String line){
        String[] data = line.split("\\s*,\\s*");
        _iata = data[0];
        _airport = data[1];
        _city = data[2];
        _state = data[3];
        _country = data[4];
        _lat = data[5];
        _long = data[6];
    }

    public String getIata() {
        return _iata;
    }

    public String getAirport() {
        return _airport;
    }

    public String getState() {
        return _state;
    }

    public String getCity() {
        return _city;
    }

    public String getCountry() {
        return _country;
    }

    public String getLat() {
        return _lat;
    }

    public String getLong() {
        return _long;
    }
}
