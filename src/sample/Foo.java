package sample;

import sample.util.P;

class Foo {

    private int _x = 12;
    public int y = 15;

    public int x () {
        P.println(this + ": Accessing Foo.x(" + _x + ")");
        return _x;
    }

    public int x_ (final int x) {
        P.println(this + ": setting x(" + _x + ") = " + x);
        _x = x;
        return x;
    }
    public int x_ (final int x, final int y) {
        return x_(x + y);
    }
    public int x_ (final int x, final int y, final int z) {
        return x_(x, y + z);
    }
}
