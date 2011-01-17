package sample;

import sample.util.P;

class Foo {

    private int _x = 12;
    public int y = 15;

    public int x () {
        P.println(this + ": Accessing Foo.x(" + _x + ")");
        return _x;
    }

    public void x_ (final int x) {
        P.println(this + ": setting x(" + _x + ") = " + x);
        _x = x;
    }
}
