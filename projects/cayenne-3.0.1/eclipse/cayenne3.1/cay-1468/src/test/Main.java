package test;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;

import test.cayenne.T1;
import test.cayenne.T2;

public class Main {

	public static void main(String[] args) {

		ServerRuntime cayenneRuntime = new ServerRuntime("cayenne-d1.xml");
		ObjectContext context = cayenneRuntime.getContext();

		T1 t1 = context.newObject(T1.class);
		t1.setName("Root");
		context.commitChanges();

		warmup(t1);
		linearAll(t1);
	}

	private static void warmup(T1 t1) {
		for (int i = 0; i < 6; i++) {
			long l = _500T2s(t1);
		}
	}

	private static void linear1(T1 t1) {

		long l = _500T2s(t1);
	}

	private static void linear2(T1 t1) {
		long l = _500T2s(t1);
	}

	private static void linearAll(T1 t1) {
		for (int i = 0; i < 20; i++) {
			long l = _500T2s(t1);
			System.out.println((i * 500) + ": " + l);
		}
	}

	private static long _500T2s(T1 t1) {

		long x0 = System.currentTimeMillis();
		for (int i = 1; i < 500; i++) {

			T2 t = new T2();
			t.setName("T2_");
			t.setT1(t1);

			t1.getObjectContext().commitChanges();
		}

		long x1 = System.currentTimeMillis();
		return x1 - x0;
	}
}
