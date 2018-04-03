package cs455.hadoop.types;

/**
 * Enum defining the different categories of record data to answer all questions.  One of the two
 * components of the KeyType.  Provides methods to friendly names for each type, as well as
 * methods to go back and forth between type and corresponding int values for serialization and
 * deserialization.
 */
public enum FieldType {
    TIME_OF_DAY(1, "Time-Avg-Delay"),
    DAY_OF_WEEK(2, "Day-Avg-Delay"),
    MONTH_OF_YEAR(3, "Month-Avg-Delay"),
    CARRIER_TOT(4, "Carrier-Num-Delays"),
    CARRIER_MIN(5, "Carrier-Tot-Minutes-Delayed"),
    CARRIER_AVG(6, "Carrier-Avg-Delay"),
    PLANE_AGE(7, "Plane-Age-Avg-Delay"),
    PLANE_MODEL(8, "Model-Most-Travelled"),
    PLANE_MODEL_FLIGHTS(9, "Model-Most-Flights"),
    PLANE_NUMBER(10, "Plane-Most-Travelled"),
    WEATHER_CITY(11, "City-Num-Weather-Delay"),
    AIRPORT(12, "Airport-by-Year");

    private final int _id;
    private final String _name;

    FieldType(int i, String n) {
        _id = i;
        _name = n;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public static String getName(int i) {
        for (FieldType a : FieldType.values()) {
            if (a.getId() == i) {
                return a._name;
            }
        }
        return "";
    }

    public static FieldType getFieldType(int i) {
        for (FieldType a : FieldType.values()) {
            if (a.getId() == i) {
                return a;
            }
        }
        throw new IllegalArgumentException();
    }

    public static FieldType getFieldType(String s) {
        for (FieldType a : FieldType.values()) {
            if (a.getName().equals(s)) {
                return a;
            }
        }
        throw new IllegalArgumentException();
    }
}
