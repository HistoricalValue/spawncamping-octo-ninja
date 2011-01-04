package sample;

class Foo {

    private int _x = 12;
    public int y = 15;

    public int x () {
        return _x;
    }

    public void x_ (final int x) {
        _x = x;
    }
}
