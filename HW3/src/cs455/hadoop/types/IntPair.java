package cs455.hadoop.types;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class IntPair implements WritableComparable {

    private IntWritable first;
    private IntWritable second;

    public IntPair() {
        first = new IntWritable();
        second = new IntWritable();
    }

    public IntPair(int first, int second) {
        this.first = new IntWritable(first);
        this.second = new IntWritable(second);
    }

    public int getFirst() {
        return first.get();
    }

    public int getSecond() {
        return second.get();
    }

    @Override
    public String toString() {
        return first + ", " + second;
    }

    @Override
    public int compareTo(Object o) {
        IntPair ip = (IntPair) o;

        int cmp = first.compareTo(ip.first);

        if (cmp != 0) {
            return cmp;
        }

        return second.compareTo(ip.second);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        first.write(dataOutput);
        second.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        first.readFields(dataInput);
        second.readFields(dataInput);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntPair intPair = (IntPair) o;
        return Objects.equals(first, intPair.first) &&
                Objects.equals(second, intPair.second);
    }

    @Override
    public int hashCode() {

        return Objects.hash(first, second);
    }
}
