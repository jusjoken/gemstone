/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author jusjoken
 * - 04/04/2012 - updated for Gemstone
 */
public class Import {
    static private final Logger LOG = Logger.getLogger(Import.class);
    private util.ExportType eType = util.ExportType.GENERIC;
    private String FilePath = "";
    private Properties Props = new Properties();
    private Boolean IsValid = Boolean.FALSE;
    private String Description = "";
    private String Name = "";
    private String DateTime = "";
    private Boolean MENUS = Boolean.FALSE;
    private Boolean FLOWS = Boolean.FALSE;
    private Boolean WIDGETS = Boolean.FALSE;
    private Boolean GENERAL = Boolean.FALSE;
    private String FLOW = "";
    
    public Import(String FilePath){
        this.FilePath = FilePath;

        Boolean KeepProcessing = Boolean.TRUE;
        if (FilePath==null){
            LOG.debug("Import: null FilePath passed.");
            KeepProcessing = Boolean.FALSE;
        }else{
            //read the properties from the properties file
            try {
                FileInputStream in = new FileInputStream(FilePath);
                try {
                    Props.load(in);
                    in.close();
                } catch (IOException ex) {
                    LOG.debug("Import: IO exception inporting properties " + util.class.getName() + ex);
                    KeepProcessing = Boolean.FALSE;
                }
            } catch (FileNotFoundException ex) {
                LOG.debug("Import: file not found inporting properties " + util.class.getName() + ex);
                KeepProcessing = Boolean.FALSE;
            }
        }
        if (KeepProcessing){
            //String tType = this.Props.getProperty(Const.ExportTypeKey, util.OptionNotFound);
            this.DateTime = this.Props.getProperty(Const.ExportDateTimeKey, "");
            
            if (!this.Props.getProperty(util.ExportType.FLOWS.toString(), util.OptionNotFound).equals(util.OptionNotFound)){
                this.FLOWS = Boolean.TRUE;
            }
            if (!this.Props.getProperty(util.ExportType.GENERAL.toString(), util.OptionNotFound).equals(util.OptionNotFound)){
                this.GENERAL = Boolean.TRUE;
            }
            if (!this.Props.getProperty(util.ExportType.MENUS.toString(), util.OptionNotFound).equals(util.OptionNotFound)){
                this.MENUS = Boolean.TRUE;
            }
            if (!this.Props.getProperty(util.ExportType.WIDGETS.toString(), util.OptionNotFound).equals(util.OptionNotFound)){
                this.WIDGETS = Boolean.TRUE;
            }
            if (!this.Props.getProperty(util.ExportType.FLOW.toString(), util.OptionNotFound).equals(util.OptionNotFound)){
                this.FLOW = this.Props.getProperty(util.ExportType.FLOW.toString(), util.OptionNotFound);
            }
            SetImportSettings();
            
            
//            if (tType.equals(util.ExportType.ALL.toString())){
//                IsValid = Boolean.TRUE;
//                eType = util.ExportType.ALL;
//                this.Name = "All Settings";
//                this.Description = "This import will overwrite ALL settings for this plugin. Use with caution.";
//            }else if (tType.equals(util.ExportType.FLOW.toString())){
//                IsValid = Boolean.TRUE;
//                eType = util.ExportType.FLOW;
//                this.Name = this.Props.getProperty(Const.ExportPropName, "Single Flow");
//                this.Description = "Import of this Flow will overwrite any existing Flow settings if the same Flow existed previously.";
//            }else if (tType.equals(util.ExportType.FLOWS.toString())){
//                IsValid = Boolean.TRUE;
//                eType = util.ExportType.FLOWS;
//                this.Name = "All Flows";
//                this.Description = "This import will overwrite and replace ALL existing Flows. Use with caution.";
//            }else if (tType.equals(util.ExportType.WIDGETS.toString())){
//                IsValid = Boolean.TRUE;
//                eType = util.ExportType.WIDGETS;
//                this.Name = "Widget Settings";
//                this.Description = "This import will overwrite and replace ALL existing Widget settings.";
//            }else if (tType.equals(util.ExportType.GENERIC.toString())){
//                IsValid = Boolean.TRUE;
//                eType = util.ExportType.GENERIC;
//                this.Name = this.Props.getProperty(Const.ExportPropName, "Generic Import");
//                this.Description = "Generic import used. No specific details are available.";
//            }else if (tType.equals(util.ExportType.MENUS.toString())){
//                IsValid = Boolean.TRUE;
//                eType = util.ExportType.MENUS;
//                this.Name = "Menus";
//                this.Description = "This import will overwrite and replace ALL menus in Menu Manager.";
//            }else{ //block this as it is not a valid import
//                if (IsOldADMExport(FilePath)){
//                    IsValid = Boolean.TRUE;
//                    eType = util.ExportType.MENUS;
//                    this.Name = "Old ADM Menus";
//                    this.Description = "This import will overwrite and replace ALL menus in Menu Manager.";
//                }else{
//                    IsValid = Boolean.FALSE;
//                    this.Name = "Invalid Import";
//                    this.Description = "This import file was not recognized so the import will not proceed.";
//                }
//            }
        }else{
            this.Name = "Failed";
            this.Description = "Import failed. Check the logs for more details.";
        }
        
    }

