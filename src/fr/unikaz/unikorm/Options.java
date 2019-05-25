package fr.unikaz.unikorm;

public class Options {
    private boolean[] options;

    public Options(Option... options) {
        this.options = new boolean[Option.maxId];
        for (Option option : options) {
            this.options[option.id] = true;
        }
    }

    public boolean has(Option opt) {
        return opt != null && options[opt.id];
    }

    public void set(Option opt, boolean value) {
        if (opt != null)
            options[opt.id] = value;
    }

    public void add(Option opt) {
        if (opt != null)
            options[opt.id] = true;
    }

    public void remove(Option opt) {
        if (opt != null)
            options[opt.id] = false;
    }
}
