/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Gemstone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 *
 * @author SBANTA
 * - 04/04/2012 - updated for Gemstone
 */
public class ClassFromString {

   
    public static Object GetSortDividerClass(String method,Object MediaObject) {
	 Object returnValue = null;
        try {
	               /*
	             * Step 3: Load the class
	             */
	            Class myClass = MetadataCalls.class;
                    if(method.contains("_")){
                    method =method.substring(method.lastIndexOf("_"));}

	            /*
	             *Step 4: create a new instance of that class
	             */
	            Object whatInstance = myClass.newInstance();

	            Object methodParameter = MediaObject;
	            /*
	             * Step 5: get the method, with proper parameter signature.
	             * The second parameter is the parameter type.
	             * There can be multiple parameters for the method we are trying to call,
	             * hence the use of array.
	             */

	            Method myMethod = myClass.getMethod(method,
	                    new Class[] { Object.class });


	            /*
	             *Step 6:
	             *Calling the real method. Passing methodParameter as
	             *parameter. You can pass multiple parameters based on
	             *the signature of the method you are calling. Hence
	             *there is an array.
	             */
	           returnValue =  myMethod.invoke(whatInstance,
	                    new Object[] { methodParameter });

	            System.out.println("The value returned make class method:"
	                    + returnValue);

	        } catch (SecurityException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();

	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (NoSuchMethodException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        }
         return returnValue;

	    }
    public static Object GetDateClass(String method,Object Airing) {
	 Object returnValue = null;
        try {
	               /*
	             * Step 3: Load the class
	             */
	            Class myClass = MetadataCalls.class;

	            /*
	             *Step 4: create a new instance of that class
	             */
	            Object whatInstance = myClass.newInstance();

	           Object methodParameter = Airing;
	            /*
	             * Step 5: get the method, with proper parameter signature.
	             * The second parameter is the parameter type.
	             * There can be multiple parameters for the method we are trying to call,
	             * hence the use of array.
	             */

	            Method myMethod = myClass.getMethod(method,
	                    new Class[] { Object.class });


	            /*
	             *Step 6:
	             *Calling the real method. Passing methodParameter as
	             *parameter. You can pass multiple parameters based on
	             *the signature of the method you are calling. Hence
	             *there is an array.
	             */
	           returnValue =  myMethod.invoke(whatInstance,
	                    new Object[] { methodParameter });

	            Log.debug("The value returned make class method:"
	                    + returnValue);

	        } catch (SecurityException e) {
	            Log.fatal("ClassFromString",ClassFromString.class.getName()+e);
	        } catch (IllegalArgumentException e) {
	             Log.fatal("ClassFromString",ClassFromString.class.getName()+e);

	        } catch (InstantiationException e) {
	            Log.fatal("ClassFromString",ClassFromString.class.getName()+e);
	        } catch (IllegalAccessException e) {
	             Log.fatal("ClassFromString",ClassFromString.class.getName()+e);
	        } catch (NoSuchMethodException e) {
	            Log.fatal("ClassFromString",ClassFromString.class.getName()+e);
	        } catch (InvocationTargetException e) {
	            Log.fatal("ClassFromString",ClassFromString.class.getName()+e);
	        }
         return returnValue;
    }

    public static String GetSortMethod(String SortToGet) {
	 String returnValue = "";
     
              

	            /*
	             * Step 3: Load the class
	             */
	            Class myClass = SortMethods.class;
        try {
            /*
             *Step 4: create a new instance of that class
             */
            Object whatInstance = myClass.newInstance();

                   
                    String SParameter = SortToGet;

            /*
             * Step 5: get the method, with proper parameter signature.
             * The second parameter is the parameter type.
             * There can be multiple parameters for the method we are trying to call,
             * hence the use of array.
             */
            Field test = myClass.getField(SortToGet);
            String Value = (String) test.get(whatInstance);
             return Value;
        }

         catch (InstantiationException ex) {
            Log.fatal("ClassFromString",ClassFromString.class.getName()+ex);
        } catch (IllegalAccessException ex) {
            Log.fatal("ClassFromString",ClassFromString.class.getName()+ex);
        }
        catch (NoSuchFieldException ex) {
           Log.fatal("ClassFromString",ClassFromString.class.getName()+ex);
        } catch (SecurityException ex) {
            Log.fatal("ClassFromString",ClassFromString.class.getName()+ex);
        }
            
	           
	     
     
               
         return "";

	    }

}
