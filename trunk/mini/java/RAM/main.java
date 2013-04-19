// RAM interpreter
// Purpose: To simulate the execution of a RAM (random access machine)
// Author: Jeremy Johnson
// Date: 9/25/00

import java.io.*;
import java.util.*;

public class main {
	public static void main(String args[])
	{
		int pSize = -1, mSize = -1;
		String pName = null, mName = null;
		RAM M = null;
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

		try {
			System.out.print("Enter program size:  ");
			pSize = Integer.parseInt(cin.readLine());
			System.out.print("Enter memory size:  ");
			mSize = Integer.parseInt(cin.readLine());

			M = new RAM(pSize,mSize);  // Create RAM with space for a program of size pSize
										   // and memory of size mSize

			System.out.print("Enter name of file containing RAM program:  ");
			pName = cin.readLine();
			System.out.print("Enter name of file containing RAM initial memory configuration:  ");
			mName = cin.readLine();
		}
		catch(IOException e) {
			System.err.println("IO Error");
			System.exit(1);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		M.init(pName,mName);  // Initialize RAM with program in pName and
									   // initial memory configuration in mName

		System.out.println("Initial Memory Configuration");
		M.dump();

		M.execute();  // Execute RAM with given program and memory configuration

		System.out.println("\nFinal Memory Configuration");
		M.dump();
	}
}
