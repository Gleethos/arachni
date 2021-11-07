package comp.imp.plugins.html;

public class AbstractHTMLBuilder<C> {

    private final StringBuilder _builder = new StringBuilder();


    public C $(Object o) {
        _builder.append( ( o==null ) ? "" : o.toString() );
        return (C) this;
    }

    @Override
    public String toString(){
        return _builder.toString();
    }

}
