/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import sagex.UIContext;
import sagex.phoenix.weather.IForecastPeriod;
import sagex.phoenix.weather.ILongRangeForecast;


/**
 *
 * @author jusjoken
 * 08/22/2021 Depricated and not in use as now use GoogleWeather.jar
 * public Gemstone single Weather instance to use across the app and all extenders
 * 02/03/2019 - added functions to support moon phases
 * 01/18/2019 - changes to support Dark Sky
 *            - default changed to Yahoo as it is free still
 *            - DarkSky NEEDS the user to supply an API key
 * 10/02/2017 - units and location are all also stored at server level
 *            - changes to default 90210 as epg/zip_code is no longer used in sagetv
 *            - fix to force an update when the location is set
 * 3/23/2013 - weather impl now stored at Server Level
 *    Note: made this change as the impl is loaded so early in the server start that the client settings were not being loaded
 *    - units and location are stored at the client level
 */
public class Weather {
    private static String implList = util.ConvertListtoString(phoenix.weather2.GetWeatherImplKeys());
    private static final String implDefault = "yahoo";
    private static final String unitsDefault = "Standard";
    private static boolean LoaderActive = false;
    private static boolean weatherInit = false;
    private static boolean useServerForProps = true;

    public static boolean IsLoaderActive(){
        return LoaderActive;
    }
    public static void SetLoaderActive(boolean value){
        LoaderActive = value;
    }
    
    //always call Init first - should be called in the Gemstone Init 
    public static void Init(){
        Log.info("Weather","Init: ***DEPRICATED: weather init started: using server settings: " + util.LogInfo());
        //force phoenix to use the current weather implementation setting
        String curImpl = util.GetOptionName(Const.WeatherProp, Const.WeatherImpl, implDefault, useServerForProps);
        //check the current phoenix impl and only change it if it's different
        if (!curImpl.equals(phoenix.weather2.GetWeatherImplKey())){
            setImpl(curImpl);
        }else{
            Log.debug("Weather","Init: phoenix weather was already set to '" + phoenix.weather2.GetWeatherImplKey() + "'");
            setUnits();
            SetLocation();
        }
        weatherInit = true;
        Log.debug("Weather","Init: weather init completed: " + util.LogInfo());
    }
    
    public static void SetImplNext(){
        util.SetListOptionNext(Const.WeatherProp, Const.WeatherImpl, implList, useServerForProps);
        setImpl(util.GetOptionName(Const.WeatherProp, Const.WeatherImpl, implDefault, useServerForProps));
        UpdateWeather();
    }
    
    private static boolean setImpl(String impl){
        boolean success = true;
        phoenix.weather2.SetWeatherImpl(impl);
        if (!impl.equals(phoenix.weather2.GetWeatherImplKey())){
            //the change failed so try changing to the next in the list
            util.SetListOptionNext(Const.WeatherProp, Const.WeatherImpl, implList, useServerForProps);
            String implNext = util.GetOptionName(Const.WeatherProp, Const.WeatherImpl, implDefault, useServerForProps);
            Log.error("Weather","setImpl: failed to set the phoenix weather to '" + impl + "'. Trying '" + implNext + "' instead.");
            phoenix.weather2.SetWeatherImpl(implNext);
            if (!implNext.equals(phoenix.weather2.GetWeatherImplKey())){
                Log.error("Weather","setImpl: failed to set the phoenix weather to '" + impl + "' or '" + implNext + "'. Using '" + phoenix.weather2.GetWeatherImplKey() + "' instead.");
                success = false;
            }else{
                success = true;
            }
        }
        if (success){
            Log.debug("Weather","setImpl: phoenix weather set to '" + phoenix.weather2.GetWeatherImplKey() + "'");
            //as the impl changed... set units and location if required and do an update
            setUnits();
            SetLocation(true);
        }
        util.SetOption(Const.WeatherProp, Const.WeatherImpl, phoenix.weather2.GetWeatherImplKey(), useServerForProps);
        return success;
    }

    private static void setUnits(){
        String curUnits = util.GetOptionName(Const.WeatherProp, Const.WeatherUnits, unitsDefault, useServerForProps);
        if (!curUnits.toLowerCase().equals(phoenix.weather2.GetUnits().toLowerCase())){
            //units are different so we need to set them
            phoenix.weather2.SetUnits(curUnits);
        }
    }
    
