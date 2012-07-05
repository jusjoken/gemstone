/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.util.Collection;
import java.util.LinkedHashSet;
import org.apache.log4j.Logger;
import sage.google.weather.GoogleWeather;
import tv.sage.weather.WeatherDotCom;

/**
 *
 * @author jusjoken
 */
public class WeatherAPI {
    private static enum APITypes{GOOGLE,WEATHERCOM};
    private APITypes APIType = APITypes.GOOGLE;
    private GoogleWeather gWeather = null;
    private WeatherDotCom wWeather = null;
    private myPhoenix.IWeatherData pWeather = null;
    static private final Logger LOG = Logger.getLogger(WeatherAPI.class);
    
    public WeatherAPI(String APIType) {
        //used for temporarily getting a WeatherAPI object without saving the type
        this.APIType = getAPIType(APIType);
    }
    public WeatherAPI() {
        this.APIType = getAPIType();
        //InitAPI();
    }

    private APITypes getAPIType() {
        String tAPIType = util.GetOptionName(Const.WeatherProp, "APIType", APITypes.GOOGLE.toString());
        if (tAPIType.equals(APITypes.GOOGLE.toString())){
            return APITypes.GOOGLE;
        }else if (tAPIType.equals(APITypes.WEATHERCOM.toString())){
            return APITypes.WEATHERCOM;
        }else{
            return APITypes.GOOGLE;
        }
    }
    private APITypes getAPIType(String tAPIType) {
        APITypes tempAPIType = APITypes.GOOGLE;
        if (tAPIType.equals(APITypes.GOOGLE.toString())){
            tempAPIType = APITypes.GOOGLE;
        }else if (tAPIType.equals(APITypes.WEATHERCOM.toString())){
            tempAPIType = APITypes.WEATHERCOM;
        }
        return tempAPIType;
    }

    private void setAPIType(APITypes newAPIType) {
        APITypes currAPIType = getAPIType();
        //LOG.debug("setAPIType: current '" + currAPIType + "' changing to '" + newAPIType + "'");
        if (!newAPIType.equals(currAPIType)){
            //change the type
            util.SetOption(Const.WeatherProp, "APIType", newAPIType.toString());
            APIType = newAPIType;
        }
    }
    
