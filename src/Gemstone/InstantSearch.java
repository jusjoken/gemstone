/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Gemstone;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.views.ViewFolder;

/**
 *
 * @author SBANTA
 * @author JUSJOKEN
 * - 10/02/2011 - added logging and minor changes
 * - 04/04/2012 - updated for Gemstone
 */
public class InstantSearch {

    private static enum TitleIgnoreTypes{ALL,THE,NONE};

    public static Object FilteredList(String SearchKeys,Object MediaFiles, Boolean IsNumericKeyListener){
        if (SearchKeys==null || SearchKeys.isEmpty()){
            Log.debug("InstantSearch","FilteredList: invalid parameters so returning full list. SearchKeys '" + SearchKeys + "'");
            return MediaFiles;
        }
        StopWatch Elapsed = new StopWatch("Flitering by '" + SearchKeys + '"');
        Elapsed.Start();
        Object[] InputMediaFiles = FanartCaching.toArray(MediaFiles);
        //Log.debug("InstantSearch","FilteredList: InputMediaFiles = '" + InputMediaFiles.length);
        Object OutputMediaFiles = null;
        if (IsNumericKeyListener){
            SearchKeys=CreateRegexFromKeypad(SearchKeys);
        }
        Pattern SearchPattern = Pattern.compile(SearchKeys);
        OutputMediaFiles = sagex.api.Database.FilterByMethodRegex(InputMediaFiles, "Gemstone_MetadataCalls_GetTitleLowerCase", SearchPattern, true, false);
        //remove these 2 lines after testing
        //Object[] Tempfiles=FanartCaching.toArray(OutputMediaFiles);
        //Log.debug("InstantSearch","FilteredList: OutputMediaFiles = '" + Tempfiles.length);
        //remove these 2 lines above after testing
        //Log.debug("InstantSearch","InstantSearch using RegEx = '" + SearchKeys + "'");
        Elapsed.StopandLog();
        if (sagex.api.Utility.Size(OutputMediaFiles)>0){
            return OutputMediaFiles;
        }
        Log.debug("InstantSearch","FilteredList: empty search result so returning full list. SearchKeys '" + SearchKeys + "'");
        return MediaFiles;
    }

    public static Object JumpToPercent(Object MediaFiles, String SearchKey, Boolean IsNumericKeyListener){
        if (MediaFiles==null){
            Log.debug("InstantSearch","JumpToPercent: null MediaFiles so returning null. SearchKey '" + SearchKey + "'");
            return null;
        }
        Object[] InputMediaFiles = FanartCaching.toArray(MediaFiles);
        if (SearchKey==null || SearchKey.isEmpty()){
            Log.debug("InstantSearch","JumpToPercent: invalid SearchKey so returning first item. SearchKey '" + SearchKey + "'");
            if (InputMediaFiles.length>0){
                Log.debug("InstantSearch","JumpToPercent: invalid SearchKey so returning first item. SearchKey '" + SearchKey + "'");
                return InputMediaFiles[0];
            }else{
                Log.debug("InstantSearch","JumpToPercent: invalid SearchKey and no items so returning null. SearchKey '" + SearchKey + "'");
                return null;
            }
        }
        Integer Element = (SearchKeyasPercent(SearchKey, IsNumericKeyListener)*InputMediaFiles.length/100)-1;
        if (Element>InputMediaFiles.length){
            Element = InputMediaFiles.length-1;
        }
        Log.debug("InstantSearch","JumpToPercent: Element '" + Element + "' SearchKey '" + SearchKey + "' Item '" + InputMediaFiles[Element] + "'");
        return InputMediaFiles[Element];
    }

