package se.commit.utils;

public class Tuple<T1, T2, T3> {

    public final T1 first;

    public final T2 second;

    public final T3 third;

    public Tuple(T1 a, T2 b, T3 c) {
        this.first = a;
        this.second = b;
        this.third = c;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public T3 getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "(" + getFirst().toString() + "," + getSecond().toString() + "," + getThird().toString() + ")";
    }
}

