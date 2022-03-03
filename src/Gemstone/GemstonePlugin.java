/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import sage.SageTVPluginRegistry;
import sagex.plugin.AbstractPlugin;
import sagex.plugin.SageEvent;
import sagex.plugin.SageEvents;

/**
 *
 * @author jusjoken
 */

public class GemstonePlugin extends AbstractPlugin {

    private static boolean OneTimeStartComplete = false;
    private static boolean OneTimePluginLoadedComplete = false;
    private static List<String> NonPCClients = new LinkedList<String>(); 
    
    public GemstonePlugin(SageTVPluginRegistry registry) { 
        super(registry); 
        Log.debug("GemstonePlugin","Gemstone Plugin registered: " + util.LogInfo());
    }    

    @SageEvent(value = SageEvents.AllPluginsLoaded, background = true)
    public void onPluginsLoaded() {  
        if (OneTimePluginLoadedComplete){
            Log.debug("GemstonePlugin","onPluginsLoaded: Plugins previously loaded: " + util.LogInfo());
        }else{
            OneTimePluginLoadedComplete = true;
            Log.debug("GemstonePlugin","onPluginsLoaded: All Plugins Loaded: " + util.LogInfo());
        }
    }  
    
    @SageEvent(value=SageEvents.ClientConnected, background=true)
    public void onClientConnected(Map args) {
        //System.out.println("*****GEMSTONE onClientConnected - args '" + args + "' - " + util.LogInfo());
        if (args.containsKey("MACAddress")){
            if (args.get("MACAddress")==null){
                Log.debug("GemstonePlugin","onClientConnected: MACAddress null so must be a PC client. Client will handle ClientStart. '" + args + "' " + util.LogInfo());
            }else{
                String tClient = args.get("MACAddress").toString();
                if (NonPCClients.contains(tClient)){
                    Log.debug("GemstonePlugin","onClientConnected: client already loaded: '" + tClient + "' IP '" + args.get("IPAddress") + "' Client will handle ClientStart: " + util.LogInfo());
                }else{
                    Log.debug("GemstonePlugin","onClientConnected: new client added: '" + tClient + "' IP '" + args.get("IPAddress") + "' Client will handle ClientStart: " + util.LogInfo());
                    NonPCClients.add(args.get("MACAddress").toString());
                    //api.clientStart() is called from the STV ApplicationStarted hook to ensure all prerequisites are loaded first
                    //api.ClientStart();
                }
            }
        }else{
            Log.debug("GemstonePlugin","onClientConnected: no MACAddress entry found'" + args + "' " + util.LogInfo());
        }
    }

    @SageEvent(value=SageEvents.ClientDisconnected, background=true)
    public void onClientDisconnected(Map args) {
        Log.debug("GemstonePlugin","onClientDisconnected: called '" + args + "' " + util.LogInfo());
        if (args.containsKey("MACAddress")){
            if (args.get("MACAddress")==null){
                Log.debug("GemstonePlugin","onClientDisconnected: MACAddress null so must be a PC client. Not running server side ClientExit. '" + args + "' " + util.LogInfo());
            }else{
                String tClient = args.get("MACAddress").toString();
                if (NonPCClients.contains(tClient)){
                    Log.debug("GemstonePlugin","onClientDisconnected: client found and removed: '" + tClient + "' IP '" + args.get("IPAddress") + "' " + util.LogInfo());
                    NonPCClients.remove(tClient);
                    api.ClientExit(tClient);
                }else{
                    Log.debug("GemstonePlugin","onClientDisconnected: client not found: '" + tClient + "' IP '" + args.get("IPAddress") + "' " + util.LogInfo());
                }
            }
        }else{
            Log.debug("GemstonePlugin","onClientDisconnected: no MACAddress entry found'" + args + "' " + util.LogInfo());
        }
    }    

    @Override
    public void start() {  
        //System.out.println("*****GEMSTONE start" + util.LogInfo());
        if (!OneTimeStartComplete){
            api.Load();
            //api.clientStart() is called from the STV ApplicationStarted hook to ensure all prerequisites are loaded first
            //api.ClientStart();
            OneTimeStartComplete = true;
            Log.debug("GemstonePlugin","start: one time api load completed: " + util.LogInfo());
        }
        Log.debug("GemstonePlugin","start: Sage Start called: " + util.LogInfo());
        super.start(); 
    }

    public static List<String> getNonPCClients() {
        return NonPCClients;
    }
    
}
