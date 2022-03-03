/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Gemstone;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SBANTA
 * @author JUSJOKEN
 * - 9/29/2011 - minor code flow updating, log message changes for testing
 * - 04/04/2012 - updated for Gemstone
 */
public class Dividers {

    public static Class SageClass = null;

    public static List<Object> AddDividers(Object[] current,String SortMethod){

        List<Object> withdividers= new ArrayList<Object>();
        Log.debug("Dividers","GettingDividerCall from sort method="+SortMethod);
        String CurrentClassCall = GetCurrentClassCall(SortMethod);
        Log.debug("Dividers","DividerCall="+CurrentClassCall);
        Object CurrDivider=0;

        for(Object curr:current){
            Log.debug("Dividers","Get current Divider");
            Object ThisDivider =ClassFromString.GetDateClass(CurrentClassCall, curr);
            Log.debug("Dividers","Checking CurrentGroup="+ThisDivider+" : Against"+CurrDivider);
            if(!CurrDivider.equals(ThisDivider)){
                Log.debug("Dividers","NewGroupFor="+ThisDivider);
                CurrDivider=ThisDivider;
                withdividers.add(CurrDivider);
            }
            withdividers.add(curr);
        }
        return withdividers;
    }

    public static boolean IsDivider(Object cell){
        return !cell.getClass().equals(SageClass);
    }

    public static String GetCurrentClassCall(String current){
        if(current.equals(SortMethods.Seasons)){
            return "GetSeasonNumberDivider";
        }
        else if(current.equals(SortMethods.EpisodeTitle)){
            return "GetEpisodeTitleDivider";
        }
        else{
            return "GetTimeAdded";
        }
     }
}
