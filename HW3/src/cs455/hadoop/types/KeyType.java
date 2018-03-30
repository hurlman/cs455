package cs455.hadoop.types;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class KeyType implements WritableComparable {

    private IntWritable _category;
    private Text _value;

    public KeyType(KeyType key) {
        _category = new IntWritable(key._category.get());
        _value = new Text(key._value.toString());
    }

    public KeyType() {
        _value = new Text();
        _category = new IntWritable();
    }

    public KeyType(FieldType category, String value) {
        _category = new IntWritable(category.getId());
        _value = new Text(value);
    }

    public int getCategory() {
        return _category.get();
    }

    @Override
    public String toString() {
        return FieldType.getName(_category.get()) + ": " + _value;
    }

    @Override
    public int compareTo(Object o) {
        KeyType kt = (KeyType) o;

        int cmp = _category.compareTo(kt._category);

        if (cmp != 0) {
            return cmp;
        }
        return _value.compareTo(kt._value);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        _category.write(dataOutput);
        _value.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        _category.readFields(dataInput);
        _value.readFields(dataInput);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyType keyType = (KeyType) o;
        return _category == keyType._category &&
                Objects.equals(_value, keyType._value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_category, _value);
    }
}
