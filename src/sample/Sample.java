package sample;

public class Sample {

    private static final Foo FA = (Foo) (Object) new SharedMemoryTPO(0x03, new Foo());

    public static void main (final String[] a) {
        Foo f0 = (Foo) (Object) new SharedMemoryTPO(0x01, new Foo());
        Foo f  = f0;
        Foo f2 = (Foo) (Object) new SharedMemoryTPO(0x02, new Foo());
        f.x_(f2.y);

        if ((Object) f instanceof SharedMemoryTPO)
            ((Foo)((SharedMemoryTPO)(Object)f).GetInstance()).x();

        FA.x_(f.y);

        boolean lol = true;
        if (lol) lol = false; else lol = true;
        if (true) lol = false; else lol = true;
    }
}
