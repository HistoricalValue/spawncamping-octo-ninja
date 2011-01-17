package sample;

import sample.util.P;

public class Sample {

//    private static final Foo FA = (Foo) (Object) new SharedMemoryTPO(0x03, new Foo());

    public static void main (final String[] a) {
//        Foo f0 = new Foo();//(Foo) (Object) new SharedMemoryTPO(0x01, new Foo());
//        Foo f  = f0;
        Foo f2 = (Foo) (Object) new SharedMemoryTPO(0x02, new Foo());
//        f.x_(f2.y);
//        f.x();
        f2.x();

//        FA.y = f.x();
//        f.y = FA.y;

        // cast to remain
        Object o = new Sample();
        Sample s = (Sample) o;
    }
}
