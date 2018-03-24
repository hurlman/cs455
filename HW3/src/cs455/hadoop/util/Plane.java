package cs455.hadoop.util;

public class Plane {
    private String _tailnum;
    private String _type;
    private String _manufacturer;
    private String _issueDate;
    private String _model;
    private String _status;
    private String _aircraftType;
    private String _engineType;
    private String _year;

    public Plane(String line){
        String data[] = line.split("\\s*,\\s*");
        _tailnum = data[0];
        _type = data[1];
        _manufacturer = data[2];
        _issueDate = data[3];
        _model = data[4];
        _status = data[5];
        _aircraftType = data[6];
        _engineType = data[7];
        _year = data[8];
    }

    public String getTailnum() {
        return _tailnum;
    }

    public String getType() {
        return _type;
    }

    public String getManufacturer() {
        return _manufacturer;
    }

    public String getIssueDate() {
        return _issueDate;
    }

    public String getModel() {
        return _model;
    }

    public String getStatus() {
        return _status;
    }

    public String getAircraftType() {
        return _aircraftType;
    }

    public String getEngineType() {
        return _engineType;
    }

    public String getYear() {
        return _year;
    }

    public boolean isOld(String year){
        int myYear = Integer.parseInt(_year);
        int compYear = Integer.parseInt(year);
        return (myYear + 20) < compYear;
    }
}
