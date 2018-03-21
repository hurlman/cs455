package cs455.hadoop.airline;

public enum FieldType {
    TIME_OF_DAY(1, "Time"),
    DAY_OF_WEEK(2, "Day"),
    MONTH_OF_YEAR(3, "Month"),
    AIRPORT(4, "Airport"),
    CARRIER(5, "Carrier"),
    PLANE(6, "Plane"),
    WEATHER_CITY(7, "City");

    private final int _id;
    private final String _name;

    FieldType(int i, String n){
        _id = i;
        _name = n;
    }

    public int getId(){
        return _id;
    }

    public static String getFriendlyName(int i){
        for(FieldType a : FieldType.values()){
            if(a.getId() == i){
                return a._name;
            }
        }
        return "";
    }
}