    public static void SetUnitsNext(){
        if (phoenix.weather2.GetUnits().toLowerCase().equals("metric")){
            phoenix.weather2.SetUnits("standard");
        }else{
            phoenix.weather2.SetUnits("metric");
        }
        util.SetOption(Const.WeatherProp, Const.WeatherUnits, phoenix.weather2.GetUnits(), useServerForProps);
    }

    public static void SetLocation(){
    	SetLocation(false);
    }
    public static void SetLocation(Boolean forced){
        String curLoc = util.GetOptionName(Const.WeatherProp, Const.WeatherLoc, util.OptionNotFound, useServerForProps);
        if (!curLoc.equals(phoenix.weather2.GetLocation()) || forced){
            if (curLoc.equals(util.OptionNotFound)){
                //try setting using a default zip code
                curLoc = "90210";
                phoenix.weather2.SetLocation(curLoc);
                Log.debug("Weather","setLoc: no vaild location so setting to default 90210");
            }else{
                phoenix.weather2.SetLocation(curLoc);
                Log.debug("Weather","setLoc: setting location to '" + curLoc + "'");
            }
            //as the location has changed then as long as we are not in the init then update needs to be run
            if (weatherInit){
            	//force the update by switching the units twice - it works 
            	//:TODO - add function in phoenix weather to force an update
            	SetUnitsNext();
            	SetUnitsNext();
            	//force an update
                UpdateWeather();
            }
        }
    }
    
    public static void UpdateWeather(){
        if (!weatherInit){
            Init();
        }
        if (phoenix.weather2.Update()){
            RefreshAllClients();
            Log.debug("Weather","UpdateWeather: " + phoenix.weather2.GetWeatherImplKey() + " at " + phoenix.weather2.GetLocation() + " weather updated for '" + phoenix.weather2.GetLocationName() + "'(" + phoenix.weather2.GetLocation() + ") as of '" + phoenix.weather2.GetRecordedDate() + "'. Units '" + phoenix.weather2.GetUnits() + "'");
        }else{
            if (phoenix.weather2.HasError()){
                Log.debug("Weather","UpdateWeather: " + phoenix.weather2.GetWeatherImplKey() + " weather update not performed. '" + phoenix.weather2.GetError() + "'");
            }else{
                RefreshAllClients();
                Log.debug("Weather","UpdateWeather: " + phoenix.weather2.GetWeatherImplKey() + " weather update not performed. Last updated '" + sagex.api.Utility.PrintDateFull(phoenix.weather2.GetRecordedDate().getTime()) + " " + sagex.api.Utility.PrintTimeShort(phoenix.weather2.GetRecordedDate().getTime()) + "'");
            }
        }
    }
    
    private static void RefreshAllClients(){
        //based on the Sage Menu that is displayed - refresh the screen
        //do this for the current context as well as each connected client
        List<String> clientList = new ArrayList<String>();
        clientList.addAll(GemstonePlugin.getNonPCClients());
        if (!clientList.contains(sagex.api.Global.GetUIContextName())){
            clientList.add(sagex.api.Global.GetUIContextName());
        }
        for (String client:clientList){
            RefreshWeatherScreens(client);
        }
    }
    
    private static void RefreshWeatherScreens(String Context){
        if (Context==null){
            Log.debug("Weather","RefreshWeatherScreens: called with null Context");
            return;
        }
        UIContext tUI = new UIContext(Context);
        String HeaderRefreshArea = "WeatherConditionsArea";
        String thisMenu = sagex.api.WidgetAPI.GetWidgetName(sagex.api.WidgetAPI.GetCurrentMenuWidget(tUI));
        if (thisMenu==null){
            Log.debug("Weather","RefreshWeatherScreens: null MenuWidget found.");
        }else if (thisMenu.equals("Main Menu")){
            //refresh each of the valid widget areas
            if (Widget.HasWeatherWidget()){
                sagex.api.Global.RefreshArea(tUI,"Main Menu Widget Panel");
                Log.debug("Weather","RefreshWeatherScreens: refreshed weather widgets for UI '" + Context + "'");
            }else{
                sagex.api.Global.RefreshArea(tUI, HeaderRefreshArea);
                Log.debug("Weather","RefreshWeatherScreens: refreshed main menu header for UI '" + Context + "'");
            }
        }else if (thisMenu.equals("Gemstone Weather")){
            sagex.api.Global.Refresh(tUI);
            Log.debug("Weather","RefreshWeatherScreens: refreshed Gemstone Weather for UI '" + Context + "'");
        }else{
            sagex.api.Global.RefreshArea(tUI, HeaderRefreshArea);
            Log.debug("Weather","RefreshWeatherScreens: refreshed header for UI '" + Context + "'");
        }
    }

