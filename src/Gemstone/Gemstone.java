/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;


/**
 *
 * @author jusjoken
 */
public class Gemstone {
    private static final Gemstone INSTANCE = new Gemstone();

    static {
            // initialize phoenix
            try {
                    INSTANCE.init();
            } catch (Exception e) {
                    Log.error("Gemstone", "INSTANCE init Failed to load gemstone. " + e);
            }
    }

    public static Gemstone getInstance() {
            return INSTANCE;
    }

    protected void init() {
        Log.info("Gemstone","Initializing Gemstone - Version: " +  api.GetVersion() + " " + util.LogInfo());

    }

    protected Gemstone() {
        //Init Gemstone
    }
    
}
