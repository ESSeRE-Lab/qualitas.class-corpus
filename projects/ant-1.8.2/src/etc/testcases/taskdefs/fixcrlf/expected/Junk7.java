//Qualitas.class: We have to rename the class to the same name of the file
public class Junk7 {
    public static boolean mybool; //Qualitas.class: We have to insert the static modifier
    public static void main (String[] args) {
	System.out.println("I have	a tab");
	//	Here is a comment with an embedded tab
	if (mybool) {	/* Here is a multi-line
			   (with embedded'	'tab)
	    Comment */char mychar = '	';	//<-tab->	<-
	} // end of if (mybool)
	
    } // end of main ()
}
