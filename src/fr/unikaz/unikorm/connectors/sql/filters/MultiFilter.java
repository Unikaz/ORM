package fr.unikaz.unikorm.connectors.sql.filters;

import fr.unikaz.unikorm.api.IFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiFilter implements IFilter {
    public List<IFilter> children = new ArrayList<>();
    public FilterType type;

    public enum FilterType {
        AND, OR;
    }

    public MultiFilter(FilterType type, IFilter... filters) {
        this.type = type;
        children.addAll(Arrays.stream(filters).collect(Collectors.toList()));
    }

    public void add(IFilter filter) {
        children.add(filter);
    }

    @Override
    public String toString() {
        return children.stream().map(f -> '(' + f.toString() + ')').collect(Collectors.joining(type.name()));
    }
}
