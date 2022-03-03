/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author jusjoken
 */
public class Log {
    static private String logLevelProp = Const.BaseProp + Const.PropDivider + Const.LogLevel;

    private Properties props = null;
    private static List<String> LogLevels = Arrays.asList("DEBUG","INFO","WARN","ERROR");
    private static String DefaultLevel = "INFO";
    private String originalLevel = "";
    
    public Log() {
        originalLevel = GetLevel();
    }

    public static void info(String message){
        info(null,message);
    }
    public static void info(String caller, String message){
        logMessage("INFO", caller, message);
    }
    public static void debug(String message){
        debug(null,message);
    }
    public static void debug(String caller, String message){
        logMessage("DEBUG", caller, message);
    }
    public static void error(String message){
        error(null,message);
    }
    public static void error(String caller, String message){
        logMessage("ERROR", caller, message);
    }
    public static void fatal(String message){
        fatal(null,message);
    }
    public static void fatal(String caller, String message){
        logMessage("FATAL", caller, message);
    }
    public static void warn(String message){
        warn(null,message);
    }
    public static void warn(String caller, String message){
        logMessage("WARN", caller, message);
    }

    private static void logMessage(String type, String caller, String message){
        String currentType = util.GetServerProperty(logLevelProp,DefaultLevel);
        if(type.equals(currentType) || type.equals("ERROR") || type.equals("INFO") || type.equals("FATAL") || type.equals("WARN")){
            if(caller == null){
                System.out.println("GEMSTONE: " + type + ": " + message);
            }else{
                System.out.println("GEMSTONE: " + type + ": " + caller + "; " + message);
            }
        }
    }

    public static Boolean isDebugEnabled(){
        String currentType = util.GetServerProperty(logLevelProp,DefaultLevel);
        if(currentType.equals("DEBUG")){
            return true;
        }else{
            return false;
        }

    }

    public List<String> GetLevels(){
        return LogLevels;
    }
    
    public boolean IsCurrentLevel(String Level){
        if (Level.equals(GetLevel())){
            return true;
        }
        return false;
    }
    
    public String GetLevel(){
        return util.GetServerProperty(logLevelProp, DefaultLevel);
    }
    
    public void SetLevel(String Level){
        //check if this is a valid level
        if (LogLevels.contains(Level)){
            util.SetServerProperty(logLevelProp, Level);
        }
    }
    
    public void SetLevelNext(){
        String tLevel = GetLevel();
        int levelIndex = LogLevels.indexOf(tLevel);
        if (levelIndex==-1){
            SetLevel(LogLevels.get(0));
        }else{
            levelIndex++;
            if (levelIndex>=LogLevels.size()){
                SetLevel(LogLevels.get(0));
            }else{
                SetLevel(LogLevels.get(levelIndex));
            }
        }
    }
    
    public boolean IsDirty(){
        if (!originalLevel.equals(GetLevel())){
            return true;
        }
        return false;
    }
    
    public void SaveSettings(){
        if (IsDirty()){
            originalLevel = GetLevel();
            Log.info("Log", "SaveSettings: log settings saved.");
        }
    }
    
    public void LoadDefaults(){
        originalLevel = GetLevel();
        //set the SageTV logging to it's default
        if (util.IsClient()){
            setSageLogSettingServer(Boolean.FALSE);
            setSageLogSettingClient(Boolean.FALSE);
        }else{
            setSageLogSettingServer(Boolean.FALSE);
        }
        Log.info("Log","LoadDefaults: log settings set to defaults.");
    }

    public static String GetSageLogSettingLabel(){
        if (util.IsClient()){
            return "Server/Client Debug Log";
        }else{
            return "SageTV Debug Log";
        }
    }
    
    public static String GetSageLogSetting(){
        String retVal = "";
        if (util.IsClient()){
            //client needs to return the server and the client value combined
            if (getSageLogSettingServer()){
                retVal = "On/";
            }else{
                retVal = "Off/";
            }
            if (getSageLogSettingClient()){
                retVal = retVal + "On";
            }else{
                retVal = retVal + "Off";
            }
        }else{
            //just need the server value
            if (getSageLogSettingServer()){
                retVal = "On";
            }else{
                retVal = "Off";
            }
        }
        return retVal;
    }
    
    public static void SetSageLogSettingNext(){
        if (util.IsClient()){
            //client: need to rotate through server and client logging settings
            //values can be false/false, false/true, true/false, true,true
            if (!getSageLogSettingServer() && !getSageLogSettingClient()){
                setSageLogSettingServer(Boolean.FALSE);
                setSageLogSettingClient(Boolean.TRUE);
            }else if (!getSageLogSettingServer() && getSageLogSettingClient()){
                setSageLogSettingServer(Boolean.TRUE);
                setSageLogSettingClient(Boolean.FALSE);
            }else if (getSageLogSettingServer() && !getSageLogSettingClient()){
                setSageLogSettingServer(Boolean.TRUE);
                setSageLogSettingClient(Boolean.TRUE);
            }else{
                setSageLogSettingServer(Boolean.FALSE);
                setSageLogSettingClient(Boolean.FALSE);
            }
        }else{
            //just need to toggle the server value
            if (getSageLogSettingServer()){
                setSageLogSettingServer(Boolean.FALSE);
            }else{
                setSageLogSettingServer(Boolean.TRUE);
            }
        }
    }
    
    private static boolean getSageLogSettingClient(){
        return util.GetPropertyAsBoolean("debug_logging", false);
    }
    private static boolean getSageLogSettingServer(){
        return util.GetServerPropertyAsBoolean("debug_logging", false);
    }
    private static void setSageLogSettingClient(Boolean setting){
        util.SetProperty("debug_logging", setting.toString());
    }
    private static void setSageLogSettingServer(Boolean setting){
        util.SetServerProperty("debug_logging", setting.toString());
    }
    
}
