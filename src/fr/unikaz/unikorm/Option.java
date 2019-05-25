package fr.unikaz.unikorm;

import java.util.Arrays;
import java.util.Comparator;

public enum Option{
    IGNORE_AUTO_INCREMENT(1),
    IGNORE_PRIMARY_KEYS(2),
    ONLY_PRIMARY_KEYS(3),
    PREFIXED(4);

    public static final int maxId = Arrays.stream(Option.values())
            .max(Comparator.comparingInt(o -> o.id))
            .get().id;

    public final int id;

    Option(int id) {
        this.id = id;
    }

}