    private Boolean IsALL(){
        if (this.FLOWS && this.MENUS && this.WIDGETS && this.GENERAL){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    public Boolean IsFLOW() {
        if (this.FLOW.equals("")){
            return Boolean.FALSE;
        }else{
            return Boolean.TRUE;
        }
    }
    
    private void SetImportSettings(){
        String tName = "";
        if (IsALL()){
            IsValid = Boolean.TRUE;
            this.Name = "All Settings";
            this.Description = "This import will overwrite ALL settings for this plugin. Use with caution.";
        }else if(IsFLOW()){
            IsValid = Boolean.TRUE;
            this.Name = Flow.GetFlowName(this.FLOW);
            this.Description = "Import of this Flow will overwrite any existing Flow settings if the same Flow existed previously.";
        }else{
            Integer counter = 0;
            if (this.FLOWS){
                IsValid = Boolean.TRUE;
                counter++;
                tName = AppendName(tName, "FLOWS");
                this.Name = "All Flows";
                this.Description = "This import will overwrite and replace ALL existing Flows. Use with caution.";
            }
            if (this.GENERAL){
                IsValid = Boolean.TRUE;
                counter++;
                tName = AppendName(tName, "GENERAL");
            }
            if (this.MENUS){
                IsValid = Boolean.TRUE;
                counter++;
                tName = AppendName(tName, "MENUS");
                this.Name = "Menus";
                this.Description = "This import will overwrite and replace ALL menus in Menu Manager.";
            }
            if (this.WIDGETS){
                IsValid = Boolean.TRUE;
                counter++;
                tName = AppendName(tName, "WIDGETS");
                this.Name = "Widget Settings";
                this.Description = "This import will overwrite and replace ALL existing Widget settings.";
            }
            if (counter==0){
                if (IsOldADMExport(FilePath)){
                    IsValid = Boolean.TRUE;
                    this.MENUS = Boolean.TRUE;
                    //eType = util.ExportType.MENUS;
                    this.Name = "Old ADM Menus";
                    this.Description = "This import will overwrite and replace ALL menus in Menu Manager.";
                }else{
                    IsValid = Boolean.FALSE;
                    this.Name = "Invalid Import";
                    this.Description = "This import file was not recognized so the import will not proceed.";
                }
            }else if (counter>1){
                IsValid = Boolean.TRUE;
                this.Name = tName;
                this.Description = "This import will overwrite settings for multiple parts of this plugin. Use with caution.";
            }else{
                //only 1 of the areas were imported so name and description are already set above
                IsValid = Boolean.TRUE;
            }
        }
    }
    private String AppendName(String Name, String AddText){
        if (Name.equals("")){
            return AddText;
        }else{
            return Name + ", " + AddText;
        }
    }
    
    private static Boolean IsOldADMExport(String FilePath){
        //check to see if this is an old ADM menu export
        Boolean OldADMExportFound = Boolean.FALSE;
        try {
            FileInputStream in = new FileInputStream(FilePath);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
                LOG.debug("IsOldADMExport: BufferedReader '" + FilePath + "'");
                try {
                    String Line = reader.readLine();
                    LOG.debug("IsOldADMExport: reader readline '" + FilePath + "'");
                    reader.close();
                    LOG.debug("IsOldADMExport: reader close '" + FilePath + "'");
                    in.close();
                    LOG.debug("IsOldADMExport: close in '" + FilePath + "'");
                    if (Line.contains(ADMutil.ADMPropertyComment)){
                        OldADMExportFound = Boolean.TRUE;
                        LOG.debug("IsOldADMExport: old ADM Menus Export found in '" + FilePath + "'");
                    }else{
                        OldADMExportFound = Boolean.FALSE;
                        LOG.debug("IsOldADMExport: old ADM Menus Export NOT found in '" + FilePath + "'");
                    }
                } catch (IOException ex) {
                    OldADMExportFound = Boolean.FALSE;
                    LOG.debug("IsOldADMExport: IOException checking old ADM import " + util.class.getName() + ex);
                }
            } catch (UnsupportedEncodingException ex) {
                OldADMExportFound = Boolean.FALSE;
                LOG.debug("IsOldADMExport: UnsupportedEncodingException checking old ADM import " + util.class.getName() + ex);
            }

        } catch (FileNotFoundException ex) {
            LOG.debug("IsOldADMExport: file not found inporting properties " + util.class.getName() + ex);
            OldADMExportFound = Boolean.FALSE;
        }
        return OldADMExportFound;
    }
    
    public String GetName(){
        return this.Name;
    }
    public String GetFilePath(){
        return this.FilePath;
    }
    public String GetDescription(){
        return this.Description;
    }
    public String GetDateTime(){
        return this.DateTime;
    }
    public Boolean IsValid(){
        return this.IsValid;
    }
    public util.ExportType Type(){
        return this.eType;
    }
    public void Load(){
        //load the properties to the SageTV properties file or menus file
        if (this.Props.size()>0 && this.IsValid){
            //clean up existing Properties from the SageTV properties file before writing the new ones
            String tProp = this.Props.getProperty(Const.ExportPropKey,util.OptionNotFound);
            if (!tProp.equals(util.OptionNotFound)){
                if (this.eType.equals(util.ExportType.MENUS)){
                    //TODO: Import Load - need to decide if we clean old ADM/menuitems from Sage properties
                }else{
                    util.RemovePropertyAndChildren(tProp);
                    LOG.debug("Load: removing old properties '" + tProp + "'");
                }

            }
            //need a properties to store potential Menus properties
            Properties Menus = new Properties();

            for (String tPropertyKey : this.Props.stringPropertyNames()){
                if (tPropertyKey.equals(Const.ExportPropKey) || tPropertyKey.equals(Const.ExportTypeKey) || tPropertyKey.equals(Const.ExportPropName) || tPropertyKey.equals(Const.ExportDateTimeKey)){
                    LOG.debug("Load: skipping '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
                }else if(tPropertyKey.startsWith(ADMutil.SagePropertyLocation)){
                    Menus.put(tPropertyKey, this.Props.getProperty(tPropertyKey));
                    LOG.debug("Load: processing menuitem '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
                }else{
                    util.SetProperty(tPropertyKey, this.Props.getProperty(tPropertyKey));
                    LOG.debug("Load: loading '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
                }
            }
            if (Menus.size()>0){
                //handle the Menus imports differently from other imports
                //TODO: EXTERNAL MENU - call the Menu Import here from the generic import class
                LOG.debug("Load: processing menus import");
            }
        }
    }

    public void Load2(){
        //load the properties to the SageTV properties file or menus file
        if (this.Props.size()>0 && this.IsValid){
            //clean up existing Properties from the SageTV properties file before writing the new ones
            String tProp = "";
            if (IsALL()){
                tProp = Const.BaseProp;
                util.RemovePropertyAndChildren(tProp);
                LOG.debug("Load: removing old properties '" + tProp + "'");
            }else if(IsFLOW()){
                tProp = Flow.GetFlowBaseProp(this.FLOW);
                util.RemovePropertyAndChildren(tProp);
                LOG.debug("Load: removing old properties '" + tProp + "'");
            }else{
                if (this.FLOWS){
                    tProp = Const.BaseProp + Const.PropDivider + Const.FlowProp;
                    util.RemovePropertyAndChildren(tProp);
                    LOG.debug("Load: removing old properties '" + tProp + "'");
                }
                if (this.WIDGETS){
                    tProp = Const.BaseProp + Const.PropDivider + Const.WidgetProp;
                    util.RemovePropertyAndChildren(tProp);
                    LOG.debug("Load: removing old properties '" + tProp + "'");
                }

            }
            
            if (!tProp.equals(util.OptionNotFound)){
                if (this.eType.equals(util.ExportType.MENUS)){
                    //TODO: Import Load - need to decide if we clean old ADM/menuitems from Sage properties
                }else{
                    util.RemovePropertyAndChildren(tProp);
                    LOG.debug("Load: removing old properties '" + tProp + "'");
                }

            }
            //need a properties to store potential Menus properties
            Properties Menus = new Properties();

            for (String tPropertyKey : this.Props.stringPropertyNames()){
                if (tPropertyKey.equals(Const.ExportPropKey) || tPropertyKey.equals(Const.ExportTypeKey) || tPropertyKey.equals(Const.ExportPropName) || tPropertyKey.equals(Const.ExportDateTimeKey)){
                    LOG.debug("Load: skipping '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
                }else if(tPropertyKey.startsWith(ADMutil.SagePropertyLocation)){
                    Menus.put(tPropertyKey, this.Props.getProperty(tPropertyKey));
                    LOG.debug("Load: processing menuitem '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
                }else{
                    util.SetProperty(tPropertyKey, this.Props.getProperty(tPropertyKey));
                    LOG.debug("Load: loading '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
                }
            }
            if (Menus.size()>0){
                //handle the Menus imports differently from other imports
                //TODO: EXTERNAL MENU - call the Menu Import here from the generic import class
                LOG.debug("Load: processing menus import");
            }
        }
    }

    public void LoadPropBasedExport(String tProp){
        //clean up existing Properties from the SageTV properties file before writing the new ones
        util.RemovePropertyAndChildren(tProp);
        LOG.debug("Load: removing old properties '" + tProp + "'");
        //load the properties to the SageTV properties file
        for (String tPropertyKey : this.Props.stringPropertyNames()){
            if (tPropertyKey.equals(Const.ExportPropKey) || tPropertyKey.equals(Const.ExportTypeKey) || tPropertyKey.equals(Const.ExportPropName) || tPropertyKey.equals(Const.ExportDateTimeKey)){
                LOG.debug("Load: skipping '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
            }else{
                util.SetProperty(tPropertyKey, this.Props.getProperty(tPropertyKey));
                LOG.debug("Load: loading '" + tPropertyKey + "' = '" + this.Props.getProperty(tPropertyKey) + "'");
            }
        }
    }
    
}