    /*
    public static String GetNightShortName(IForecastPeriod forecastperiod){
        String tName = "";
        if (phoenix.weather2.IsDay(forecastperiod)){
            return "Tomorrow";
        }else{
            if (phoenix.weather2.GetForecastDay(forecastperiod)==0){
                return "Tonight";
            }else{
                return phoenix.weather2.GetDay(forecastperiod) + " Night";
            }
        }
    }
    
    public static String GetLocationTitle(){
        if (!phoenix.weather2.IsConfigured()){
            return "Not Configured";
        }
        String tLoc = phoenix.weather2.GetLocation();
        String tName = phoenix.weather2.GetLocationName();
        if (isValidZIP(tLoc)){
            return tName + " (" + tLoc + ")";
        }else{
            return tName;
        }
    }
    
    private static boolean isValidZIP(String ZIPCode){
        //String regex = "^\\d{5}(-\\d{4})?$"; //extended zip
        String regex = "^\\d{5}";  //5 digit zip
        //Log.debug("Weather","isValidZIP: checking for ZIP '" + ZIPCode + "'");
        return Pattern.matches(regex, ZIPCode);
    }
    
    public static String GetBackground(){
        IForecastPeriod current = phoenix.weather2.GetCurrentWeather();
        return GetBackground(current);
    }
    public static String GetBackground(IForecastPeriod current){
        if (phoenix.weather2.IsConfigured()){
            int code = phoenix.weather2.GetCode(current);
            return GetBackground(code);
        }else{
            return "";
        }
    }
    public static String GetBackground(int code){
        if (phoenix.weather2.IsConfigured()){
            if (code>-1 && code <48){
                //use the proper day or night code
                if (WIcons.IsDaytime()){
                    code = phoenix.weather2.GetCodeForceDay(code);
                }else{
                    code = phoenix.weather2.GetCodeForceNight(code);
                }
                
                //determine which image file to return
                int BGIndex = GetBackgroundIndex(code);
                if (GetBackgroundsList(code).size()>BGIndex){
                    return GetBackgroundsPath(code) + GetBackgroundsList(code).get(BGIndex);
                }else{
                    if (GetBackgroundsList(code).size()>0){
                        //adjust the index back to 0
                        SetBackgroundIndex(code, 0);
                        return GetBackgroundsPath(code) + GetBackgroundsList(code).get(0);
                    }
                    return "";
                }
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public static ArrayList<String> GetBackgroundsList(int code){
        SortedSet<String> tList = new TreeSet<String>();
        File BGLoc = new File(GetBackgroundsPath(code));
        if (BGLoc!=null){
            File[] files = BGLoc.listFiles();
            if (files==null){
                Log.debug("Weather","GetBackgroundsList: for code '" + code + "' invalid backgrounds location '" + GetBackgroundsPath(code) + "'");
                return new ArrayList<String>();
            }else{
                for (File file : files){
                    if (!file.isDirectory()){
                        tList.add(file.getName());
                    }
                }
                //Log.debug("Weather","GetBackgroundsList: for code '" + code + "' found '" + tList + "'");
                return new ArrayList<String>(tList);
            }
        }else{
            Log.debug("Weather","GetBackgroundsList: for code '" + code + "' invalid backgrounds location '" + GetBackgroundsPath(code) + "'");
            return new ArrayList<String>();
        }
    }
    
    public static String GetBackgroundsPath(int code){
        return util.WeatherLocation() + File.separator + "Backgrounds" + File.separator + code + File.separator;
    }

    private static String getBGPropLocation(int code){
        return Const.Weather + Const.PropDivider + Const.WeatherBGIndex + Const.PropDivider + code;
    }
    
    public static int GetBackgroundIndex(int code){
        return util.GetPropertyAsInteger(getBGPropLocation(code), 0);
    }
    public static void SetBackgroundIndexNext(int code){
        int BGIndex = GetBackgroundIndex(code)+1;
        util.SetProperty(getBGPropLocation(code), BGIndex + "");
    }
    public static void SetBackgroundIndex(int code, int newIndex){
        util.SetProperty(getBGPropLocation(code), newIndex + "");
    }
    
    public static String GetMoonPhaseImage(ILongRangeForecast longrangeforecast){
        return util.WeatherLocation() + File.separator + "MoonPhases" + File.separator + phoenix.weather2.GetMoonPhaseFileName(longrangeforecast,"moon_","jpg");
    }

    public static String GetMoonPhaseImage(IForecastPeriod iforecastperiod){
        return util.WeatherLocation() + File.separator + "MoonPhases" + File.separator + phoenix.weather2.GetMoonPhaseFileName(iforecastperiod,"moon_","jpg");
    }

    public static String GetIconImage(IForecastPeriod iforecastperiod){
        if (phoenix.weather2.GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            return WIcons.GetWeatherIconByNumber(phoenix.weather2.GetCode(iforecastperiod));
        }
    }
    public static String GetIconImageCurrent(IForecastPeriod iforecastperiod){
        if (phoenix.weather2.GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            if (WIcons.IsDaytime()){
                return WIcons.GetWeatherIconByNumber(phoenix.weather2.GetCodeForceDay(phoenix.weather2.GetCode(iforecastperiod)));
            }else{
                return WIcons.GetWeatherIconByNumber(phoenix.weather2.GetCodeForceNight(phoenix.weather2.GetCode(iforecastperiod)));
            }
        }
    }
    public static String GetIconImageDay(IForecastPeriod iforecastperiod){
        if (phoenix.weather2.GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            return WIcons.GetWeatherIconByNumber(phoenix.weather2.GetCodeForceDay(phoenix.weather2.GetCode(iforecastperiod)));
        }
    }
    public static String GetIconImageNight(IForecastPeriod iforecastperiod){
        if (phoenix.weather2.GetCode(iforecastperiod)==-1){
            return WIcons.GetWeatherIconByNumber("na");
        }else{
            return WIcons.GetWeatherIconByNumber(phoenix.weather2.GetCodeForceNight(phoenix.weather2.GetCode(iforecastperiod)));
        }
    }

    public static boolean HasDescription(){
        if (phoenix.weather2.GetForecastPeriods()==null){
            return false;
        }
        if (phoenix.weather2.IsSupported(phoenix.weather2.GetDescription(phoenix.weather2.GetForecastPeriod(0)))){
            return true;
        }else{
            return false;
        }
        
    }
    public static int GetForecastColumns(boolean ForecastExpandFocused){
        if (HasDescription()){
            if (ForecastExpandFocused){
                return 3;
            }else{
                return 5;
            }
        }else if(phoenix.weather2.GetForecastDays()<3){
            return 2;
        }else{
            if (ForecastExpandFocused){
                return 4;
            }else{
                return 5;
            }
        }
    }
    
    public static double GetForecastWidth(boolean Focused, boolean ForecastExpandFocused, IForecastPeriod NightPeriod){
        if (HasDescription()){
            if (Focused){
                if (ForecastExpandFocused){
                    if (NightPeriod==null){
                        return (1.0/5)*1.5;
                    }else{
                        return (1.0/5)*3;
                    }
                }else{
                    return (1.0/5);
                }
            }else{
                return (1.0/5);
            }
        }else if(phoenix.weather2.GetForecastDays()<3){
            return (1.0/2);
        }else{
            if (Focused){
                if (ForecastExpandFocused){
                    if (NightPeriod==null){
                        return (1.0/5)*1;
                    }else{
                        return (1.0/5)*2;
                    }
                }else{
                    return (1.0/5);
                }
            }else{
                return (1.0/5);
            }
        }
    }
    
    public static boolean UseSplitForecast(boolean Focused, boolean ForecastExpandFocused){
        if (HasDescription()){
            if (Focused){
                if (ForecastExpandFocused){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }else if(phoenix.weather2.GetForecastDays()<3){
            return true;
        }else{
            if (Focused){
                if (ForecastExpandFocused){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
    }
    */
}
