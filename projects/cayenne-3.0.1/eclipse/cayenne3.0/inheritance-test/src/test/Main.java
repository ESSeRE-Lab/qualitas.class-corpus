package test;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;

public class Main {

	public static void main(String[] args) {
	
		ObjectContext c = DataContext.createDataContext();
		
		Instr i1 = c.newObject(Instr.class);
		i1.setInstrAttribute("ia1");
		
		Trade t1 = c.newObject(Trade.class);
		t1.setTradeAttribute("ya1");
		
		c.commitChanges();
	}
}
