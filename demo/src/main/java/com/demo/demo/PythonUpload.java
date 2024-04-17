package com.demo.demo;

import java.util.HashMap;

import org.python.util.PythonInterpreter;

public class PythonUpload {
	 public static void main(String[] args) {
		 HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		 hm.put(10, 5);
		// System.out.println(hm);
	        PythonInterpreter pythonInterpreter = new PythonInterpreter();
	        pythonInterpreter.set("hm1111", hm);

	        pythonInterpreter.exec(""
	        		+ "print('ommmm')\n"
	        		+ "a = 10\n"
	        		+ "print(a)\n"
	        		+"print(hm1111)"
	        );
	    }

}