    public void Init(){
        LOG.debug("Init: Type '" + APIType + "'");
        if (APIType.equals(APITypes.WEATHERCOM)){
            wWeather = WeatherDotCom.getInstance();
            gWeather = null;
            if (!myPhoenix.weather.IsConfigured()){
                myPhoenix.weather.SetLocation("55373");
                myPhoenix.weather.SetUnits("s");
            }else{
                myPhoenix.weather.SetLocation("55373");
                myPhoenix.weather.SetUnits("s");
            }
            myPhoenix.weather.Update();
            pWeather = myPhoenix.weather.GetCurrentWeather();
        }else{
            //default to GOOGLE
            gWeather = GoogleWeather.getInstance();
            wWeather = null;
        }
    }
    public void Update(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            LOG.debug("Update: updating WEATHERCOM");
            wWeather.updateNow();
            myPhoenix.weather.Update();
        }else{
            //Google
            //get the language code
            String LangCode = util.GetProperty("ui/translation_language_code", "en");
            if (IsGoogleNWSWeather()){
                LOG.debug("Update: updating GOOGLE and NWS");
                gWeather.updateAllNow(LangCode);
            }else{
                LOG.debug("Update: updating GOOGLE");
                gWeather.updateGoogleNow(LangCode);
            }
        }
    }
    public Boolean IsConfigured(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            if (wWeather==null){
                return Boolean.FALSE;
            }else{
                return Boolean.TRUE;
            }
        }else{
            if (gWeather==null){
                return Boolean.FALSE;
            }else{
                return Boolean.TRUE;
            }
        }
    }
    public Boolean IsWeatherDotCom(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }
    public Boolean IsGoogleWeather(){
        if (APIType.equals(APITypes.GOOGLE)){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }
    public Boolean IsGoogleNWSWeather(){
        if (APIType.equals(APITypes.GOOGLE)){
            if (gWeather.getNWSZipCode()==null || gWeather.getNWSZipCode().length()==0){
                return Boolean.FALSE;
            }else{
                return Boolean.TRUE;
            }
        }else{
            return Boolean.FALSE;
        }
    }
    public String GetType() {
        return util.GetOptionName(Const.WeatherProp, "APIType", APITypes.GOOGLE.toString());
    }
    //get a name for menu items/settings
    public String GetTypeName() {
        String tAPIType = util.GetOptionName(Const.WeatherProp, "APIType", APITypes.GOOGLE.toString());
        if (tAPIType.equals(APITypes.GOOGLE.toString())){
            return "Google Weather";
        }else if (tAPIType.equals(APITypes.WEATHERCOM.toString())){
            return "Weather.com";
        }else{
            return "Google Weather";
        }
    }
    //change to the next valid WeatherAPI type
    public void NextType() {
        APITypes tempAPIType = getAPIType();
        if (tempAPIType.equals(APITypes.GOOGLE)){
            setAPIType(APITypes.WEATHERCOM);
        }else{
            setAPIType(APITypes.GOOGLE);
        }
    }
    //allow override of the default conditions or forecast display
    public String GetDefaultConditionsDisplay() {
        String tDefault = util.GetOptionName(Const.WeatherProp, "DefaultConditionsDisplay", util.OptionNotFound);
        if (tDefault.equals(util.OptionNotFound) || tDefault.equals("Default")){
            if (APIType.equals(APITypes.WEATHERCOM)){
                return "Old";
            }else{
                return "New";
            }
        }else{
            return tDefault;
        }
    }
    public String GetDefaultConditionsButtonText() {
        String tDefault = util.GetOptionName(Const.WeatherProp, "DefaultConditionsDisplay", util.OptionNotFound);
        if (tDefault.equals(util.OptionNotFound)){
            return "Default";
        }else{
            return tDefault;
        }
    }
    public void SetDefaultConditionsDisplayNext() {
        String Value = util.GetOptionName(Const.WeatherProp, "DefaultConditionsDisplay", util.OptionNotFound);
        if (Value.equals("New")){
            util.SetOption(Const.WeatherProp, "DefaultConditionsDisplay", "Old");
        }else if (Value.equals("Old")){
            util.SetOption(Const.WeatherProp, "DefaultConditionsDisplay", "Default");
        }else{
            util.SetOption(Const.WeatherProp, "DefaultConditionsDisplay", "New");
        }
    }
    public String GetDefaultForecastDisplay() {
        String tDefault = util.GetOptionName(Const.WeatherProp, "DefaultForecastDisplay", util.OptionNotFound);
        if (tDefault.equals(util.OptionNotFound) || tDefault.equals("Default")){
            if (APIType.equals(APITypes.WEATHERCOM)){
                return "Old";
            }else{
                return "New";
            }
        }else{
            return tDefault;
        }
    }
    public String GetDefaultForecastButtonText() {
        String tDefault = util.GetOptionName(Const.WeatherProp, "DefaultForecastDisplay", util.OptionNotFound);
        if (tDefault.equals(util.OptionNotFound)){
            return "Default";
        }else{
            return tDefault;
        }
    }
    public void SetDefaultForecastDisplayNext() {
        String Value = util.GetOptionName(Const.WeatherProp, "DefaultForecastDisplay", util.OptionNotFound);
        if (Value.equals("New")){
            util.SetOption(Const.WeatherProp, "DefaultForecastDisplay", "Old");
        }else if (Value.equals("Old")){
            util.SetOption(Const.WeatherProp, "DefaultForecastDisplay", "Default");
        }else{
            util.SetOption(Const.WeatherProp, "DefaultForecastDisplay", "New");
        }
    }
    //get 3 entries for the short forecast list
    public Collection<String> GetDayListShort(){
        Collection<String> tList = new LinkedHashSet<String>();
        if (APIType.equals(APITypes.WEATHERCOM)){
            if (FCHasTodaysHigh()){
                tList.add("A");
                tList.add("B");
                tList.add("1");
            }else{
                tList.add("B");
                tList.add("1");
                tList.add("2");
            }
        }else{
            if (FCHasTodaysHigh()){
                tList.add("A");
                tList.add("B");
                tList.add("1");
            }else{
                tList.add("B");
                tList.add("1");
                tList.add("2");
            }
        }
        LOG.debug("GetDayListShort: List '" + tList + "'");
        return tList;
    }
    //get enough entries based on the sources available entries
    public Collection<String> GetDayList(){
        Collection<String> tList = new LinkedHashSet<String>();
        Integer MaxItems = 0;
        if (APIType.equals(APITypes.WEATHERCOM)){
            if (FCHasTodaysHigh()){
                tList.add("A");
            }else{
                tList.add("N/A");
            }
            tList.add("B");
            tList.add("d1");
            tList.add("n1");
            tList.add("d2");
            tList.add("n2");
            tList.add("d3");
            tList.add("n3");
            tList.add("d4");
            tList.add("n4");
        }else{
            if (IsGoogleNWSWeather()){
                MaxItems = gWeather.getNWSPeriodCount();
                if (!FCHasTodaysHigh()){
                    MaxItems++;
                }
            }else{
                MaxItems = gWeather.getGWDayCount()*2;
            }
            if (FCHasTodaysHigh()){
                tList.add("A");
            }else{
                tList.add("N/A");
            }
            if (MaxItems>tList.size())tList.add("B");
            if (MaxItems>tList.size())tList.add("d1");
            if (MaxItems>tList.size())tList.add("n1");
            if (MaxItems>tList.size())tList.add("d2");
            if (MaxItems>tList.size())tList.add("n2");
            if (MaxItems>tList.size())tList.add("d3");
            if (MaxItems>tList.size())tList.add("n3");
            if (MaxItems>tList.size())tList.add("d4");
            if (MaxItems>tList.size())tList.add("n4");
            if (MaxItems>tList.size())tList.add("d5");
            if (MaxItems>tList.size())tList.add("n5");
            if (MaxItems>tList.size())tList.add("d6");
            if (MaxItems>tList.size())tList.add("n6");
        }
        LOG.debug("GetDayList: List '" + tList + "' Max '" + MaxItems + "'" );
        return tList;
    }
    public Integer GetDayCount(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return 5;
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSPeriodCount()/2;
            }else{
                return gWeather.getGWDayCount();
            }
        }
    }
    
    public String GetTemp(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            //need to strip off the degree and unit from the temp
            //String tTemp = wWeather.getCurrentCondition("curr_temp");
            //return tTemp.substring(0, tTemp.length()-2);
            String tTemp = pWeather.getTemp();
            return tTemp.substring(0, tTemp.length()-2);
        }else{
            return gWeather.getGWCurrentCondition("Temp");
        }
    }
    //return the temp with the degrees and units as part of the display
    public String GetTempFull(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            //this already has the units so just return the results
            LOG.debug("GetTempFull: pWeather '" + pWeather + "' temp '" + pWeather.getTemp() + "'");
            return pWeather.getTemp();
            //return wWeather.getCurrentCondition("curr_temp");
        }else{
            return gWeather.getGWCurrentCondition("Temp") + GetUnitsDisplay();
        }
    }
    public void SetUnits(String Value){
        if (APIType.equals(APITypes.WEATHERCOM)){
            wWeather.setUnits(Value);
        }else{
            gWeather.setUnits(Value);
        }
    }
    public String GetUnits(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            //LOG.debug("GetUnits: '" + myPhoenix.weather.GetUnits() + "'");
            return myPhoenix.weather.GetUnits().toLowerCase().substring(0,1);
            //return wWeather.getUnits();
        }else{
            return gWeather.getUnits();
        }
    }
    public String GetUnitsDisplay(){
        if (GetUnits().equals("s")){
            return "\u00B0" + "F";
        }else{
            return "\u00B0" + "C";
        }
    }
    public String GetIcon(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return WIcons.GetWeatherIconByNumber(wWeather.getCurrentCondition("curr_icon"));
        }else{
            return WIcons.GetWeatherIconURL(gWeather.getGWCurrentCondition("iconURL"));
        }
    }
    public String GetFCIcon(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            if (iDay==0 && !FCHasTodaysHigh()){
                return WIcons.GetWeatherIconByNumber(wWeather.getForecastCondition("icon" + "n" + iDay));
            }else{
                return WIcons.GetWeatherIconByNumber(wWeather.getForecastCondition("icon" + "d" + iDay));
            }
        }else{
            return WIcons.GetWeatherIconURLDay(gWeather.getGWForecastCondition(iDay, "iconURL"));
        }
    }
    public String GetFCIcon(Object DayNumber, String DayPart){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return WIcons.GetWeatherIconByNumber(wWeather.getForecastCondition("icon" + ValidateDayPart(DayPart) + iDay));
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                //LOG.debug("GetFCIcon: iDay '" + iDay + "' DayPart '" + DayPart + "' Period '" + GetPeriod(iDay, ValidateDayPart(DayPart)) + "'");
                return WIcons.GetWeatherIconURLDay(gWeather.getNWSForecastCondition(GetPeriod(iDay, ValidateDayPart(DayPart)), "icon_url"));
            }else{
                return WIcons.GetWeatherIconURLDay(gWeather.getGWForecastCondition(iDay, "iconURL"));
            }
            
        }
    }
    public String GetFCIconPeriod(Integer Period){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return WIcons.GetWeatherIconByNumber(wWeather.getForecastCondition("icon" + GetDayPartFromPeriod(Period) + GetDayFromPeriod(Period)));
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                return WIcons.GetWeatherIconURLDay(gWeather.getNWSForecastCondition(Period, "icon_url"));
            }else{
                return WIcons.GetWeatherIconURLDay(gWeather.getGWForecastCondition(GetDayFromPeriod(Period), "iconURL"));
            }
            
        }
    }
    public String GetHumidity(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_humidity");
        }else{
            return gWeather.getGWCurrentCondition("HumidText").replaceAll("Humidity:", "").trim();
        }
    }
    public String GetFCHumidity(Object DayNumber){
        return GetFCHumidity(DayNumber, "d");
    }
    public String GetFCHumidityPeriod(Integer Period){
        return GetFCHumidity(GetDayFromPeriod(Period), GetDayPartFromPeriod(Period));
    }
    public String GetFCHumidity(Object DayNumber, String DayPart){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tHumidity = wWeather.getForecastCondition("humid" + ValidateDayPart(DayPart) + iDay);
            return tHumidity;
        }else{
            return "N/A";
        }
    }
    public String GetSunrise(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getLocationInfo("curr_sunrise");
        }else{
            return "N/A";
        }
    }
    public String GetSunset(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getLocationInfo("curr_sunset");
        }else{
            return "N/A";
        }
    }
    public String GetFCSunrisePeriod(Integer Period){
        if (GetDayPartFromPeriod(Period).equals("n")){
            return "N/A";
        }else{
            return GetFCSunrise(GetDayFromPeriod(Period));
        }
    }
    public String GetFCSunrise(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("sunrise" + iDay);
        }else{
            return "N/A";
        }
    }
    public String GetFCSunsetPeriod(Integer Period){
        if (GetDayPartFromPeriod(Period).equals("d")){
            return "N/A";
        }else{
            return GetFCSunset(GetDayFromPeriod(Period));
        }
    }
    public String GetFCSunset(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("sunset" + iDay);
        }else{
            return "N/A";
        }
    }
    public String GetUVIndex(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_uv_index");
        }else{
            return "N/A";
        }
    }
    public String GetUVWarn(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_uv_warning");
        }else{
            return "N/A";
        }
    }
    public String GetVisibility(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_visibility");
        }else{
            return "N/A";
        }
    }
    public String GetFeelsLike(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_windchill");
        }else{
            return "N/A";
        }
    }
    public String GetBarometer(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_pressure");
        }else{
            return "N/A";
        }
    }
    public String GetDewPoint(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_dewpoint");
        }else{
            return "N/A";
        }
    }
    public String GetCondition(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_conditions");
        }else{
            return gWeather.getGWCurrentCondition("CondText");
        }
    }
    public String GetWind(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tWind = wWeather.getCurrentCondition("curr_wind");
            if (tWind.startsWith("CALM")){
                return "CALM";
            }
            String WindDir = tWind.substring(0, tWind.indexOf(" "));
            String WindSpeed = tWind.substring(tWind.indexOf(" ")+1);
            return WindDir + "/" + WindSpeed;
        }else{
            String tWind = gWeather.getGWCurrentCondition("WindText");
            if (tWind.contains("0 mph")){
                return "CALM";
            }else{
                return tWind.replaceAll("Wind:", "").trim().replaceFirst(" at ", "/");
            }
        }
    }
    public String GetFCWind(Object DayNumber){
        return GetFCWind(DayNumber, "d");
    }
    public String GetFCWindPeriod(Integer Period){
        return GetFCWind(GetDayFromPeriod(Period), GetDayPartFromPeriod(Period));
    }
    public String GetFCWind(Object DayNumber, String DayPart){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tWind = wWeather.getForecastCondition("wind" + ValidateDayPart(DayPart) + iDay);
            //LOG.debug("GetFCWind: '" + tWind + "'");
            if (tWind.startsWith("CALM")){
                return "CALM";
            }
            String WindDir = tWind.substring(0, tWind.indexOf(" "));
            String WindSpeed = tWind.substring(tWind.indexOf(" ")+1);
            //LOG.debug("GetFCWind: returning '" + WindDir + "/" + WindSpeed + "'");
            return WindDir + "/" + WindSpeed;
        }else{
            return "N/A";
        }
    }
    public String GetLocation(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getLocationInfo("curr_location");
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getGWCityName() + " (" + gWeather.getNWSZipCode() + ")";
            }else{
                return gWeather.getGWCityName();
            }
            
        }
    }
    //Use the Ext function for sources that have a second source like NWS to return the 2nd source
    public String GetLocationExt(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getLocationInfo("curr_location");
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSCityName() + " (" + gWeather.getNWSZipCode() + ")";
            }else{
                return gWeather.getGWCityName();
            }
            
        }
    }
    public String GetLocationID(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getLocationID();
        }else{
            return gWeather.getGoogleWeatherLoc();
        }
    }
    public void SetLocationID(String Value){
        if (APIType.equals(APITypes.WEATHERCOM)){
            wWeather.setLocationID(Value);
        }else{
            gWeather.setGoogleWeatherLoc(Value);
        }
    }
    public void RemoveLocationID(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            wWeather.setLocationID("");
        }else{
            gWeather.removeGoogleWeatherLoc();
        }
    }
    //Use the Ext function for sources that have a second source like NWS to return the 2nd source
    public String GetLocationIDExt(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getLocationID();
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSZipCode();
            }else{
                return gWeather.getGoogleWeatherLoc();
            }
            
        }
    }
    public Boolean SetLocationIDExt(String Value){
        if (APIType.equals(APITypes.WEATHERCOM)){
            wWeather.setLocationID(Value);
            return Boolean.TRUE;
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.setNWSZipCode(Value);
            }else{
                return Boolean.FALSE;
            }
            
        }
    }
    public void RemoveLocationIDExt(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            //setting this to blank may need to be tested
            wWeather.setLocationID("");
        }else{
            if (IsGoogleNWSWeather()){
                gWeather.removeNWSZipCode();
            }else{
                //do nothing as has no Extended function
            }
            
        }
    }
    public String GetRecordedAtLocation(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getCurrentCondition("curr_recorded_at");
        }else{
            return gWeather.getGWCityName();
        }
    }
    public String GetUpdateTime(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            //return wWeather.getCurrentCondition("curr_updated");
            return myPhoenix.weather.GetLastUpdated().toString();
        }else{
            Long tTime = gWeather.getLastUpdateTimeGW();
            return sagex.api.Utility.PrintDateLong(tTime) + " at " + sagex.api.Utility.PrintTimeShort(tTime);
        }
    }
    public String GetUpdateTimeExt(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return myPhoenix.weather.GetLastUpdated().toString();
            //return wWeather.getCurrentCondition("curr_updated");
        }else{
            Long tTime = gWeather.getLastUpdateTimeGW();
            if (IsGoogleNWSWeather()){
                tTime = gWeather.getLastUpdateTimeNWS();
            }
            return sagex.api.Utility.PrintDateLong(tTime) + " at " + sagex.api.Utility.PrintTimeShort(tTime);
        }
    }
    public String GetFCDayName(Object DayNumber){
        //return a short dayname like Sun, Mon, Tues etc (first 3 letters)
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tName = wWeather.getForecastCondition("date" + iDay);
            return tName.substring(0, 3);
        }else{
            return gWeather.getGWForecastCondition(iDay, "name");
        }
    }
    public String GetFCDayNameFull(Object DayNumber){
        //return the full dayname that is available
        //LOG.debug("GetFCDayNameFull: for '" + DayNumber + "'");
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (iDay==0){
            return "Today";
        }
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("date" + iDay);
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "name");
            }else{
                return gWeather.getGWForecastCondition(iDay, "name");
            }
        }
    }
    public String GetFCDayName(Object DayNumber, String DayPart){
        //return a custom day name for the forecast displays
        //LOG.debug("GetFCDayNameFull: for '" + DayNumber + "'");
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (iDay==0){
            if (ValidateDayPart(DayPart).equals("d")){
                return "Today";
            }else{
                return "Tonight";
            }
        }else{
            if (APIType.equals(APITypes.WEATHERCOM)){
                String tDay = wWeather.getForecastCondition("date" + iDay);
                if (ValidateDayPart(DayPart).equals("n")){
                    tDay = tDay.substring(0, 3) + " Night";
                }else{
                    tDay = tDay.substring(0, 3) + " " + tDay.substring(tDay.indexOf(" ")+1);
                }
                //LOG.debug("GetFCDayName: for '" + DayNumber + "' DayPart '" + DayPart + "' = '" + tDay + "'");
                return tDay;
            }else{
                String tDateName = "";
                if (IsGoogleNWSWeather()){
                    tDateName = gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "name");
                    if (tDateName.toLowerCase().equals("monday")){
                        tDateName = tDateName.substring(0, 3);
                    }else if (tDateName.toLowerCase().equals("tuesday")){
                        tDateName = tDateName.substring(0, 3);
                    }else if (tDateName.toLowerCase().equals("wednesday")){
                        tDateName = tDateName.substring(0, 3);
                    }else if (tDateName.toLowerCase().equals("thursday")){
                        tDateName = tDateName.substring(0, 3);
                    }else if (tDateName.toLowerCase().equals("friday")){
                        tDateName = tDateName.substring(0, 3);
                    }else if (tDateName.toLowerCase().equals("saturday")){
                        tDateName = tDateName.substring(0, 3);
                    }else if (tDateName.toLowerCase().equals("sunday")){
                        tDateName = tDateName.substring(0, 3);
                    }else{
                        //return the first word to keep it short
                        tDateName = tDateName.substring(0, tDateName.indexOf(" "));
                    }
                }else{
                    tDateName = gWeather.getGWForecastCondition(iDay, "name");
                }
                if (ValidateDayPart(DayPart).equals("n")){
                    return tDateName + " Night";
                }else{
                    return tDateName;
                }
            }
        }
    }
    public String GetFCDayNamePeriod(Integer Period){
        //return a short dayname like Sun, Mon, Tues etc (first 3 letters)
        Integer iDay = GetDayFromPeriod(Period);
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tName = wWeather.getForecastCondition("date" + iDay);
            return tName.substring(0, 3);
        }else{
            if (IsGoogleNWSWeather()){
                String tDateName = gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "name");
                if (tDateName.toLowerCase().equals("monday")){
                    return tDateName.substring(0, 3);
                }else if (tDateName.toLowerCase().equals("tuesday")){
                    return tDateName.substring(0, 3);
                }else if (tDateName.toLowerCase().equals("wednesday")){
                    return tDateName.substring(0, 3);
                }else if (tDateName.toLowerCase().equals("thursday")){
                    return tDateName.substring(0, 3);
                }else if (tDateName.toLowerCase().equals("friday")){
                    return tDateName.substring(0, 3);
                }else if (tDateName.toLowerCase().equals("saturday")){
                    return tDateName.substring(0, 3);
                }else if (tDateName.toLowerCase().equals("sunday")){
                    return tDateName.substring(0, 3);
                }else{
                    //return the first word to keep it short
                    return tDateName.substring(0, tDateName.indexOf(" "));
                }
            }else{
                return gWeather.getGWForecastCondition(iDay, "name");
            }
        }
    }
    public String GetFCDayNameFullPeriod(Integer Period){
        //return the full dayname that is available
        if (IsGoogleNWSWeather()){
            return gWeather.getNWSForecastCondition(Period, "name");
        }
        Integer iDay = GetDayFromPeriod(Period);
        String DayPart = GetDayPartFromPeriod(Period);
        //special handling for Today and Tonight
        if (iDay==0){
            if (DayPart.equals("d")){
                return "Today";
            }else{
                return "Tonight";
            }
        }else{
            if (APIType.equals(APITypes.WEATHERCOM)){
                String DayName = wWeather.getForecastCondition("date" + iDay);
                DayName = DayName.substring(0, DayName.indexOf(" "));
                if (DayPart.equals("n")){
                    return DayName + " Night";
                }else{
                    return DayName;
                }
            }else{
                return gWeather.getGWForecastCondition(iDay, "name");
            }
        }
    }
    public String GetFCHigh(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            //need to strip off the degree and unit from the temp
            String tTemp = wWeather.getForecastCondition("hi"+iDay);
            return tTemp.substring(0, tTemp.length()-2);
        }else{
            if (IsGoogleNWSWeather()){
                if (iDay==0){
                    if (FCHasTodaysHigh()){
                        return gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "temp");
                    }else{
                        return "N/A";
                    }
                }else{
                    return gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "temp");
                }
            }else{
                return gWeather.getGWForecastCondition(iDay, "high");
            }
        }
    }
    //return the temp with the degrees and units as part of the display
    public String GetFCHighFull(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("hi"+iDay);
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "temp") + GetUnitsDisplay();
            }else{
                return gWeather.getGWForecastCondition(iDay, "high") + GetUnitsDisplay();
            }
        }
    }
    public String GetFCLow(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tTemp = wWeather.getForecastCondition("low"+iDay);
            return tTemp.substring(0, tTemp.length()-2);
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, "n"), "temp");
            }else{
                return gWeather.getGWForecastCondition(iDay, "low");
            }
        }
    }
    //return the temp with the degrees and units as part of the display
    public String GetFCLowFull(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("low"+iDay);
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, "n"), "temp") + GetUnitsDisplay();
            }else{
                return gWeather.getGWForecastCondition(iDay, "low") + GetUnitsDisplay();
            }
        }
    }
    //get a forecst temp for a specified period
    public String GetFCTempPeriod(Integer Period){
        if (IsGoogleNWSWeather()){
            return gWeather.getNWSForecastCondition(Period, "temp");
        }
        //LOG.debug("GetFCTempPeriod: for Period '" + Period + "'");
        if (GetDayPartFromPeriod(Period).equals("d")){
            return GetFCHigh(GetDayFromPeriod(Period));
        }else{
            return GetFCLow(GetDayFromPeriod(Period));
        }
    }
    public String GetFCTempFullPeriod(Integer Period){
        if (IsGoogleNWSWeather()){
            return gWeather.getNWSForecastCondition(Period, "temp") + GetUnitsDisplay();
        }
        if (GetDayPartFromPeriod(Period).equals("d")){
            return GetFCHighFull(GetDayFromPeriod(Period));
        }else{
            return GetFCLowFull(GetDayFromPeriod(Period));
        }
    }
    public Boolean FCHasTodaysHigh(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tHigh = wWeather.getForecastCondition("hi0");
            if (tHigh.startsWith("N/A")){
                return Boolean.FALSE;
            }else{
                return Boolean.TRUE;
            }
        }else{
            if (IsGoogleNWSWeather()){
                String checkPeriod = gWeather.getNWSForecastCondition(0, "tempType");
                if (checkPeriod.equals("h")){
                    return Boolean.TRUE;
                }else{
                    return Boolean.FALSE;
                }
            }else{
                return Boolean.TRUE;
            }
        }
    }
    //get a single forecst condition representing the day
    public String GetFCCondition(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            if (iDay==0 && !FCHasTodaysHigh()){
                return wWeather.getForecastCondition("conditions" + "n" + iDay);
            }else{
                return wWeather.getForecastCondition("conditions" + "d" + iDay);
            }
        }else{
            return gWeather.getGWForecastCondition(iDay, "CondText");
        }
    }
    //get a forecst condition for the specified part of the day
    public String GetFCCondition(Object DayNumber, String DayPart){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("conditions" + ValidateDayPart(DayPart) + iDay);
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                //LOG.debug("GetFCCondition: iDay '" + iDay + "' DayPart '" + DayPart + "' Period '" + GetPeriod(iDay, ValidateDayPart(DayPart)) + "'");
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, ValidateDayPart(DayPart)), "summary");
            }else{
                //without NWS, Google only has one condition
                return gWeather.getGWForecastCondition(iDay, "CondText");
            }
        }
    }
    //get a forecst condition for a specified period
    public String GetFCConditionPeriod(Integer Period){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("conditions" + GetDayPartFromPeriod(Period) + GetDayFromPeriod(Period));
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                return gWeather.getNWSForecastCondition(Period, "summary");
            }else{
                //without NWS, Google only has one condition
                return gWeather.getGWForecastCondition(GetDayFromPeriod(Period), "CondText");
            }
        }
        
    }
    public String GetFCPrecip(Object DayNumber){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            //need to remove the % symbol
            if (iDay==0 && !FCHasTodaysHigh()){
                String tPrecip = wWeather.getForecastCondition("precip" + "n" + iDay);
                return tPrecip.substring(0, tPrecip.length()-1);
            }else{
                String tPrecip = wWeather.getForecastCondition("precip" + "d" + iDay);
                return tPrecip.substring(0, tPrecip.length()-1);
            }
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, "d"), "precip");
            }else{
                return "N/A";
            }
        }
    }
    public String GetFCPrecip(Object DayNumber, String DayPart){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tPrecip = wWeather.getForecastCondition("precip" + ValidateDayPart(DayPart) + iDay);
            return tPrecip.substring(0, tPrecip.length()-1);
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, ValidateDayPart(DayPart)), "precip");
            }else{
                return "N/A";
            }
        }
    }
    public String GetFCPrecipPeriod(Integer Period){
        if (APIType.equals(APITypes.WEATHERCOM)){
            String tPrecip = wWeather.getForecastCondition("precip" + GetDayPartFromPeriod(Period) + GetDayFromPeriod(Period));
            return tPrecip.substring(0, tPrecip.length()-1);
        }else{
            if (IsGoogleNWSWeather()){
                return gWeather.getNWSForecastCondition(Period, "precip");
            }else{
                return "N/A";
            }
        }
    }
    public Boolean HasFCDescription(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return Boolean.FALSE;
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                return Boolean.TRUE;
            }else{
                //without NWS, Google only has one condition
                return Boolean.FALSE;
            }
        }
    }
    public String GetFCDescription(Object DayNumber, String DayPart){
        Integer iDay = util.GetInteger(DayNumber, 0);
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather.getForecastCondition("conditions" + ValidateDayPart(DayPart) + iDay);
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                return gWeather.getNWSForecastCondition(GetPeriod(iDay, ValidateDayPart(DayPart)), "forecast_text");
            }else{
                //without NWS, Google only has one condition
                return gWeather.getGWForecastCondition(iDay, "CondText");
            }
        }
    }
    //get a forecst condition for a specified period
    public String GetFCDescriptionPeriod(Integer Period){
        if (APIType.equals(APITypes.WEATHERCOM)){
            //LOG.debug("GetFCDescriptionPeriod: Period '" + Period + "' DayPart '" + GetDayPartFromPeriod(Period) + "' Day '" + GetDayFromPeriod(Period) + "'" );
            return wWeather.getForecastCondition("conditions" + GetDayPartFromPeriod(Period) + GetDayFromPeriod(Period));
        }else{
            if (IsGoogleNWSWeather()){
                //with NWS need to convert Day and DayPart to a period
                return gWeather.getNWSForecastCondition(Period, "forecast_text");
            }else{
                //without NWS, Google only has one condition
                return gWeather.getGWForecastCondition(GetDayFromPeriod(Period), "CondText");
            }
        }
        
    }
    public Boolean HasExtForecast(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return Boolean.TRUE;
        }else{
            if (IsGoogleNWSWeather()){
                return Boolean.TRUE;
            }else{
                //without NWS, Google does not have enough info for an extended forecast
                return Boolean.FALSE;
            }
        }
    }
    public String GetFCTempTypeText(Integer Period){
        if (APIType.equals(APITypes.WEATHERCOM)){
            if (GetDayPartFromPeriod(Period).equals("d")){
                return "High";
            }else{
                return "Low";
            }
        }else{
            if (IsGoogleNWSWeather()){
                if (gWeather.getNWSForecastCondition(Period, "tempType").equals("h")){
                    return "High";
                }else{
                    return "Low";
                }
            }else{
                return "High";
            }
        }
        
    }
    public Integer GetFCPeriodCount(){
        if (IsGoogleNWSWeather()){
            return gWeather.getNWSPeriodCount();
        }else{
            if (APIType.equals(APITypes.WEATHERCOM)){
                if (FCHasTodaysHigh()){
                    return 10;
                }else{
                    return 9;
                }
            }else{
                return gWeather.getGWDayCount()*2;
            }
        }
    }
    public Object GetInstance(){
        if (APIType.equals(APITypes.WEATHERCOM)){
            return wWeather;
        }else{
            return gWeather;
        }
    }
    
    public Integer GetPeriod(Integer DayNumber, String DayPart){
        //will return -1 if the period is not valid
        //find offset by checking period 0 for "l" or "h"
        Integer DayPartOffset = 0;
        if (DayPart.equals("n")){
            DayPartOffset = 1;
        }
        //String checkPeriod = gWeather.getNWSForecastCondition(0, "tempType");
        if (FCHasTodaysHigh()){
            //LOG.debug("GetPeriod: DayNumber '" + DayNumber + "' DayPart '" + DayPart + "' Period '" + ((DayNumber * 2) + DayPartOffset) + "'");
            return (DayNumber * 2) + DayPartOffset; 
        }else{
            //LOG.debug("GetPeriod: DayNumber '" + DayNumber + "' DayPart '" + DayPart + "' Period '" + ((DayNumber * 2) + DayPartOffset -1) + "'");
            return (DayNumber * 2) + DayPartOffset - 1; 
        }
    }
    public Integer GetDayFromPeriod(Integer Period){
        if (IsGoogleNWSWeather()){
            String checkPeriod = gWeather.getNWSForecastCondition(0, "tempType");
            if (checkPeriod.equals("h")){
                return Period/2; 
            }else{
                return (Period + 1)/2; 
            }
        }else{
            if (APIType.equals(APITypes.WEATHERCOM)){
                if (FCHasTodaysHigh()){
                    return Period/2; 
                }else{
                    return (Period + 1)/2; 
                }
            }else{
                return Period/2; 
            }
        }
    }
    public String GetDayPartFromPeriod(Integer Period){
        //add 2 to the Period to take care of the fact that zero is not odd nor even but we need it to be even
        Period = Period + 2;
        if (IsGoogleNWSWeather()){
            String checkPeriod = gWeather.getNWSForecastCondition(0, "tempType");
            if (checkPeriod.equals("h")){
                //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' checkPeriod '" + checkPeriod + "'");
                if (Period%2==0){
                    //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' checkPeriod '" + checkPeriod + "' returning 'd'");
                    return "d";
                }else{
                    //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' checkPeriod '" + checkPeriod + "' returning 'n'");
                    return "n";
                }
            }else{
                //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' checkPeriod '" + checkPeriod + "'");
                if (Period%2==0){
                    //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' checkPeriod '" + checkPeriod + "' returning 'n'");
                    return "n";
                }else{
                    //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' checkPeriod '" + checkPeriod + "' returning 'd'");
                    return "d";
                }
            }
        }else{
            if (APIType.equals(APITypes.WEATHERCOM)){
                if (FCHasTodaysHigh()){
                    if (Period%2==0){
                        //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' returning 'd'");
                        return "d";
                    }else{
                        //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' returning 'n'");
                        return "n";
                    }
                }else{
                    if (Period%2==0){
                        //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' returning 'd'");
                        return "n";
                    }else{
                        //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' returning 'n'");
                        return "d";
                    }
                }
                
            }else{
                if (Period%2==0){
                    //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' returning 'd'");
                    return "d";
                }else{
                    //LOG.debug("GetDayPartFromPeriod: for Period '" + Period + "' returning 'n'");
                    return "n";
                }
            }
        }
    }
    private String ValidateDayPart(String DayPart){
        //DayPart can be d for day or n for night - default to d
        if (DayPart.startsWith("n")){
            return "n";
        }else{
            return "d";
        }
    }
    
}