    public static Object JumpToAlpha(Object MediaFiles, String SearchKey){
        if (MediaFiles==null){
            Log.debug("InstantSearch","JumpToAlpha: null MediaFiles so returning null. SearchKey '" + SearchKey + "'");
            return null;
        }
        Object[] InputMediaFiles = FanartCaching.toArray(MediaFiles);
        IMediaResource imr = Source.ConvertToIMR(InputMediaFiles[0]);
        ViewFolder view = (ViewFolder) phoenix.umb.GetParent(imr);
        Log.debug("InstantSearch","JumpToAlpha: SearchKey '" + SearchKey + "' using view '" + view + "' based on item '" + imr + "'");
        return JumpToAlpha(MediaFiles, SearchKey, view);
    }
    public static Object JumpToAlpha(Object MediaFiles, String SearchKey, ViewFolder view){
        if (MediaFiles==null){
            Log.debug("InstantSearch","JumpToAlpha: null MediaFiles so returning null. SearchKey '" + SearchKey + "'");
            return null;
        }
        Object[] InputMediaFiles = FanartCaching.toArray(MediaFiles);
        if (SearchKey==null || SearchKey.isEmpty()){
            Log.debug("InstantSearch","JumpToAlpha: invalid SearchKey so returning first item. SearchKey '" + SearchKey + "'");
            if (InputMediaFiles.length>0){
                Log.debug("InstantSearch","JumpToAlpha: invalid SearchKey so returning first item. SearchKey '" + SearchKey + "'");
                return InputMediaFiles[0];
            }else{
                Log.debug("InstantSearch","JumpToAlpha: invalid SearchKey and no items so returning null. SearchKey '" + SearchKey + "'");
                return null;
            }
        }
        TitleIgnoreTypes TitleIgnore = TitleIgnoreTypes.NONE;
        if (view.getPresentation().getSorters().get(0).getOption("ignore-the").getBoolean(false)){
            TitleIgnore = TitleIgnoreTypes.THE;
        }else if (view.getPresentation().getSorters().get(0).getOption("ignore-all").getBoolean(false)){
            TitleIgnore = TitleIgnoreTypes.ALL;
        }
        String JumpTo = SearchKey.toLowerCase().substring(0, 1);
        //Log.debug("InstantSearch","JumpToAlpha: JumpTo '" + JumpTo + "' TitleIgnoreType '" + TitleIgnore.toString() + "'");
        Object JumpToItem = InputMediaFiles[0];
        for (Object Item: InputMediaFiles){
            String firstChar = GetTitleFirstChar(Item, TitleIgnore);
            Integer Result = firstChar.compareToIgnoreCase(JumpTo);
            //Log.debug("InstantSearch","JumpToAlpha: Compare '" + JumpTo + "' firstChar '" + firstChar + "' Result '" + Result + "' Title '" + MetadataCalls.GetTitleLowerCase(Item) + "'");
            if (Result>=0){
                //found the first match so we are done
                JumpToItem = Item;
                break;
            }
        }
        //Log.debug("InstantSearch","JumpToAlpha: SearchKey '" + SearchKey + "' returning '" + JumpToItem + "'");
        return JumpToItem;
    }
    
    private static String GetTitleFirstChar(Object MediaObject, TitleIgnoreTypes TitleIgnore) {
        String tTitle = MetadataCalls.GetTitle(MediaObject).toLowerCase();
        Integer offset = 0;
        if (TitleIgnore.equals(TitleIgnoreTypes.THE)){
            if (tTitle.startsWith("the ")){
                offset = 4;
            }
        }else if (TitleIgnore.equals(TitleIgnoreTypes.ALL)){
            if (tTitle.startsWith("the ")){
                offset = 4;
            }else if (tTitle.startsWith("a ")){
                offset = 2;
            }else if (tTitle.startsWith("an ")){
                offset = 3;
            }
        }else{
            offset = 0;
        }
        if (offset>=tTitle.length()){
            offset = 0;
        }
        //Log.debug("InstantSearch","GetTitleFirstChar: offset '" + offset + "' tTitle '" + tTitle + "' returning '" + tTitle.substring(offset, offset + 1) + "'");
        return tTitle.substring(offset, offset + 1);
    }
    
