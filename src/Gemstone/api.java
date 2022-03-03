/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.lang.reflect.InvocationTargetException;
import sagex.UIContext;
import sagex.phoenix.Phoenix;

/**
 *
 * @author SBANTA
 * @author JUSJOKEN
 * - 9/27/2011 - added LOG4J setup and Load method
 * - 10/1/2011 - implemented Load and InitLogger methods
 * - 10/28/2011 - Conversion to Diamond API for 4.x
 * - 04/02/2012 - Conversion to Gemstone API for 1.x
 * - 08/28/2021 - removed the use of Log4J - using system log directly
 */
public class api {

    public static String Version = "1.0606" + "";
    private static boolean STVAppStarted = false;
    private static boolean LoadCompleted = false;
    
    public static boolean IsSTVAppStarted(){
        return STVAppStarted;
    }
    public static void SetSTVAppStarted(boolean value){
        STVAppStarted = value;
    }

    public static void main(String[] args){

        Load();
    }


    //load any Gemstone settings that need to load at application start
    //should be called from GemstonePlugin on the start event
    public static void Load(){
        Log.info("api", "Loading Gemstone - IsClient:" + util.IsClient() + " IsRemote:" + util.IsRemote() + " IsClientSage:" + util.IsClientSage() + " IsClientProp:" + util.IsClientProp());

        if (LoadCompleted){
            Log.info("Load: api load called again - already loaded: " + util.LogInfo());
        }else{

            //Initialize the GWeather config
            sageweather.utils.getServerOWM();

            //Initialize Phoenix services as sometimes on a PC Client they do not load fully without this
            if(util.IsClientSage()){
                Log.info("api", "Load - begin phoenix service");
                Phoenix phoenixservice = sagex.phoenix.Phoenix.getInstance();
                phoenixservice.reinit();
                Log.info("api", "Load - phoenix service re-loaded");
            }

            //get the gemstone instance which will also initiate logging
            Gemstone.getInstance();
            Log.info("Load: api load started: " + util.LogInfo());

            //initialize the ADM settings
            //these are now called directly by the client start
            //ADMutil.LoadADM();

            //Init the common Weather interface
            //REMOVED 7/28/2021 as gemstone now uses GoogleWeather.jar and OWM for weather calls
            //Weather.Init();

            //generate symbols to be used for new names
            util.InitNameGen();

            //ensure the gemstone file location exists
            util.InitLocations();

            //prepare the image cache
            ImageCache.Init();

            LoadCompleted = true;
            Log.info("Load: api load completed: " + util.LogInfo());
        }
        
   }

    public static void ClientStart(){
        //client specific settings
        util.InitLocations(); //call just to ensure this is completed
        ADMutil.ClientStart();
        util.LogConnectedClients();
        util.LogPlugins();
        util.gc(2);
        
    }

    public static void ClientExit(String UIContext){
        //remove client specific settings for Menus
        ADMutil.ClientExit(UIContext);
        util.LogConnectedClients();
        util.gc(2);
        
    }
    
    public static void AddStaticContext(String Context, Object Value) {
        sagex.api.Global.AddStaticContext(new UIContext(sagex.api.Global.GetUIContextName()), Context, Value);

    }

    public static void ExecuteWidgeChain(String UID) {
        String UIContext = sagex.api.Global.GetUIContextName();
        Log.debug("Getting Ready to execute widget chain for " + UIContext);
        Log.debug("Actual context " + sagex.api.Global.GetUIContextName());
        Object[] passvalue = new Object[1];
        passvalue[0] = sagex.api.WidgetAPI.FindWidgetBySymbol(new UIContext(UIContext), UID);
        try {
            sage.SageTV.apiUI(UIContext, "ExecuteWidgetChainInCurrentMenuContext", passvalue);
        } catch (InvocationTargetException ex) {
            Log.error("error executing widget" + api.class.getName() + ex);
        }
//        sagex.api.WidgetAPI.ExecuteWidgetChain(new UIContext(UIContext),sagex.api.WidgetAPI.FindWidgetBySymbol(new UIContext(UIContext),UID));
        }

    public static String GetVersion() {
        return Version;
    }
}
