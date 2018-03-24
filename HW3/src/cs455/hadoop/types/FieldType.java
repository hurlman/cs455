package cs455.hadoop.types;


public enum FieldType {
    TIME_OF_DAY(1, "Time"),
    DAY_OF_WEEK(2, "Day"),
    MONTH_OF_YEAR(3, "Month"),
    CARRIER_TOT(4, "Carrier-Num-Delays"),
    CARRIER_AVG(5, "Carrier-Avg-Delay"),
    PLANE(6, "Plane"),
    WEATHER_CITY(7, "City-Weather-Delay"),
    AIRPORT(8, "Airport-by-Year");

    private final int _id;
    private final String _name;

    FieldType(int i, String n){
        _id = i;
        _name = n;
    }

    public int getId(){
        return _id;
    }

    public String getName(){
        return _name;
    }

    public static String getName(int i){
        for(FieldType a : FieldType.values()){
            if(a.getId() == i){
                return a._name;
            }
        }
        return "";
    }

    public static FieldType getFieldType(int i){
        for(FieldType a : FieldType.values()){
            if(a.getId() == i){
                return a;
            }
        }
        throw new IllegalArgumentException();
    }

    public static FieldType getFieldType(String s){
        for(FieldType a : FieldType.values()){
            if(a.getName().equals(s)){
                return a;
            }
        }
        throw new IllegalArgumentException();
    }
}
