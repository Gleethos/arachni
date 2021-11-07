package comp.imp.plugins.html;

public record Col(
        int sm, int md, int lg
) {

    public enum Type {
        SMALL, MIDDLE, LARGE
    }

    public Col plus(Col other) {
        return new Col(
                other.sm()+this.sm,
                other.md()+this.md(),
                other.lg()+this.lg()
        );
    }

    public Col minus(Col other) {
        return new Col(
                other.sm()-this.sm,
                other.md()-this.md(),
                other.lg()-this.lg()
        );
    }

    public Col div(double other) {
        return new Col(
                (int)(this.sm /other),
                (int)(this.md()/other),
                (int)(this.lg()/other)
        );
    }

    public Col mul(double other) {
        return new Col(
                (int)(this.sm *other),
                (int)(this.md()*other),
                (int)(this.lg()*other)
        );
    }

    public boolean tooLarge(Type type) {
        return switch ( type ) {
            case SMALL -> sm > 12;
            case MIDDLE -> md > 12;
            case LARGE -> lg > 12;
        };
    }

    public Col max(Type type, Col max) {
        return new Col(
                type == Type.SMALL ? Math.max(max.sm,this.sm) : sm,
                type == Type.MIDDLE ? Math.max(max.md,this.md) : md,
                type == Type.LARGE ? Math.max(max.lg,this.lg) : lg
            );
    }

    public Col delta(int number) {
        return new Col(
                Math.abs(number-this.sm),
                Math.abs(number-this.md),
                Math.abs(number-this.lg)
        );
    }

}
