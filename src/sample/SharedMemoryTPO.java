package sample;

import java.util.HashMap;
import java.util.Map;

public class SharedMemoryTPO {
    private static final Map<Integer, Object> instances = new HashMap<>(1024);

    private final Integer key;
    public SharedMemoryTPO (int _key, final Object instIfNoInst) {
        key = _key;
        if (!instances.containsKey(_key))
            instances.put(_key, instIfNoInst);
    }
    public void SetInstance (final Object inst) {
        instances.put(key, inst);
    }
    //
    //
    //

    public Object GetInstance () {
        return instances.get(key);
    }
}
