package test;

import test.auto._UntitledDomainMap;

public class UntitledDomainMap extends _UntitledDomainMap {

    private static UntitledDomainMap instance;

    private UntitledDomainMap() {}

    public static UntitledDomainMap getInstance() {
        if(instance == null) {
            instance = new UntitledDomainMap();
        }

        return instance;
    }
}
