package fr.unikaz.orm;

public class Pair<F, V> {
    public F field;
    public V value;

    public Pair(F field, V value) {
        this.field = field;
        this.value = value;
    }
}
