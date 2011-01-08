package sample;


public class Sample
{

    public static void main(String[]  r0)
    {

        Foo r3, r5;
        r3 = (Foo) new SharedMemoryTPO(1, new Foo());
        r5 = (Foo) new SharedMemoryTPO(2, new Foo());
        r3.x_(r5.y);
    }
}
