/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import Gemstone.ADMutil.TriState;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author jusjoken
 */
public class PropertiesExt extends Properties {

    public PropertiesExt() {
        super();
    }
    
    //Evaluates the property and returns it's value - must be true or false - returns DefaultValue otherwise
    public Boolean GetPropertyEvalAsBoolean(String Property, Boolean DefaultValue){
        if (this.containsKey(Property)){
            String tValue = this.getProperty(Property);
            return Boolean.parseBoolean(util.EvaluateAttribute(tValue));
        }else{
            return DefaultValue;
        }
    }

    public Boolean GetPropertyAsBoolean(String Property, Boolean DefaultValue){
        if (this.containsKey(Property)){
            String tValue = this.getProperty(Property);
            return Boolean.parseBoolean(tValue);
        }else{
            return DefaultValue;
        }
    }
    
    public TriState GetPropertyAsTriState(String Property, TriState DefaultValue){
        if (this.containsKey(Property)){
            String tValue = this.getProperty(Property);
            if(tValue.equals("YES")){
                return TriState.YES;
            }else if(tValue.equals("NO")){
                return TriState.NO;
            }else if(tValue.equals("OTHER")){
                return TriState.OTHER;
            }else if(Boolean.parseBoolean(tValue)){
                return TriState.YES;
            }else if(!Boolean.parseBoolean(tValue)){
                return TriState.NO;
            }else{
                return TriState.YES;
            }
        }else{
            return DefaultValue;
        }
    }
    
    public List<String> GetPropertyAsList(String Property){
        if (this.containsKey(Property)){
            String tValue = this.getProperty(Property);
            return util.ConvertStringtoList(tValue);
        }else{
            return new LinkedList<String>();
        }
    }

    public Integer GetPropertyAsInteger(String Property, Integer DefaultValue){
        //read in the Sage Property and force convert it to an Integer
        if (this.containsKey(Property)){
            Integer tInteger = DefaultValue;
            String tValue = this.getProperty(Property);
            try {
                tInteger = Integer.valueOf(tValue);
            } catch (NumberFormatException ex) {
                //use DefaultValue
                return DefaultValue;
            }
            return tInteger;
        }else{
            return DefaultValue;
        }
    }
    
    //mimic the sage function to return a list of properties that have subproperties
    public Collection<String> GetSubpropertiesThatAreBranches(String PropertyKey){
        return GetSubpropertiesThatAreLeavesOrBranches(PropertyKey, Boolean.TRUE);
    }
    public Collection<String> GetSubpropertiesThatAreLeaves(String PropertyKey){
        return GetSubpropertiesThatAreLeavesOrBranches(PropertyKey, Boolean.FALSE);
    }
    private Collection<String> GetSubpropertiesThatAreLeavesOrBranches(String PropertyKey, Boolean Branches){
        Collection<String> ReturnList = new LinkedHashSet<String>();
        //make sure the PropertyKey ends in a "/" so we only find branches and NOT leaves
        if (!PropertyKey.endsWith("/")){
            PropertyKey = PropertyKey + "/";
        }
        for (String Key: this.stringPropertyNames()){
            if (Key.startsWith(PropertyKey)){
                Integer startindex = PropertyKey.length();
                Integer endindex = Key.indexOf("/", startindex);
                if (endindex!=-1){
                    if (Branches){
                        ReturnList.add(Key.substring(startindex, endindex));
                        //Log.debug("PropertiesExt","GetSubpropertiesThatAreBranches: FOUND Key '" + Key.substring(startindex, endindex) + "'");
                    }else{
                        //Log.debug("PropertiesExt","GetSubpropertiesThatAreBranches: SKIPPING as there are branches '" + Key + "'");
                }
                }else{
                    if (Branches){
                        //Log.debug("PropertiesExt","GetSubpropertiesThatAreBranches: SKIPPING as no branches '" + Key + "'");
                    }else{
                        ReturnList.add(Key.substring(startindex));
                        //Log.debug("PropertiesExt","GetSubpropertiesThatAreBranches: FOUND Key '" + Key.substring(startindex) + "'");
                    }
                }
            }else{
                //Log.debug("PropertiesExt","GetSubpropertiesThatAreBranches: SKIPPING Key '" + Key + "'");
            }
        }
        Log.debug("PropertiesExt","GetSubpropertiesThatAreBranches: PropertyKey '" + PropertyKey + "' found (" + ReturnList.size() + ") List '" + ReturnList + "'");
        return ReturnList;
        
    }
    public void load(String inString){
        //used to parse the input string into properties
        String lines[] = inString.split("\\r?\\n"); 
        for (String line:lines){
            if (line.startsWith("#")){
                //skip comment
                Log.debug("PropertiesExt","load: from String: skipping comment = '" + line + "'");
            }else{
                String entrys[] = line.split("=");
                String key = entrys[0];
                String value = "";
                if (entrys.length>1){
                    value = entrys[1];
                    value = StringEscapeUtils.unescapeJava(value);
                }
                this.setProperty(key, value);
                Log.debug("PropertiesExt","load: from String: key = '" + key + "' value = '" + value + "'");
            }
        }
    }
}
