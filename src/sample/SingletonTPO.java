package sample;

public class SingletonTPO {
    private static Object instance;
    public static void SetInstance (final Object _instance) {
        instance = _instance;
    }
    //
    //
    //

    public Object GetCommonInstance () {
        return instance;
    }
}
