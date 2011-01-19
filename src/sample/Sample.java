package sample;

import sample.util.P;

public class Sample {

    private static final Foo FA = (Foo) (Object) new SharedMemoryTPO(0x03, new Foo());

    public static void main (final String[] a) {
        Foo f0 = new Foo();//(Foo) (Object) new SharedMemoryTPO(0x01, new Foo());
        Foo f2 = (Foo) (Object) new SharedMemoryTPO(0x982379, new Foo());

        Foo f = f2;
        f.y = f.x_(f.y, f.x());
//        P.println(f.y);
//        f = f0;
//        f.y = f.x_(f.y, f.x());
//        P.println(f.y);


//        FA.y = f.x();
//        f.y = FA.y;

        // cast to remain
//        Object o = new Sample();
//        Sample s = (Sample) o;
    }
}
