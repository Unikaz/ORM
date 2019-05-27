package fr.unikaz.unikorm.api;

public enum Option {
    IGNORE_AUTO_INCREMENT(1),
    IGNORE_PRIMARY_KEYS(2),
    ONLY_PRIMARY_KEYS(3),
    PREFIXED(4);

    public final int id;

    Option(int id) {
        this.id = id;
    }

    
}