    public static String SearchKeyasAlpha(String SearchKey, String LastSearchKey, Boolean IsNumericKeyListener){
        SearchKey = SearchKey.toLowerCase();
        LastSearchKey = LastSearchKey.toLowerCase();
        if (IsNumericKeyListener){
            //Log.debug("InstantSearch","SearchKeyasAlpha: SearchKey '" + SearchKey + "' LastSearchKey '" + LastSearchKey + "'");
            //convert the number to an alpha
            if (SearchKey.equals("0")){
                if (LastSearchKey.equals("z")){
                    return "0";
                }else{
                    return "z";
                }
            }else if (SearchKey.equals("1")){
                if (LastSearchKey.equals("a")){
                    return "1";
                }else{
                    return "a";
                }
            }else if (SearchKey.equals("2")){
                if (LastSearchKey.equals("a")){
                    return "b";
                }else if (LastSearchKey.equals("b")){
                    return "c";
                }else if (LastSearchKey.equals("c")){
                    return "2";
                }else{
                    return "a";
                }
            }else if (SearchKey.equals("3")){
                if (LastSearchKey.equals("d")){
                    return "e";
                }else if (LastSearchKey.equals("e")){
                    return "f";
                }else if (LastSearchKey.equals("f")){
                    return "3";
                }else{
                    return "d";
                }
            }else if (SearchKey.equals("4")){
                if (LastSearchKey.equals("g")){
                    return "h";
                }else if (LastSearchKey.equals("h")){
                    return "i";
                }else if (LastSearchKey.equals("i")){
                    return "4";
                }else{
                    return "g";
                }
            }else if (SearchKey.equals("5")){
                if (LastSearchKey.equals("j")){
                    return "k";
                }else if (LastSearchKey.equals("k")){
                    return "l";
                }else if (LastSearchKey.equals("l")){
                    return "5";
                }else{
                    return "j";
                }
            }else if (SearchKey.equals("6")){
                if (LastSearchKey.equals("m")){
                    return "n";
                }else if (LastSearchKey.equals("n")){
                    return "o";
                }else if (LastSearchKey.equals("o")){
                    return "6";
                }else{
                    return "m";
                }
            }else if (SearchKey.equals("7")){
                if (LastSearchKey.equals("p")){
                    return "q";
                }else if (LastSearchKey.equals("q")){
                    return "r";
                }else if (LastSearchKey.equals("r")){
                    return "s";
                }else if (LastSearchKey.equals("s")){
                    return "7";
                }else{
                    return "p";
                }
            }else if (SearchKey.equals("8")){
                if (LastSearchKey.equals("t")){
                    return "u";
                }else if (LastSearchKey.equals("u")){
                    return "v";
                }else if (LastSearchKey.equals("v")){
                    return "8";
                }else{
                    return "t";
                }
            }else if (SearchKey.equals("9")){
                if (LastSearchKey.equals("w")){
                    return "x";
                }else if (LastSearchKey.equals("x")){
                    return "y";
                }else if (LastSearchKey.equals("y")){
                    return "z";
                }else if (LastSearchKey.equals("z")){
                    return "9";
                }else{
                    return "w";
                }
            }else{
                return "a";
            }
        }else{
            return SearchKey;
        }
    }

    public static String SearchKeyasPercentName(String SearchKey, Boolean IsNumericKeyListener){
        return SearchKeyasPercent(SearchKey, IsNumericKeyListener).toString();
    }
    public static Integer SearchKeyasPercent(String SearchKey, Boolean IsNumericKeyListener){
        Integer Percent = 0;
        Integer Key = 10;
        String Alphabet = "abcdefghijklmnopqrstuvwxyz";
        try {
            Key = Integer.valueOf(SearchKey);
        } catch (NumberFormatException ex) {
            //use the first item by default
        }
        if (Key.equals(10)){
            if (IsNumericKeyListener){
                Percent = 0;
            }else{
                Integer Loc = Alphabet.indexOf(SearchKey.toLowerCase());
                if (Loc.equals(-1)){
                    Percent = 0;
                }else{
                    Percent = ((Loc+1)*100/26);
                }
            }
        }else if (Key.equals(0)){
            Percent = 100;
        }else{
            Percent = (Key*100/10);
        }
        return Percent;
    }

    public static String AddKey(String SearchString, String AddedString, Boolean IsNumericKeyListener){
        String NewString = "";
        if (IsNumericKeyListener){
            if (AddedString.equals("-")){
                //remove the last keypress from the string
                if (!SearchString.isEmpty()){
                    NewString = SearchString.substring(0, SearchString.length()-1);
                }
            }else{
                NewString = SearchString + AddedString;
            }
        }else{
            //add keyboard keypress
            if (AddedString.equals("Space")){
                NewString = SearchString + " ";
            }else if (AddedString.equals("Backspace")){
                //remove the last keypress from the string
                if (!SearchString.isEmpty()){
                    NewString = SearchString.substring(0, SearchString.length()-1);
                }
            }else{
                NewString = SearchString + AddedString.toLowerCase();
            }
        }
        //Log.debug("InstantSearch","AddKey: SearchString = '" + NewString + "' AddedString = '" + AddedString + "'");
        return NewString;
    }
    
