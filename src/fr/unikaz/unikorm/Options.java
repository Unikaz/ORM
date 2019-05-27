package fr.unikaz.unikorm;

public class Options {
    private int options;

    public Options(Option... options) {
        this.options = 0;
        for (Option option : options) {
            this.options |= 1 << option.id;
        }
    }

    public boolean has(Option opt) {
        return (options & (1 << opt.id)) != 0;
    }

    public void set(Option opt, boolean value) {
        if (value)
            this.options |= 1 << opt.id;
        else
            this.options &= ~(1 << opt.id);
    }

    public void add(Option opt) {
        this.options |=  1 << opt.id;
    }

    public void remove(Option opt) {
        this.options &= ~(1 << opt.id);
    }
}
