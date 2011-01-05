package sample;

public class Sample {

    public static void main (final String[] a) {
        Foo f0 = (Foo) (Object) new SharedMemoryTPO(0x01, new Foo());
        Foo f  = f0;
        Foo f2 = (Foo) (Object) new SharedMemoryTPO(0x02, new Foo());
        f.x_(f2.y);
    }
}
