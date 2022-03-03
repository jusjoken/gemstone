/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import Gemstone.SourceUI.OrganizerType;

/**
 *
 * @author jusjoken
 * - 04/04/2012 - updated for Gemstone
 */
public class PresentationUI {
    private PresentationOrg thisGroupBy = null;
    private PresentationOrg thisSortBy = null;
    private Integer thisLevel = 0;
    //HasContent is set to true if properties are found for this UI
    private Boolean HasContent = Boolean.FALSE;

    public PresentationUI(String FlowName, Integer Level){
        this.thisLevel = Level;
        this.thisGroupBy = new PresentationOrg(FlowName,Level,OrganizerType.GROUP);
        this.thisSortBy = new PresentationOrg(FlowName,Level,OrganizerType.SORT);
        if (thisGroupBy.HasContent() || thisSortBy.HasContent()){
            HasContent = Boolean.TRUE;
        }
    }
    public Boolean HasContent(){
        return HasContent;
    }
    public PresentationOrg Group(){
        return this.thisGroupBy;
    }
    public PresentationOrg Sort(){
        return this.thisSortBy;
    }
    public Integer Level(){
        return this.thisLevel;
    }
    public String LogMessage(){
        String tMess = "Level-";
        tMess = tMess + thisLevel + "|" + thisGroupBy.LogMessage() + "|" + thisSortBy.LogMessage();
        return tMess;
    }
    
}
