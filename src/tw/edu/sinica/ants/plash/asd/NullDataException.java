package tw.edu.sinica.ants.plash.asd;

import java.io.*;

/**
 * Class NullDataException. This is a custom exception used to indicate empty data delivery from server. <br>
 * This exception is thrown when the program tries to query data from server but receives null value. <br>
 * This exception can be used when the program expects non-empty data from server but receives null data. <br>
 * Notice that if the real cause is network or hardware error, exceptions related to these errors should be used instead of this exception. <br> 
 * For most times a null return value is inevitable. The programmer should be aware of this. <br>
 * @author Yi-Chun Teng
 *
 */
public class NullDataException extends RuntimeException implements Serializable {
	/**
	 * This ID is generated
	 */
	private static final long serialVersionUID = 1L;
	String errMsg;
	
	public NullDataException() {
		this.errMsg = new String("no detail given");
	}//end constructor
	
	public NullDataException(String detailMessage) {
		this.errMsg = detailMessage;
	}//end constructor
	
	public void printStackTrace(PrintWriter err) {
		err.print(errMsg);
	}//end method

}//end method