    public static Boolean ValidKey(String FlowName, String KeyPress, Boolean IsNumericKeyListener){
        //see if the KeyPress is valid for the InstantSearchMode
        if (FlowName==null || KeyPress==null){
            return Boolean.FALSE;
        }
        Boolean IsValid = Boolean.FALSE;
        if (!Flow.GetTrueFalseOption(FlowName, Const.InstantSearchFilteredJumpTo, Boolean.FALSE)){ //JumpTo
            IsValid = KeyPress.matches("[A-Za-z0-9]");
        }else if (IsNumericKeyListener){
            //check numeric input
            if (KeyPress.equals("-")){
                IsValid = Boolean.TRUE;
            }else{
                IsValid = KeyPress.matches("[0-9]");
            }
        }else{
            //check keyboard input
            if (KeyPress.equals("Space")){
                IsValid = Boolean.TRUE;
            }else if (KeyPress.equals("Backspace")){
                IsValid = Boolean.TRUE;
            }else{
                IsValid = KeyPress.matches("[A-Za-z0-9]");
            }
        }
        //Log.debug("InstantSearch","ValidKey: FlowName = '" + FlowName + "' KeyPress = '" + KeyPress + "' Valid = '" + IsValid + "' NumericKeyListener = '" + IsNumericKeyListener + "'");
        return IsValid;
    }

    public static String KeypadDisplay(String InString){
        StringBuilder OutString = new StringBuilder();
        for (int i = 0; i < InString.length(); i++){
            String thisChar = keydisplay.get(InString.charAt(i));
            if (thisChar!=null){
                OutString.append(thisChar);
            }
        }
        return OutString.toString();
    }

        private static Map<Character, String> keydisplay = new HashMap<Character, String>();
        static {
                keydisplay.put('1', "(*?')");
                keydisplay.put('2', "(abc2)");
                keydisplay.put('3', "(def3)");
                keydisplay.put('4', "(ghi4)");
                keydisplay.put('5', "(jkl5)");
                keydisplay.put('6', "(mno6)");
                keydisplay.put('7', "(pqrs7)");
                keydisplay.put('8', "(tuv8)");
                keydisplay.put('9', "(wxyz9)");
                keydisplay.put('0', "( 0)");
        }    
    
    /** From Phoenix api
     * Given a keypad of numbers return a regex that can be used to find titles
     * based on the number pattern
     * 
     * @param numbers
     * @author seans
     * @return
     */
    public static String CreateRegexFromKeypad(String numbers) {
            if (numbers == null)
                    return null;
            int s = numbers.length();
            StringBuilder regex = new StringBuilder();
            for (int i = 0; i < s; i++) {
                    String reg = keyregex.get(numbers.charAt(i));
                    if (reg != null) {
                            regex.append(reg);
                    } else {
                            Log.debug("InstantSearch","CreateRegexFromKeypad: Invalid Charact for KeyPad Regex Search: " + numbers.charAt(i) + " in " + numbers);
                    }
            }
            Log.debug("InstantSearch","CreateRegexFromKeypad: KeyPad Search Regex: " + regex);
            return regex.toString();
    }
    
        private static Map<Character, String> keyregex = new HashMap<Character, String>();
        static {
                keyregex.put('1', "[\\p{Punct}1]");
                keyregex.put('2', "[abcABC2]");
                keyregex.put('3', "[defDEF3]");
                keyregex.put('4', "[ghiGHI4]");
                keyregex.put('5', "[jklJKL5]");
                keyregex.put('6', "[mnoMNO6]");
                keyregex.put('7', "[pqrsPQRS7]");
                keyregex.put('8', "[tuvTUV8]");
                keyregex.put('9', "[wxyzWXYZ9]");
                keyregex.put('0', "[ 0]");
        }    
}
