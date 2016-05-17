package test.cayenne;

import test.cayenne.auto._M1;

public class M1 extends _M1 {

    private static M1 instance;

    private M1() {}

    public static M1 getInstance() {
        if(instance == null) {
            instance = new M1();
        }

        return instance;
    }
}
