package comp.imp.plugins.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record Entry(
        String title, String content, Col cols
) {

    private static final Pattern[] _NAME_PATTERNS = new Pattern[] {
                                                Pattern.compile("(_)?content"),
                                                Pattern.compile("(_)?description"),
                                                Pattern.compile("(_)?value"),
                                                Pattern.compile("(_)?deleted"),
                                                Pattern.compile("(_)?created"),
                                                Pattern.compile("(_)?id"),
                                        };

    public static List<Entry> listFrom( Map<String,String> map ) {

        List<Entry> entries = map.entrySet()
                                .stream()
                                .map( e -> {
                                    String key = e.getKey();
                                    String value = e.getValue();
                                    return new Entry( key, value, _basicColsFor(key, value) );
                                })
                                .sorted( (a, b) -> b._rank() - a._rank() )
                                .collect(Collectors.toList());

        entries = _fitColumn(Col.Type.SMALL, entries);
        entries = _fitColumn(Col.Type.MIDDLE, entries);
        entries = _fitColumn(Col.Type.LARGE, entries);
        return entries;
    }

    private static List<Entry> _fitColumn(Col.Type type, List<Entry> entries) {

        var all = new ArrayList<Entry>();
        var row = new ArrayList<Entry>();
        Consumer<Col> reader = ( sum ) -> {
            row.sort( (a, b) -> b._rank() - a._rank() );
            var lastIndex = (row.size() - 1);
            var last = row.get( lastIndex );
            row.set(
                    lastIndex,
                    new Entry(
                            last.title(),
                            last.content(),
                            last.cols().max(type, sum.delta(12))
                    )
            );
            all.addAll(row);
            row.clear();
        };
        var sum = new Col(0,0,0);
        for ( var attributeName : entries ) {
            var cols = attributeName.cols();
            var tempSum = sum.plus(cols);
            if ( tempSum.tooLarge(type) ) {
                reader.accept(sum);
                sum = new Col(0,0,0);
            }
            else sum = tempSum;
            row.add(attributeName);
        }
        reader.accept(sum);
        return all;
    }

    private static Col _basicColsFor(String title, String content ) {
        Col sizes;
        if ( title.equals("id") || title.endsWith("_id") ) {
            sizes = new Col(4, 3, 2);
        } else if ( title.contains("value") || title.contains("content") ) {
            sizes = new Col(12, 12, 12);
        } else if ( title.contains("deleted") || title.contains("created") ) {
            sizes = new Col(12, 4, 4);
        } else if ( title.contains("description") ) {
            sizes = new Col(12, 12, 6);
        } else if ( title.equals("name") ) {
            if ( content.length() < 10 )
                sizes = new Col(6, 3, 2);
            else
                sizes = new Col(12, 6, 4);
        } else {
            sizes = new Col(12, 6, 4);
        }
        return sizes;
    }

    private int _rank() {
        int rank = 0;
        for ( var p : _NAME_PATTERNS) {
            if ( p.matcher(title).matches() ) return rank;
            rank++;
        }
        return rank;
    }


}
