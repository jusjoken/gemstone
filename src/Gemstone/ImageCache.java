/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gemstone;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import sagex.UIContext;
import sagex.phoenix.fanart.FanartUtil;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.metadata.IMetadata;
import sagex.phoenix.metadata.MediaArtifactType;
import sagex.phoenix.metadata.MediaType;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.views.ViewFolder;

/**
 *
 * @author jusjoken
 * - 04/04/2012 - updated for Gemstone
 */
public class ImageCache {
//    public ImageCache(){
//        
//    }
    
    //a ImageCache object needs
    // - ICache - SoftHashMap - this is the Cache itself
    //   - MinCacheItems - keep this number of items in the Cache when Memory is cleaned
    // - IQueue - LinkedList - list of MediaObject Image strings to get a specific ImageType to add to the Cache
    // - 
    //Global Functions/Variables
    // - CacheType - Background - items get added to Queue for background processing
    // - CacheType - NoQueue - items get added to the Cache and returned
    // - CacheType - Off - items get returned and NOT added to Queue NOR Cache
    // - CacheType - ByImageType - 
        
    private static final String ICacheProps = Const.BaseProp + Const.PropDivider + Const.ImageCacheProp;
    private static LinkedHashMap<String,ImageCacheKey> IQueue = new LinkedHashMap<String,ImageCacheKey>();
    private static SoftHashMap ICache = new SoftHashMap(GetMinSize());
    public static enum ImageCacheTypes{BACKGROUND,NOQUEUE,BYIMAGETYPE,OFF};
    private static final String ImageCacheTypesList = ImageCacheTypes.BACKGROUND + util.ListToken + ImageCacheTypes.NOQUEUE + util.ListToken + ImageCacheTypes.BYIMAGETYPE  + util.ListToken + ImageCacheTypes.OFF;
    private static final String ImageCacheTypesListByImageType = ImageCacheTypes.BACKGROUND + util.ListToken + ImageCacheTypes.NOQUEUE  + util.ListToken + ImageCacheTypes.OFF;
    public static final String CreateImageTag = "GemstoneImages";
    private static boolean QueueActive = false;
    
    //Initialize the Cache and the Queue
    public static void Init(){
        Log.debug("ImageCache","Init: imagecache init started: " + util.LogInfo());
        
        ICache = new SoftHashMap(GetMinSize());
        ClearQueue();
        Log.debug("ImageCache","Init: imagecache init completed: MinSize set to '" + GetMinSize() + "' " + util.LogInfo());
    }
    
    public static boolean IsQueueActive(){
        return QueueActive;
    }
    public static void SetQueueActive(boolean value){
        QueueActive = value;
    }
    
    //Clear all lists - Queue and Cache
    public static void Clear(){
        IQueue.clear();
        ICache.clear();
    }
    
    public static void ClearQueue(){
        IQueue.clear();
    }
    
    public static void RemoveItemFromCache(String Key){
        //Log.debug("ImageCache","RemoveItemFromCache: checking Queue for '" + Key + "'");
        if (IQueue.containsKey(Key)){
            //Log.debug("ImageCache","RemoveItemFromCache: removing from Queue '" + Key + "'");
            IQueue.remove(Key);
        }
        //Log.debug("ImageCache","RemoveItemFromCache: removing from Cache '" + Key + "'");
        ICache.remove(Key);
        //Object Test = ICache.get(Key);
        //Log.debug("ImageCache","RemoveItemFromCache: Test get retrieved '" + Test + "'");
    }
    
    //This will return a background and refresh that specific area
    //Check for null in the STV to not change the background if that is desired
    public static Object GetBackground(IMediaResource imediaresource, String RefreshArea){
        return GetArtifact(imediaresource, "background", RefreshArea, Boolean.FALSE);
    }
    public static Object GetBackground(IMediaResource imediaresource, String RefreshArea, Boolean originalSize){
        return GetArtifact(imediaresource, "background", RefreshArea, originalSize);
    }
    public static Object GetBackground(Object imediaresource, String RefreshArea){
        return GetBackground(Source.ConvertToIMR(imediaresource), RefreshArea, Boolean.FALSE);
    }
    public static Object GetBackground(Object imediaresource, String RefreshArea, Boolean originalSize){
        return GetBackground(Source.ConvertToIMR(imediaresource), RefreshArea, originalSize);
    }

    //This will return a poster and refresh that specific area
    //Check for null in the STV to not change the poster if that is desired
    public static Object GetPoster(IMediaResource imediaresource, String RefreshArea){
        return GetArtifact(imediaresource, "poster", RefreshArea, Boolean.FALSE);
    }
    public static Object GetPoster(IMediaResource imediaresource, String RefreshArea, Boolean originalSize){
        return GetArtifact(imediaresource, "poster", RefreshArea, originalSize);
    }
    public static Object GetPoster(Object imediaresource, String RefreshArea){
        return GetPoster(Source.ConvertToIMR(imediaresource), RefreshArea, Boolean.FALSE);
    }
    public static Object GetPoster(Object imediaresource, String RefreshArea, Boolean originalSize){
        return GetPoster(Source.ConvertToIMR(imediaresource), RefreshArea, originalSize);
    }
    //special call specifically for the Old TV Views
    public static Object GetPosterSeries(Object imediaresource, String RefreshArea){
        return GetArtifact(Source.ConvertToIMR(imediaresource), "poster", RefreshArea, Boolean.FALSE, Boolean.TRUE);
    }

    //This will return a banner and refresh that specific area
    //Check for null in the STV to not change the banner if that is desired
    public static Object GetBanner(IMediaResource imediaresource, String RefreshArea){
        return GetArtifact(imediaresource, "banner", RefreshArea, Boolean.FALSE);
    }
    public static Object GetBanner(IMediaResource imediaresource, String RefreshArea, Boolean originalSize){
        return GetArtifact(imediaresource, "banner", RefreshArea, originalSize);
    }
    public static Object GetBanner(Object imediaresource, String RefreshArea){
        return GetBanner(Source.ConvertToIMR(imediaresource), RefreshArea, Boolean.FALSE);
    }
    public static Object GetBanner(Object imediaresource, String RefreshArea, Boolean originalSize){
        return GetBanner(Source.ConvertToIMR(imediaresource), RefreshArea, originalSize);
    }
    //special call specifically for the Old TV Views
    public static Object GetBannerSeries(Object imediaresource, String RefreshArea){
        return GetArtifact(Source.ConvertToIMR(imediaresource), "banner", RefreshArea, Boolean.FALSE, Boolean.TRUE);
    }

    //used to handle a specific refresh after the image is loaded in the cache
    public static Object GetArtifact(IMediaResource imediaresource, String resourcetype, String RefreshArea, Boolean originalSize){
        return GetArtifact(imediaresource, resourcetype, RefreshArea, originalSize, Boolean.FALSE);
    }
    public static Object GetArtifact(IMediaResource imediaresource, String resourcetype, String RefreshArea, Boolean originalSize, Boolean ForceSeries){
        //return the default image passed in when none found or waiting for background processing from the queue
        Log.debug("ImageCache","GetArtifact: ***** START GetArtifact for '" + resourcetype + "' imediaresource '" + imediaresource + "' RefreshArea '" + RefreshArea + "' originalSize '" + originalSize + "'");
        if (imediaresource == null) {
            Log.debug("ImageCache","GetArtifact: imediaresource is NULL so returning NULL");
            return null;
        }

        ImageCacheKey tKey = GetImageKey(imediaresource, resourcetype, originalSize, null, ForceSeries);
        if (!tKey.IsValidKey()){
            Log.debug("ImageCache","GetArtifact: Not a valid Key so returning defaultimage '" + tKey + "'");
//            return tKey.getDefaultImage();
            return "USE:DEFALUT";
        }
        tKey.setRefreshArea(RefreshArea);
        Log.debug("ImageCache","GetArtifact: calling GetImage with tKey '" + tKey + "'");
        return GetImage(tKey);
    }
    
    //This will return an image from Cache or direct or add to the Queue depending on the settings
    public static Object GetImage(IMediaResource imediaresource, String resourcetype ){
        return GetImage(imediaresource, resourcetype, Boolean.FALSE, null);
    }
    public static Object GetImage(IMediaResource imediaresource, String resourcetype, String defaultImage){
        return GetImage(imediaresource, resourcetype, Boolean.FALSE, defaultImage);
    }
    public static Object GetImage(IMediaResource imediaresource, String resourcetype, Boolean originalSize){
        return GetImage(imediaresource, resourcetype, originalSize, null);
    }
    
    public static Object GetImage(IMediaResource imediaresource, String resourcetype, Boolean originalSize, String defaultImage){
        //return the default image passed in when none found or waiting for background processing from the queue
        //Log.debug("ImageCache","GetImage: imediaresource '" + imediaresource + "' resourcetype '" + resourcetype + "' defaultImage '" + defaultImage + "'");
        if (imediaresource == null) {
            Log.debug("ImageCache","GetImage: imediaresource is NULL so returning NULL");
            return null;
        }

        ImageCacheKey tKey = GetImageKey(imediaresource, resourcetype, originalSize, defaultImage);
        //store the mediakey to be used for refresh
        if (!tKey.IsValidKey()){
            Log.debug("ImageCache","GetImage: Not a valid Key so returning defaultimage '" + tKey + "'");
            return tKey.getDefaultImage();
        }
        tKey.setRefreshKey(GetMediaKey(imediaresource));
        return GetImage(tKey);
    }

    public static Object GetImage(ImageCacheKey Key){
        return GetImage(Key, Boolean.FALSE);
    }
    public static Object GetImage(ImageCacheKey Key, Boolean SkipQueue){
        MediaArtifactType faArtifactType = Key.getArtifactType();
        Object mediaObject = null;
        //String tImageString = Key.getImagePath();
        Object tImage = null;

        //make sure there is a valid key available
        if (Key.IsValidKey()){
            Log.debug("ImageCache","GetImage: FromKey: '" + Key + "'");
            
            //see if there is a DefaultEpisodeImage as we do not cache these so just return it
            if (Key.HasDefaultEpisodeImage()){
                Log.debug("ImageCache","GetImage: FromKey: using DefaultEpisodeImage for Key '" + Key.getKey() + "'");
                return Key.getDefaultEpisodeImage();
            }
            
            //see if we are caching or just returning an image
            if (UseCache(faArtifactType)){
                //see if the image is in the cache and if so return it
                mediaObject = ICache.get(Key.getKey());
                if (mediaObject!=null){
                    Log.debug("ImageCache","GetImage: FromKey: found Image in Cache and return it based on Key '" + Key.getKey() + "'");
                    return mediaObject;
                }else{
                    if (UseQueue(faArtifactType) && !SkipQueue){
                        Log.debug("ImageCache","GetImage: FromKey: using Queue for key '" + Key.getKey() + "'");
                        //see if the item is already in the queue
                        if (IQueue.containsKey(Key.getKey())){
                            ImageCacheKey tItem = IQueue.get(Key.getKey());
                            tItem.MergeKey(Key);
                            Log.debug("ImageCache","GetImage: FromKey: already in the Queue - merging keys '" + Key.getKey() + "' returning DefaultImage '" + Key.getDefaultImage() + "'");
//                            if (Key.HasRefreshArea()){
//                                if (Key.getRefreshArea().equals(tItem.getRefreshArea())){
//                                    Log.debug("ImageCache","GetImage: FromKey: already in the Queue '" + Key.getKey() + "' defaultImage returned '" + Key.getDefaultImage() + "' QueueSize '" + IQueue.size() + "'");
//                                }else{ //different RefreshAreas for set to RefreshAll
//                                    Log.debug("ImageCache","GetImage: FromKey: already in the Queue but different RefreshArea - RefreshAll will be used '" + Key.getKey() + "' defaultImage returned '" + Key.getDefaultImage() + "' QueueSize '" + IQueue.size() + "'");
//                                    tItem.setRefreshAll(Boolean.TRUE);
//                                }
//                            }
                            return Key.getDefaultImage();
                        }else{
                            //add the imagestring to the queue for background processing later
                            IQueue.put(Key.getKey(),Key);
                            Log.debug("ImageCache","GetImage: FromKey: adding to Queue '" + Key.getKey() + "' defaultImage returned '" + Key.getDefaultImage() + "'");
                            return Key.getDefaultImage();
                        }
                    }else{
                        //get the image and add it to the cache then return it
                        Log.debug("ImageCache","GetImage: FromKey: NOT using Queue for key '" + Key.getKey() + "'");
                        tImage = CreateImage(Key);
                        if (tImage==null){
                            Log.debug("ImageCache","GetImage: FromKey: null image returned from CreateImage '" + Key.getKey() + "'");
                        }else{
                            ICache.put(Key.getKey(), tImage);
                            Log.debug("ImageCache","GetImage: FromKey: adding to Cache '" + Key.getKey() + "'");
                        }
                        return tImage;
                    }
                }
            }else{
                //get the image and return it
                tImage = CreateImage(Key);
                if (tImage==null){
                    Log.debug("ImageCache","GetImage: FromKey: null image returned from CreateImage '" + Key.getKey() + "'");
                }else{
                    Log.debug("ImageCache","GetImage: FromKey: cache off so returning image for '" + Key.getKey() + "'");
                }
                return tImage;
            }
        }else{
            Log.debug("ImageCache","GetImage: FromKey: Key is invalid '" + Key + "' defaultImage returned '" + Key.getDefaultImage() + "'");
            return Key.getDefaultImage();
        }
    }
    
    //Convenience method that will convert the incoming object parameter to a IMediaResource type 
    public static Object GetImage(Object imediaresource, String resourcetype){
        return GetImage(imediaresource, resourcetype, Boolean.FALSE, "");
    }
    public static Object GetImage(Object imediaresource, String resourcetype, String defaultImage){
        return GetImage(imediaresource, resourcetype, Boolean.FALSE, defaultImage);
    }
    public static Object GetImage(Object imediaresource, String resourcetype, Boolean originalSize){
        return GetImage(imediaresource, resourcetype, originalSize, "");
    }
    public static Object GetImage(Object imediaresource, String resourcetype, Boolean originalSize, String defaultImage){
        return GetImage(Source.ConvertToIMR(imediaresource), resourcetype, originalSize, defaultImage);
    }

    public static IMediaResource GetChild(IMediaResource imediaresource, Boolean UseRandom){
        IMediaResource childmediaresource = null;
        ViewFolder Folder = (ViewFolder) imediaresource;
        //get a child item (if any) from the Folder
        if (phoenix.media.GetAllChildren(Folder, 1).size()>0){
            Integer Element = 0;
            if (UseRandom){
                Element = phoenix.util.GetRandomNumber(phoenix.media.GetAllChildren(Folder).size());
            }
            childmediaresource = (IMediaResource) phoenix.media.GetAllChildren(Folder).get(Element);
        }
        return childmediaresource;
    }
    
    public static ImageCacheKey GetImageKey(IMediaResource imediaresource, String resourcetype){
        return GetImageKey(imediaresource, resourcetype, Boolean.FALSE, "", Boolean.FALSE);
    }
    public static ImageCacheKey GetImageKey(IMediaResource imediaresource, String resourcetype, Boolean originalSize){
        return GetImageKey(imediaresource, resourcetype, originalSize, "", Boolean.FALSE);
    }
    public static ImageCacheKey GetImageKey(IMediaResource imediaresource, String resourcetype, Boolean originalSize, String defaultImage){
        return GetImageKey(imediaresource, resourcetype, originalSize, defaultImage, Boolean.FALSE);
    }
    public static ImageCacheKey GetImageKey(IMediaResource imediaresource, String resourcetype, Boolean originalSize, String defaultImage, Boolean ForceSeries){
        resourcetype = resourcetype.toLowerCase();
        Object tImage = null;
        Object mediaObject = null;
        String tImageString = "";
        IMediaResource childmediaresource = null;
        String Grouping = "NoGroup";
        Object faMediaObject = null;
        MediaType faMediaType = null;
        String faMediaTitle = null;
        MediaArtifactType faArtifactType = ImageCacheKey.ConvertStringtoMediaArtifactType(resourcetype);
        String faArtifiactTitle = null;
        Map<String,String> faMetadata = null;
        Object DefaultEpisodeImage = null;
        Boolean FolderBasedFanart = Boolean.FALSE;
        
        //see if this is a FOLDER item
        //we will need a MediaObject to get any fanart so get it from the passed in resource OR the child if any
        if (ForceSeries){
            faMediaObject = phoenix.media.GetMediaObject(imediaresource);
            faMetadata = Collections.emptyMap();
            faMediaType = MediaType.TV;
        }else if (phoenix.media.IsMediaType( imediaresource , "FOLDER" )){
            ViewFolder Parent = (ViewFolder) phoenix.media.GetParent(imediaresource);
            //see how the folder is grouped
            if (phoenix.umb.GetGroupers(Parent).size() > 0){
                Grouping = phoenix.umb.GetName( phoenix.umb.GetGroupers(Parent).get(0) );
                //tImageString = 
                //"genre" - get genre specific images
                //"season" - for banners or posters get Season Specific ones if available
                //"show" - get the first item in the group and use it for the image
                //else - get the first item in the group and use it for the image
            }
            if ("show".equals(Grouping)){
                //need to know if this is a TV show grouping to get a Series fanart item
                childmediaresource = GetChild(imediaresource, Boolean.FALSE);
                if (phoenix.media.IsMediaType( childmediaresource , "TV" )){
                    //Log.debug("ImageCache","GetImageKey: TV show found '" + phoenix.media.GetTitle(imediaresource) + "' using Series Fanart");
                    //use Series type fanart
                    faMediaObject = phoenix.media.GetMediaObject(childmediaresource);
                    faMetadata = Collections.emptyMap();
                    faMediaType = MediaType.TV;
                }else{
                    //Log.debug("ImageCache","GetImageKey: Other show found '" + phoenix.media.GetTitle(imediaresource) + "' using Child for Fanart");
                    //use a child for the show fanart
                    if (resourcetype.equals("background")){
                        //only for backgrounds get a random child so the backgrounds vary
                        childmediaresource = GetChild(imediaresource, Boolean.TRUE);
                    }
                    faMediaObject = phoenix.media.GetMediaObject(childmediaresource);
                }
            }else if ("genre".equals(Grouping)){
                Log.debug("ImageCache","GetImageKey: genre group found '" + phoenix.media.GetTitle(imediaresource) + "' using Child for Fanart");
                childmediaresource = GetChild(imediaresource, Boolean.FALSE);
                faMediaObject = phoenix.media.GetMediaObject(childmediaresource);
                //TODO:SPECIAL handling to get GENRE images
                //genreImage ="Themes\\Gemstone\\GenreImages\\"+phoenix_media_GetTitle(ThumbFile)+".png"
                // File[] Files = phoenix.util.GetFiles("Path", new String[] {"jpg","gif","png"}, Boolean.FALSE);
                // File[] Files = phoenix.util.GetImages("Path");
                
            }else if ("season".equals(Grouping)){
                //Log.debug("ImageCache","GetImageKey: season group found '" + phoenix.media.GetTitle(imediaresource) + "' using Child for Fanart");
                //just use a child item so you get fanart for the specific season
                if ("background".equals(resourcetype)){
                    //only for backgrounds get a random child so the backgrounds vary
                    childmediaresource = GetChild(imediaresource, Boolean.TRUE);
                }else{
                    childmediaresource = GetChild(imediaresource, Boolean.FALSE);
                }
                faMediaObject = phoenix.media.GetMediaObject(childmediaresource);
            }else if ("NoGroup".equals(Grouping)){
                Log.debug("ImageCache","GetImageKey: Folder found but no grouping for '" + phoenix.media.GetTitle(imediaresource) + "' using passed in object for Fanart");
                faMediaObject = phoenix.media.GetMediaObject(imediaresource);
            }else{
                Log.debug("ImageCache","GetImageKey: unhandled grouping found '" + Grouping + "' for Title '" + phoenix.media.GetTitle(imediaresource) + "' using Child for Fanart");
                if ("background".equals(resourcetype)){
                    //only for backgrounds get a random child so the backgrounds vary
                    childmediaresource = GetChild(imediaresource, Boolean.TRUE);
                }else{
                    childmediaresource = GetChild(imediaresource, Boolean.FALSE);
                }
                faMediaObject = phoenix.media.GetMediaObject(childmediaresource);
            }
        }else{
            //not a FOLDER
            if (phoenix.media.IsMediaType( imediaresource , "TV" )){
                //for TV items we need to get an Episode Fanart
                //the resourcetype changes to a background as poster and banner fanaet are not available
                Log.debug("ImageCache","GetImageKey: TV item. IsAiring '" + phoenix.media.IsMediaType( imediaresource , "EPG_AIRING" ) + "' IsRecording '" + phoenix.media.IsMediaType( imediaresource , "RECORDING" ) + "' for tv item '" + imediaresource + "'");
                faMediaObject = phoenix.media.GetMediaObject(imediaresource);
                if (resourcetype.equals("background") && !originalSize){//
                    //special Episode handling for backgrounds
                    if (CheckFoldersFirst(resourcetype)){
                        tImageString = GetFolderImage(faMediaObject, resourcetype);
                        if (!"".equals(tImageString)){
                            FolderBasedFanart = Boolean.TRUE;
                        }
                    }
                    if ("".equals(tImageString)){
                        //try to get Episode level fanart from phoenix first
                        tImageString = phoenix.fanart.GetEpisode(faMediaObject);
                        Log.debug("ImageCache","GetImageKey: phoenix.fanart.GetEpisode returned '" + tImageString + "' for '" + phoenix.media.GetTitle(imediaresource) + "' imediaresource [" + imediaresource + "]");
                    }
                    
                    if (tImageString==null || tImageString.equals("")){
                        //DefaultEpisodeImage = phoenix.fanart.GetDefaultEpisode(faMediaObject);
                        //Log.debug("ImageCache","GetImageKey: phoenix.fanart.GetDefaultEpisode returned '" + DefaultEpisodeImage + "' for '" + phoenix.media.GetTitle(imediaresource) + "' imediaresource [" + imediaresource + "]");
                        
                        //try to get an image from Sage functions
                        //NOTE: these Sage based images should not get cached as they are already cached in some means within Sage or are too small to bother with
                        DefaultEpisodeImage = GetDefaultThumbnail(faMediaObject);
                        Log.debug("ImageCache","GetImageKey: GetDefaultThumbnail returned '" + DefaultEpisodeImage + "' for '" + phoenix.media.GetTitle(imediaresource) + "' imediaresource [" + imediaresource + "]");
                        
                        //if we have a valid image then build an image string to be used as the key
                        if (DefaultEpisodeImage!=null){
                            //Build a imagestring that will be unique for this episode
                            tImageString = phoenix.media.GetTitle(imediaresource);
                            IMediaFile mf = phoenix.media.GetMediaFile(faMediaObject);
                            if (mf!=null){
                                IMetadata md = mf.getMetadata();
                                tImageString = tImageString + "-" + FanartUtil.EPISODE_TITLE + "-" + md.getEpisodeName();
                                if (md.getEpisodeNumber()>0) {
                                    tImageString = tImageString + "{S" + String.valueOf(md.getSeasonNumber()) + "E" + String.valueOf(md.getEpisodeNumber()) + "}";
                                }
                            }
                            Log.debug("ImageCache","GetImageKey: Derived ImageString for DefaultEpisodeImage '" + tImageString + "' for '" + phoenix.media.GetTitle(imediaresource) + "' imediaresource [" + imediaresource + "]");
                        }

                    }else{
                        Log.debug("ImageCache","GetImageKey: Episode '" + phoenix.media.GetTitle(imediaresource) + "' Fanart found '" + tImageString + "'");
                    }
                }else if ("background".equals(resourcetype) && originalSize){
                    //use SERIES level Background
                    Log.debug("ImageCache","GetImageKey: Full Size Background requested for '" + phoenix.media.GetTitle(imediaresource) + "'");
                    faMetadata = Collections.emptyMap();
                    faMediaType = MediaType.TV;
                    //try to get a full size background from phoenix fanart call
                    tImageString = phoenix.fanart.GetFanartArtifact(faMediaObject, faMediaType.toString(), faMediaTitle, faArtifactType.toString(), faArtifiactTitle, faMetadata);
                    if (tImageString==null || tImageString.equals("")){
                        Log.debug("ImageCache","GetImageKey: Full Size Background NOT FOUND. Looking for an EPISODE one for '" + phoenix.media.GetTitle(imediaresource) + "'");
                        //now try to get an episode background
                        if (CheckFoldersFirst(resourcetype)){
                            tImageString = GetFolderImage(faMediaObject, resourcetype);
                            if (!"".equals(tImageString)){
                                FolderBasedFanart = Boolean.TRUE;
                            }
                        }
                        if ("".equals(tImageString)){
                            tImageString = phoenix.fanart.GetEpisode(faMediaObject);
                        }

                        if (tImageString==null || tImageString.equals("")){
                            Log.debug("ImageCache","GetImageKey: Full Episode fanart not found for '" + phoenix.media.GetTitle(imediaresource) + "'");
                            //TODO:: the following removed as DefaultEpisode fanart is very low resolution and should not be used for full backgrounds
//                            DefaultEpisodeImage = phoenix.fanart.GetDefaultEpisode(faMediaObject);
//                            if (DefaultEpisodeImage==null){
//                                //try to get a TV Thumbnail from Sage
//                                DefaultEpisodeImage = GetDefaultThumbnail(faMediaObject);
//                            }
//                            //Build a imagestring that will be unique for this episode
//                            tImageString = phoenix.media.GetTitle(imediaresource);
//                            IMediaFile mf = phoenix.media.GetMediaFile(faMediaObject);
//                            if (mf!=null){
//                                IMetadata md = mf.getMetadata();
//                                tImageString = tImageString + "-" + FanartUtil.EPISODE_TITLE + "-" + md.getEpisodeName();
//                                if (md.getEpisodeNumber()>0) {
//                                    tImageString = tImageString + "{S" + String.valueOf(md.getSeasonNumber()) + "E" + String.valueOf(md.getEpisodeNumber()) + "}";
//                                }
//                            }

                        }else{
                            Log.debug("ImageCache","GetImageKey: Episode '" + phoenix.media.GetTitle(imediaresource) + "' Fanart found '" + tImageString + "'");
                        }
                    }
                }else{
                    Log.debug("ImageCache","GetImageKey: TV found for other than background '" + phoenix.media.GetTitle(imediaresource) + "'");
                }
            }else{
                faMediaObject = phoenix.media.GetMediaObject(imediaresource);
                //faMediaType = MediaType.MOVIE;
                Log.debug("ImageCache","GetImageKey: Title '" + phoenix.media.GetTitle(imediaresource) + "' using passed in object for Fanart");
            }
                
        }
        
        if ("".equals(tImageString)){
            String tMediaType = null;
            if (faMediaType!=null){
                tMediaType = faMediaType.toString();
            }
            if (CheckFoldersFirst(resourcetype)){
                tImageString = GetFolderImage(faMediaObject, resourcetype);
                if (!"".equals(tImageString)){
                    FolderBasedFanart = Boolean.TRUE;
                }
            }
            if ("".equals(tImageString)){
                tImageString = phoenix.fanart.GetFanartArtifact(faMediaObject, tMediaType, faMediaTitle, faArtifactType.toString(), faArtifiactTitle, faMetadata);
                Log.debug("ImageCache","GetImageKey: faMediaObject '" + faMediaObject + "' tMediaType '" + tMediaType + "' faMediaTitle '" + faMediaTitle + "' faMetadata '" + faMetadata + "' tImageString = '" + tImageString + "'");
                //removed old internal call as phoenix now handles this as of 11/4/2012
                //tImageString = GetFanartArtifact(faMediaObject, tMediaType, faMediaTitle, faArtifactType.toString(), faArtifiactTitle, faMetadata);
            }
        }
        if (tImageString==null || tImageString.equals("")){
            Log.debug("ImageCache","GetImageKey: tImageString blank or NULL so returning defaultImage");
            ImageCacheKey tICK = new ImageCacheKey();
            tICK.setDefaultImage(defaultImage);
            return tICK;
        }
        if (CheckFoldersFirst(resourcetype) && FolderBasedFanart){
            ImageCacheKey tICK = new ImageCacheKey(tImageString,originalSize,faArtifactType, Boolean.TRUE);
            tICK.setDefaultEpisodeImage(DefaultEpisodeImage);
            tICK.setDefaultImage(defaultImage);
            Log.debug("ImageCache","GetImageKey: FolderBasedFanart used - Key '" + tICK + "'");
            return tICK;
        }else{
            ImageCacheKey tICK = new ImageCacheKey(tImageString,originalSize,faArtifactType);
            tICK.setDefaultEpisodeImage(DefaultEpisodeImage);
            tICK.setDefaultImage(defaultImage);
            Log.debug("ImageCache","GetImageKey: Key '" + tICK + "'");
            return tICK;
        }
    }
    //Convenience method that will convert the incoming object parameter to a IMediaResource type 
    public static ImageCacheKey GetImageKey(Object imediaresource, String resourcetype){
        return GetImageKey(imediaresource, resourcetype, Boolean.FALSE, "");
    }
    public static ImageCacheKey GetImageKey(Object imediaresource, String resourcetype, Boolean originalSize){
        return GetImageKey(imediaresource, resourcetype, originalSize, "");
    }
    public static ImageCacheKey GetImageKey(Object imediaresource, String resourcetype, Boolean originalSize, String defaultImage){
        return GetImageKey(Source.ConvertToIMR(imediaresource), resourcetype, originalSize, defaultImage);
    }
    
    public static String GetFolderImage(Object mediaObject, String resourcetype){
        if (mediaObject==null){
            Log.debug("ImageCache","GetFolderImage: null Object passed in");
            return "";
        }
        IMediaResource imediaresource = Source.ConvertToIMR(mediaObject);
        if (imediaresource==null){
            Log.debug("ImageCache","GetFolderImage: could not convert '" + mediaObject + "' to IMediaResource");
            return "";
        }
        String FoldersFirstName = FoldersFirstName(resourcetype);
        if (FoldersFirstName.contains("*")){
            //"*" represents the media file name so swap it out in the expected folder file name
            File tFile = sagex.api.MediaFileAPI.GetFileForSegment(mediaObject,0);
            if (tFile==null){
                FoldersFirstName.replace("*", "");
                Log.debug("ImageCache","GetFolderImage: * found in '" + FoldersFirstName + " but GetFileForSegment returned null so skipping replace for '" + mediaObject + "'");
            }else{
                String tFileName = FilenameUtils.removeExtension(tFile.getName());
                FoldersFirstName = FoldersFirstName.replace("*", tFileName);
                Log.debug("ImageCache","GetFolderImage: * found and replaced - using '" + FoldersFirstName + " for '" + mediaObject + "'");
            }
        }
        File FolderImage = new File(sagex.api.MediaFileAPI.GetParentDirectory(mediaObject) + File.separator + FoldersFirstName);
        if (FolderImage==null){
            Log.debug("ImageCache","GetFolderImage: could not create a image file for '" + mediaObject + "' '" + resourcetype + "'" );
            return "";
        }
        if (!FolderImage.exists()) {
            Log.debug("ImageCache","GetFolderImage: file not found for '" + FolderImage + "'" );
            return "";
        }
        Log.debug("ImageCache","GetFolderImage: returning Folder Image '" + FolderImage + "' for '" + imediaresource.getTitle() + "' type '" + resourcetype + "'" );
        return FolderImage.toString();
    }
    
//    public static String GetKeyFromImageKey(ImageCacheKey Key){
//        return Key.getKey();
//    }
//    
    public static void GetImageFromQueue(){
        if (IQueue.size()>0){
            UIContext UIc = new UIContext(sagex.api.Global.GetUIContextName());
            String tItemKey = IQueue.entrySet().iterator().next().getKey();
            ImageCacheKey tItem = IQueue.get(tItemKey);
            IQueue.remove(tItemKey);
            String tRefresh = "";
            //get the image and add it to the cache then return it
            Object tImage = CreateImage(tItem);
            if (tImage!=null){
                tRefresh = tItem.Refresh();
                ICache.put(tItem.getKey(), tImage);
                Log.debug("ImageCache","GetImageFromQueue: remaining(" + IQueue.size() + ") Refresh '" + tRefresh + "' adding to Cache '" + tItem + "'");
            }
        }else{
            Log.debug("ImageCache","GetImageFromQueue: EMPTY QUEUE");
        }
    }

    public static Integer GetQueueSize(){
        //Log.debug("ImageCache","GetQueueSize: '" + IQueue.size() + "'");
        return IQueue.size();
    }

    public static Object CreateImage(ImageCacheKey Key){
        return CreateImage(Key, Boolean.FALSE);
    }
    public static Object CreateImage(ImageCacheKey Key, Boolean OverWrite){
        if (!Key.IsValidKey()){
            Log.debug("ImageCache","CreateImage: called with invalid Key '" + Key + "'");
            return null;
        }
        Object ThisImage = null;
        if (!OverWrite){
            //See if the image is already cached in the filesystem by a previous CreateImage call
            ThisImage = phoenix.image.GetImage(Key.getKey(), CreateImageTag);
            if (ThisImage!=null){
                Log.debug("ImageCache","CreateImage: Filesystem cached item found for Tag '" + CreateImageTag + "' ID '" + Key.getKey() + "' ThisImage = '" + ThisImage + "'");
                return ThisImage;
            }
        }
        
        //if we got this far then an OverWrite was either FORCED or the Image was not in the FileSystem Cache
        Double finalscalewidth = GetScaleWidth(Key.getArtifactType(), OverWrite);
        if (Key.HasDefaultEpisodeImage()){
            try {
                ThisImage = phoenix.image.CreateImage(Key.getKey(), CreateImageTag, Key.getDefaultEpisodeImage(), "{name: scale, width: " + finalscalewidth + ", height: -1}", true);
                Log.debug("ImageCache","CreateImage: DefaultEpisodeImage = '" + ThisImage + "' for Key '" + Key.getKey() + "'");
            } catch (Exception e) {
                Log.debug("ImageCache","CreateImage: phoenix.image.CreateImage FAILED for DefaultEpisodeImage - finalscalewidth = '" + finalscalewidth + "' for Type = '" + Key.getArtifactType().toString() + "' Image = '" + Key.getImagePath() + "' Error: '" + e + "'");
                return null;
            }
        }else{
            try {
                ThisImage = phoenix.image.CreateImage(Key.getKey(), CreateImageTag, Key.getImagePath(), "{name: scale, width: " + finalscalewidth + ", height: -1}", true);
                Log.debug("ImageCache","CreateImage: Image = '" + ThisImage + "' for Key '" + Key.getKey() + "'");
            } catch (Exception e) {
                Log.debug("ImageCache","CreateImage: phoenix.image.CreateImage FAILED - finalscalewidth = '" + finalscalewidth + "' for Type = '" + Key.getArtifactType().toString() + "' Image = '" + Key.getImagePath() + "' Error: '" + e + "'");
                return null;
            }
        }
        UIContext UIc = new UIContext(sagex.api.Global.GetUIContextName());
        if (OverWrite){
            Log.debug("ImageCache","CreateImage: Forced (OverWrite) load using LoagImage(loadImage)) - finalscalewidth = '" + finalscalewidth + "' for Type = '" + Key.getArtifactType().toString() + "' Image = '" + Key.getImagePath() + "'");
            //single LoadImage
            sagex.api.Utility.UnloadImage(UIc, ThisImage.toString());
            //sagex.api.Utility.LoadImage(UIc, ThisImage);
            //double LoadImage
            sagex.api.Utility.LoadImage(UIc, sagex.api.Utility.LoadImage(UIc, ThisImage));
        }else{
            if (!sagex.api.Utility.IsImageLoaded(UIc, ThisImage)){
                Log.debug("ImageCache","CreateImage: Loaded using LoagImage(loadImage)) - finalscalewidth = '" + finalscalewidth + "' for Type = '" + Key.getArtifactType().toString() + "' Image = '" + Key.getImagePath() + "'");
                //single LoadImage
                //sagex.api.Utility.LoadImage(UIc, ThisImage);
                //double LoadImage
                sagex.api.Utility.LoadImage(UIc, sagex.api.Utility.LoadImage(UIc, ThisImage));
            }else{
                sagex.api.Utility.UnloadImage(UIc, ThisImage.toString());
                //sagex.api.Utility.LoadImage(UIc, ThisImage);
                sagex.api.Utility.LoadImage(UIc, sagex.api.Utility.LoadImage(UIc, ThisImage));
                Log.debug("ImageCache","CreateImage: already Loaded - finalscalewidth = '" + finalscalewidth + "' for Type = '" + Key.getArtifactType().toString() + "' Image = '" + Key.getImagePath() + "'");
            }
        }
        return ThisImage;
    }
    
    //generic routine to get a default or custom scale width
    public static Double GetScaleWidth(MediaArtifactType FanartType, Boolean OriginalSize){
        //based on the ImageType determine the scalewidth to use
        Integer UIWidth = GetUIWidth();
        Double scalewidth = 1.0;
        if (OriginalSize){
            if (FanartType.equals(MediaArtifactType.BACKGROUND)){ //full background
                scalewidth = GetImageScale("fullbackground")*0.01;
            }else if (FanartType.equals(MediaArtifactType.POSTER)){ //full poster
                scalewidth = GetImageScale("fullposter")*0.01;
            }else{
                scalewidth = 1.0;
            }
        }else{
            scalewidth = GetImageScale(FanartType.toString().toLowerCase())*0.01;
        }
        Log.debug("ImageCache","GetScaleWidth: for '" + FanartType.toString() + "' OriginalSize '" + OriginalSize + "' = '" + scalewidth + "'");
        return scalewidth * UIWidth;
    }

    public static Boolean IsCheckFoldersFirstEnabled(){
        //check each type to see if at least 1 type is enabled
        if (CheckFoldersFirst(MediaArtifactType.POSTER)) return Boolean.TRUE;
        if (CheckFoldersFirst(MediaArtifactType.BANNER)) return Boolean.TRUE;
        if (CheckFoldersFirst(MediaArtifactType.BACKGROUND)) return Boolean.TRUE;
        return Boolean.FALSE;
    }
    public static String CheckFoldersFirstName(String ImageType){
        if (CheckFoldersFirst(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType))){
            return "Yes";
        }else{
            return "No";
        }
    }
    public static Boolean CheckFoldersFirst(String ImageType){
        return CheckFoldersFirst(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType));
    }
    public static Boolean CheckFoldersFirst(MediaArtifactType ImageType){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageCheckFoldersFirst + Const.PropDivider + ImageType.toString().toLowerCase();
        return util.GetPropertyAsBoolean(tProp, Boolean.FALSE);
    }
    public static void SetCheckFoldersFirstNext(String ImageType){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageCheckFoldersFirst + Const.PropDivider + ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType).toString().toLowerCase();
        util.SetTrueFalseOptionNext(tProp);
    }

    public static String FoldersFirstName(String ImageType){
        return FoldersFirstName(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType));
    }
    public static String FoldersFirstName(MediaArtifactType ImageType){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageFoldersFirstName + Const.PropDivider + ImageType.toString().toLowerCase();
        return util.GetProperty(tProp, ImageType.toString().toLowerCase() + ".jpg");
    }
    public static void SetFoldersFirstName(String ImageType, String Value){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageFoldersFirstName + Const.PropDivider + ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType).toString().toLowerCase();
        util.SetProperty(tProp, Value);
    }
    
    public static Boolean IsPreCacheEnabled(){
        //check each type to see if at least 1 type is enabled
        if (PreCache(MediaArtifactType.POSTER)) return Boolean.TRUE;
        if (PreCache(MediaArtifactType.BANNER)) return Boolean.TRUE;
        if (PreCache(MediaArtifactType.BACKGROUND)) return Boolean.TRUE;
        if (PreCacheFullBackgrounds()) return Boolean.TRUE;
        return Boolean.FALSE;
    }
    public static String PreCacheName(String ImageType){
        if (PreCache(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType))){
            return "Yes";
        }else{
            return "No";
        }
    }
    public static Boolean PreCache(String ImageType){
        return PreCache(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType));
    }
    public static Boolean PreCache(MediaArtifactType ImageType){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + ImageType.toString().toLowerCase();
        return util.GetPropertyAsBoolean(tProp, Boolean.TRUE);
    }
    public static void SetPreCacheNext(String ImageType){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType).toString().toLowerCase();
        util.SetTrueFalseOptionNext(tProp);
    }

    public static String PreCacheFullBackgroundsName(){
        if (PreCacheFullBackgrounds()){
            return "Yes";
        }else{
            return "No";
        }
    }
    public static Boolean PreCacheFullBackgrounds(){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + "FULL:BACKGROUND";
        return util.GetPropertyAsBoolean(tProp, Boolean.TRUE);
    }
    public static void SetPreCacheFullBackgroundsNext(){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + "FULL:BACKGROUND";
        util.SetTrueFalseOptionNext(tProp);
    }
    
    public static String PreCacheFullPostersName(){
        if (PreCacheFullPosters()){
            return "Yes";
        }else{
            return "No";
        }
    }
    public static Boolean PreCacheFullPosters(){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + "FULL:POSTER";
        return util.GetPropertyAsBoolean(tProp, Boolean.TRUE);
    }
    public static void SetPreCacheFullPostersNext(){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + "FULL:POSTER";
        util.SetTrueFalseOptionNext(tProp);
    }
    
    public static Integer GetImageScale(String ImageType){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageScale + Const.PropDivider + ImageType.toLowerCase();
        return util.GetPropertyAsInteger(tProp, GetImageScaleDefault(ImageType));
    }
    public static void SetImageScale(String ImageType, Integer Value){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageScale + Const.PropDivider + ImageType.toLowerCase();
        if (Value>0){
        }else{
            Value = GetImageScaleDefault(ImageType);
        }
        util.SetProperty(tProp, Value.toString());
    }

    public static Integer GetUIWidth(){
        String tProp = ICacheProps + Const.PropDivider + Const.UIWidth;
        Integer UIWidth = util.GetPropertyAsInteger(tProp, -1);
        if (UIWidth<1){
            //cleanup 0 value if needed
            if (UIWidth==0){
                SetUIWidth(0);
            }
            return sagex.api.Global.GetFullUIWidth(new UIContext(sagex.api.Global.GetUIContextName()));
        }else{
            return UIWidth;
        }
    }
    public static String GetUIWidthButtonText(){
        String tProp = ICacheProps + Const.PropDivider + Const.UIWidth;
        Integer UIWidth = util.GetPropertyAsInteger(tProp, -1);
        if (UIWidth<1){
            //not found or default so return system info as default
            return "Default (" + sagex.api.Global.GetFullUIWidth(new UIContext(sagex.api.Global.GetUIContextName())) + ")";
        }else{
            return "Override (" + UIWidth.toString() + ")";
        }
    }
    public static void SetUIWidth(Integer Value){
        String tProp = ICacheProps + Const.PropDivider + Const.UIWidth;
        if (Value>0){
        }else{
            Value = sagex.api.Global.GetFullUIWidth(new UIContext(sagex.api.Global.GetUIContextName()));
        }
        util.SetProperty(tProp, Value.toString());
    }

    public static Integer GetImageScaleDefault(String ImageType){
        ImageType = ImageType.toLowerCase();
        if (ImageType.equals("poster")){
            return 20;
        }else if (ImageType.equals("banner")){
            return 60;
        }else if (ImageType.equals("background")){
            return 40;
        }else if (ImageType.equals("fullbackground")){
            return 100;
        }else if (ImageType.equals("fullposter")){
            return 100;
        }else{
            return 100;
        }
    }
    public static void ResetImageScaleDefaults(){
        ResetImageScaleDefault("poster");
        ResetImageScaleDefault("banner");
        ResetImageScaleDefault("background");
        ResetImageScaleDefault("fullposter");
        ResetImageScaleDefault("fullbackground");
    }
    public static void ResetImageScaleDefault(String ImageType){
        ImageType = ImageType.toLowerCase();
        SetImageScale(ImageType, GetImageScaleDefault(ImageType));
    }
    
    public static Integer GetMinSize(){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageCacheMinSize;
        return util.GetPropertyAsInteger(tProp, 10);
    }
    public static void SetMinSize(Integer Value){
        String tProp = ICacheProps + Const.PropDivider + Const.ImageCacheMinSize;
        util.SetProperty(tProp, Value.toString());
    }

    public static String GetCacheType(){
        return util.GetListOptionName(Const.ImageCacheProp, Const.ImageCacheType, ImageCacheTypesList, ImageCacheTypes.BACKGROUND.toString());
    }
    public static void SetCacheTypeNext(){
        util.SetListOptionNext(Const.ImageCacheProp, Const.ImageCacheType, ImageCacheTypesList);
    }

    public static String GetCacheType(String ImageType){
        return GetCacheType(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType));
    }
    public static String GetCacheType(MediaArtifactType ImageType){
        if (GetCacheType().equals(ImageCacheTypes.BYIMAGETYPE.toString())){
            String tProp = Const.ImageCacheProp + Const.PropDivider + Const.ImageCacheType;
            return util.GetListOptionName(tProp, ImageType.toString(), ImageCacheTypesListByImageType, ImageCacheTypes.BACKGROUND.toString());
        }else{
            return GetCacheType();
        }
    }
    public static void SetCacheTypeNext(String ImageType){
        SetCacheTypeNext(ImageCacheKey.ConvertStringtoMediaArtifactType(ImageType));
    }
    public static void SetCacheTypeNext(MediaArtifactType ImageType){
        if (GetCacheType().equals(ImageCacheTypes.BYIMAGETYPE.toString())){
            String tProp = Const.ImageCacheProp + Const.PropDivider + Const.ImageCacheType;
            util.SetListOptionNext(tProp, ImageType.toString(), ImageCacheTypesListByImageType);
        }
    }

    public static Boolean IsCacheTypeByImageType(){
        if (GetCacheType().equals(ImageCacheTypes.BYIMAGETYPE.toString())){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    public static Boolean UseQueue(){
        if (GetCacheType().equals(ImageCacheTypes.BACKGROUND.toString())){
            return Boolean.TRUE;
        }else if (GetCacheType().equals(ImageCacheTypes.BYIMAGETYPE.toString())){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }
    public static Boolean UseQueue(MediaArtifactType ImageType){
        if (GetCacheType(ImageType).equals(ImageCacheTypes.BACKGROUND.toString())){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    public static Boolean UseCache(MediaArtifactType ImageType){
        if (GetCacheType(ImageType).equals(ImageCacheTypes.OFF.toString())){
            return Boolean.FALSE;
        }else{
            return Boolean.TRUE;
        }
    }
    
    //still testing if there is value add to this prefetch approach
    public static void PreFetchPosters(List<IMediaResource> Children){
        Log.debug("ImageCache","PreFetchPosters: Started '" + Children + "'");
        for (IMediaResource Child: Children){
            Log.debug("ImageCache","PreFetchPosters: processing Child '" + Child + "'");
            GetImage(Child, "poster");
        }
    }
    
    public static void BuildFileSystemCache(){
        ClearPreCacheInfo();
        if (IsPreCacheEnabled()){
            SetPreCacheRunning(Boolean.TRUE);
            ViewFolder view = phoenix.umb.CreateView("gemstone.base.allforcache");
            SetPreCacheItems(phoenix.umb.GetChildCount(view));
            Log.debug("ImageCache","BuildFileSystemCache: Started - Items '" + GetPreCacheItems() + "' for view '" + view + "'");
            Integer counter = 0;
            for (IMediaResource MediaItem: view){
                IMediaResource MediaItemChild = MediaItem;
                if (phoenix.media.IsMediaType( MediaItem , "FOLDER" )){
                    MediaItemChild = GetChild(MediaItem, Boolean.FALSE);
                }
                counter ++;
//                if (counter>50){
//                    break;
//                }
                SetPreCacheItem(counter);
                SetPreCacheItemTitle(MediaItemChild.getTitle());
                FanartManager faManager = new FanartManager(MediaItemChild);
                Log.debug("ImageCache","BuildFileSystemCache: processing Child (" + counter + ") '" + MediaItemChild + "'");
                faManager.CacheEachFanartItem();
            }
            SetPreCacheRunning(Boolean.FALSE);
        }else{
            Log.debug("ImageCache","BuildFileSystemCache: PreCache is not enabled - processing not started");
        }
    }

    public static void ClearFileSystemCache(){
        File CacheLoc = ImageUtil.getImageCacheDir();
        File GemCache = new File(CacheLoc, CreateImageTag);
        File[] files = GemCache.listFiles();
        Integer counterYes = 0;
        Integer counterNo = 0;
        for (File file : files){
            if (!file.delete()){
                Log.debug("ImageCache","ClearFileSystemCache: failed to delete '" + file + "'");
                counterNo++;
            }else{
                counterYes++;
            }
        }
        Log.debug("ImageCache","ClearFileSystemCache: deleted " + counterYes + " of " + files.length + " files from: '" + GemCache.getPath());
        if (counterNo>0){
            Log.debug("ImageCache","ClearFileSystemCache: FAILED to delete " + counterNo + " of " + files.length + " files from: '" + GemCache.getPath());
        }
    }

    private static Integer PreCacheItem = 0;
    private static Integer PreCacheItems = 0;
    public static Integer PreCachePosterItems = 0;
    public static Integer PreCacheBannerItems = 0;
    public static Integer PreCacheBackgroundItems = 0;
    public static Integer PreCacheFullBackgroundItems = 0;
    public static Integer PreCacheFullPosterItems = 0;
    public static Integer PreCacheItemsCreated = 0;
    public static Integer PreCacheItemsExisted = 0;
    public static Integer PreCacheItemsFailed = 0;
    private static String PreCacheItemInfo = "";
    private static String PreCacheItemTitle = "";
    private static String PreCacheItemType = "";
    private static String PreCacheItemLocation = "";
    public static void ClearPreCacheInfo(){
        SetPreCacheRunning(Boolean.TRUE);
        SetPreCacheItem(0);
        SetPreCacheItemInfo("");
        SetPreCacheItemLocation("");
        SetPreCacheItemTitle("");
        SetPreCacheItemType("");
        SetPreCacheItems(0);
        PreCachePosterItems = 0;
        PreCacheBannerItems = 0;
        PreCacheBackgroundItems = 0;
        PreCacheFullBackgroundItems = 0;
        PreCacheFullPosterItems = 0;
        PreCacheItemsCreated = 0;
        PreCacheItemsExisted = 0;
        PreCacheItemsFailed = 0;
    }
    
    public static Boolean IsPreCacheRunning(){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + Const.ImagePreCacheRunning;
        return util.GetPropertyAsBoolean(tProp, Boolean.FALSE);
    }
    public static void SetPreCacheRunning(Boolean Value){
        String tProp = ICacheProps + Const.PropDivider + Const.ImagePreCache + Const.PropDivider + Const.ImagePreCacheRunning;
        util.SetProperty(tProp, Value.toString());
    }
    public static Integer GetPreCacheItem(){
        return PreCacheItem;
    }
    public static void SetPreCacheItem(Integer Value){
        PreCacheItem = Value;
    }
    
    public static Integer GetPreCacheItems(){
        return PreCacheItems;
    }
    public static void SetPreCacheItems(Integer Value){
        PreCacheItems = Value;
    }
    
    public static String GetPreCacheItemTitle(){
        return PreCacheItemTitle;
    }
    public static void SetPreCacheItemTitle(String Value){
        PreCacheItemTitle = Value;
    }

    public static String GetPreCacheItemType(){
        return PreCacheItemType;
    }
    public static void SetPreCacheItemType(String Value){
        PreCacheItemType = Value;
    }
    public static String GetPreCacheItemLocation(){
        return PreCacheItemLocation;
    }
    public static void SetPreCacheItemLocation(String Value){
        PreCacheItemLocation = Value;
    }
    public static String GetPreCacheItemInfo(){
        return PreCacheItemInfo;
    }
    public static void SetPreCacheItemInfo(String Value){
        PreCacheItemInfo = Value;
    }

    public static String GetPreCacheItemsProcessed(){
        return "Final: " + PreCacheItemsCreated + "/" + PreCacheItemsExisted + "/" + PreCacheItemsFailed + "/" + (PreCacheItemsCreated+PreCacheItemsExisted+PreCacheItemsFailed) + " (Created/Skipped/Failed/Total)";
    }
    public static String GetPreCachePosterItems(){
        if (PreCache(MediaArtifactType.POSTER)){
            return util.intToString(PreCachePosterItems,4) + " Posters";
        }else{
            return "Poster " + Const.OptionNotEnabled;
        }
    }
    public static String GetPreCacheBannerItems(){
        if (PreCache(MediaArtifactType.BANNER)){
            return util.intToString(PreCacheBannerItems,4) + " Banners";
        }else{
            return "Banner " + Const.OptionNotEnabled;
        }
    }
    public static String GetPreCacheBackgroundItems(){
        if (PreCache(MediaArtifactType.BACKGROUND)){
            return util.intToString(PreCacheBackgroundItems,4) + " Backgrounds";
        }else{
            return "Background " + Const.OptionNotEnabled;
        }
    }
    public static String GetPreCacheFullBackgroundItems(){
        if (PreCacheFullBackgrounds()){
            return util.intToString(PreCacheFullBackgroundItems,4) + " Full Backgrounds";
        }else{
            return "Full Background " + Const.OptionNotEnabled;
        }
    }

    public static String GetPreCacheFullPosterItems(){
        if (PreCacheFullPosters()){
            return util.intToString(PreCacheFullPosterItems,4) + " Full Posters";
        }else{
            return "Full Poster " + Const.OptionNotEnabled;
        }
    }
    
    //TODO:: no longer used from STV... can likely remove
    public static Object GetTVThumbnail(Object MediaFile, Boolean UseBackNotThumb){
        UIContext uIContext = new UIContext(sagex.api.Global.GetUIContextName());
        Object FinalThumb = null;
        //try to see if we can get a phoenix image first
        FinalThumb = GetImage(GetImageKey(MediaFile,"background"),Boolean.TRUE);
        if (FinalThumb!=null){
            Log.debug("ImageCache","GetTVThumbnail: Using Gemstone GetImage");
            return FinalThumb;
        }
        
        if(MediaFile==null){
            //find some sort of image to display
            Log.debug("ImageCache","GetTVThumbnail: null MediaFile");
        }else{
            //check if Sage has a Thumb for this MediaFile - xShowImage
            Boolean ImageFound = Boolean.FALSE;
            String[] ImageTypeList = {"PosterWide", "PosterTall", "PhotoWide", "PhotoTall"};
            for (String ImageType:ImageTypeList){
                if (sagex.api.ShowAPI.GetShowImageCount( uIContext, MediaFile, ImageType )>0){
                    FinalThumb = sagex.api.ShowAPI.GetShowImage( uIContext, MediaFile, ImageType, 0, 2 );
                    Log.debug("ImageCache","GetTVThumbnail: Using ShowAPI.GetShowImage");
                    ImageFound = Boolean.TRUE;
                    break;
                }
            }
            if (!ImageFound){
                //No Zap2it-provided show images; try thumbnail
                if (sagex.api.MediaFileAPI.HasAnyThumbnail(uIContext,MediaFile)){
                    //xNormal
                    if (MetadataCalls.IsMediaTypeTV(MediaFile)  &&  sagex.api.ShowAPI.GetShowCategory(uIContext,MediaFile).indexOf("Movie") == -1){
                        //Check if we want to display Backgrounds rather than Thumbs
                        if (UseBackNotThumb){
                            FinalThumb = GetImage(MediaFile,"background");
                            Log.debug("ImageCache","GetTVThumbnail: trying Gemstone backround");
                        }else{
                            FinalThumb = sagex.api.MediaFileAPI.GetThumbnail(uIContext,MediaFile);
                            Log.debug("ImageCache","GetTVThumbnail: Using MediaFileAPI.GetThumbnail");
                        }
                    }
                }else{
                    //try Series
                    Object SeriesInfo = sagex.api.ShowAPI.GetShowSeriesInfo(uIContext,MediaFile);
                    if (sagex.api.SeriesInfoAPI.HasSeriesImage(uIContext,SeriesInfo)){
                        //xSeriesInfo
                        FinalThumb = sagex.api.SeriesInfoAPI.GetSeriesImage(uIContext,MediaFile);
                        Log.debug("ImageCache","GetTVThumbnail: Using SeriesInfoAPI.GetSeriesImage");
                    }else{
                        //try Channel Logo
                        FinalThumb = sagex.api.ChannelAPI.GetChannelLogo( uIContext, MediaFile, "Large", 1, true );
                        Log.debug("ImageCache","GetTVThumbnail: Using ChannelAPI.GetChannelLogo");
                    }
                            
                }
            }
        }
        if (FinalThumb==null){
            //last try to get an image
            if (MediaFile!=null){
                FinalThumb = GetImage(MediaFile,"backbround");
                Log.debug("ImageCache","GetTVThumbnail: Trying Gemstone fanart as last resort");
            }
        }
        return FinalThumb;
    }

    public static Object GetDefaultThumbnail(Object MediaFile){
        UIContext uIContext = new UIContext(sagex.api.Global.GetUIContextName());
        Object FinalThumb = null;
        if(MediaFile==null){
            //find some sort of image to display
            Log.debug("ImageCache","GetDefaultThumbnail: null MediaFile");
        }else{
            //check if Sage has a Thumb for this MediaFile - xShowImage
            Boolean ImageFound = Boolean.FALSE;
            String[] ImageTypeList = {"PosterWide", "PosterTall", "PhotoWide", "PhotoTall"};
            for (String ImageType:ImageTypeList){
                if (sagex.api.ShowAPI.GetShowImageCount( uIContext, MediaFile, ImageType )>0){
                    FinalThumb = sagex.api.ShowAPI.GetShowImage( uIContext, MediaFile, ImageType, 0, 2 );
                    if (FinalThumb!=null){
                        Log.debug("ImageCache","GetDefaultThumbnail: Using ShowAPI.GetShowImage. Type '" + ImageType + "' returning [" + FinalThumb + "]");
                        ImageFound = Boolean.TRUE;
                        break;
                    }
                }
            }
            if (!ImageFound){
                //No Zap2it-provided show images; try thumbnail
                if (sagex.api.MediaFileAPI.HasAnyThumbnail(uIContext,MediaFile)){
                    if (MetadataCalls.IsMediaTypeTV(MediaFile)  &&  sagex.api.ShowAPI.GetShowCategory(uIContext,MediaFile).indexOf("Movie") == -1){
                        FinalThumb = sagex.api.MediaFileAPI.GetThumbnail(uIContext,MediaFile);
                        if (FinalThumb!=null){
                            ImageFound = Boolean.TRUE;
                            Log.debug("ImageCache","GetDefaultThumbnail: Using MediaFileAPI.GetThumbnail for TV returning [" + FinalThumb + "]");
                        }
                    }else{
                        //this is likely a movie so we need to return something
                        FinalThumb = sagex.api.MediaFileAPI.GetThumbnail(uIContext,MediaFile);
                        if (FinalThumb!=null){
                            ImageFound = Boolean.TRUE;
                            Log.debug("ImageCache","GetDefaultThumbnail: Using MediaFileAPI.GetThumbnail for TV Movie returning [" + FinalThumb + "]");
                        }
                    }
                }
            }
            if (!ImageFound){
                //try Series
                Object SeriesInfo = sagex.api.ShowAPI.GetShowSeriesInfo(uIContext,MediaFile);
                if (SeriesInfo!=null){
                    if (sagex.api.SeriesInfoAPI.HasSeriesImage(uIContext,SeriesInfo)){
                        //xSeriesInfo
                        FinalThumb = sagex.api.SeriesInfoAPI.GetSeriesImage(uIContext,MediaFile);
                        if (FinalThumb!=null){
                            ImageFound = Boolean.TRUE;
                            Log.debug("ImageCache","GetDefaultThumbnail: Using SeriesInfoAPI.GetSeriesImage returning [" + FinalThumb + "]");
                        }
                    }
                }
            }
            if (!ImageFound){
                //try Channel Logo
                FinalThumb = sagex.api.ChannelAPI.GetChannelLogo( uIContext, MediaFile, "Large", 1, true );
                if (FinalThumb==null){
                    Log.debug("ImageCache","GetDefaultThumbnail: No valid images found - returning null");
                }else{
                    ImageFound = Boolean.TRUE;
                    Log.debug("ImageCache","GetDefaultThumbnail: Using ChannelAPI.GetChannelLogo returning [" + FinalThumb + "]");
                }
            }
        }
        return FinalThumb;
    }

    //phoenix core now handles so this was removed 11/04/2012
//    public static String GetDefaultArtifact(Object imediaresource, String resourcetype, Map<String, String> metadata){
//        return GetDefaultArtifact(Source.ConvertToIMR(imediaresource), resourcetype, metadata);
//    }
    //phoenix core now handles so this was removed 11/04/2012
//    public static String GetDefaultArtifact(IMediaResource imediaresource, String resourcetype, Map<String, String> metadata){
//        if (imediaresource==null){
//            Log.debug("ImageCache","GetDefaultArtifact: null resource passed in - returning null");
//            return null;
//        }
//        IMediaFile mf = phoenix.media.GetMediaFile(imediaresource.getMediaObject());
//        if (mf==null){
//            Log.debug("ImageCache","GetDefaultArtifact: resource could not be converted to a MediaFile '" + imediaresource + "'");
//            return null;
//        }
//        Object tArtifact = getDefaultArtifact(mf, ImageCacheKey.ConvertStringtoMediaArtifactType(resourcetype),metadata);
//        if (tArtifact==null){
//            return null;
//        }else{
//            //Log.debug("ImageCache","GetDefaultArtifact: returned '" + tArtifact.toString() + "'");
//            return tArtifact.toString();
//        }
//    }
    
    //phoenix core now handles so this was removed 11/04/2012
//    //phoenix does not expose this as public so recreate this here
//    private static final String STORE_SERIES_FANART = "phoenix.seriesfanart";
//    private static final String STORE_SEASON_FANART = "phoenix.seasonfanart";
    //phoenix core now handles so this was removed 11/04/2012
//    private static File getDefaultArtifact(IMediaFile file, MediaArtifactType artifactType, Map<String, String> metadata) {
//
//        //Log.debug("ImageCache","getDefaultArtifact: file '" + file + "' artifactType '" + artifactType + "'");
//        if (file==null||artifactType==null){
//            Log.debug("ImageCache","getDefaultArtifact: called with null items");
//            return null;
//        }
//
//        String key = null;
//        if (artifactType == MediaArtifactType.POSTER) {
//                key=ISageCustomMetadataRW.FieldName.DEFAULT_POSTER;
//        } else if (artifactType == MediaArtifactType.BACKGROUND) {
//                key=ISageCustomMetadataRW.FieldName.DEFAULT_BACKGROUND;
//        } else if (artifactType == MediaArtifactType.BANNER) {
//                key=ISageCustomMetadataRW.FieldName.DEFAULT_BANNER;
//        }
//
//        String def = MediaFileAPI.GetMediaFileMetadata(file.getMediaObject(), key);
//        //Log.debug("ImageCache","getDefaultArtifact: based on GetMediaFileMetadata - key '" + key + "' def '" + def + "'");
//        if (def.isEmpty() && file.isType(MediaResourceType.TV.value())) {
//            //see if this TV item is a SERIES or a SEASON based on the metadata
//            metadata = resolveFanartMetadata(metadata, "tv", file.getMediaObject());
//            String title = resolveMediaTitle(file.getTitle(), file);
//            if (metadata!=null && metadata.containsKey(FanartUtil.SEASON_NUMBER)){
//                // defaults for TV shows need to be stored against the seriesname plus the SEASON number
//                String SeasonNumber = metadata.get(FanartUtil.SEASON_NUMBER);
//                String SeasonTitle = resolveMediaSeasonTitle(title, SeasonNumber);
//                def = UserRecordUtil.getField(STORE_SEASON_FANART, SeasonTitle, artifactType.name());
//                //Log.debug("ImageCache","getDefaultArtifact: testing for TV SEASON for '" + SeasonTitle + "' artifactType.name() '" + artifactType.name() + "' def '" + def + "'");
//            }else{
//                // defaults for TV shows need to be stored against the seriesname
//                def = UserRecordUtil.getField(STORE_SERIES_FANART, title, artifactType.name());
//                //Log.debug("ImageCache","getDefaultArtifact: testing for TV SERIES for '" + title + "' artifactType.name() '" + artifactType.name() + "' def '" + def + "'");
//            }
//        }
//
//        if (def !=null && !def.isEmpty()) {
//                File f = null;
//                String central = null;
//                try {
//                    central = (new File(phoenix.fanart.GetFanartCentralFolder())).getCanonicalPath();
//                } catch (IOException ex) {
//                    java.util.logging.Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                //Log.debug("ImageCache","getDefaultArtifact: central '" + central + "' def '" + def + "'");
//                if (central!=null) {
//                    if (def.startsWith(central)) {
//                        f = new File(def);
//                        //Log.debug("ImageCache","getDefaultArtifact: starts with central '" + central + "' file '" + f + "'");
//                    }else{
//                        //f = new File(phoenix.fanart.GetFanartCentralFolder(), def);
//                        f = new File(central, def);
//                        //Log.debug("ImageCache","getDefaultArtifact: not start with central '" + central + "' file '" + f + "'");
//                    }
//                } else {
//                    f = new File(def);
//                    //Log.debug("ImageCache","getDefaultArtifact: not central - file '" + f + "'");
//                }
//
//                if (f.exists() && f.isFile()) {
//                    return f;
//                }
//        }
//        return null;
//    }

    //phoenix core now handles so this was removed 11/04/2012
//    public static String resolveMediaSeasonTitle(String mediaTitle, String SeasonNumber) {
//        return mediaTitle + "-" + FanartUtil.SEASON_NUMBER + "-" + SeasonNumber;
//    }
    
    //phoenix core now handles so this was removed 11/04/2012
//    public static String resolveMediaTitle(String mediaTitle, IMediaFile mf) {
//        if (mf==null) return mediaTitle;
//        if (!mediaTitle.isEmpty()) return mediaTitle;
//
//        // check for music
//        if (mf.isType(MediaResourceType.MUSIC.value())) {
//                IAlbumInfo info = mf.getAlbumInfo();
//                if (info!=null) {
//                        mediaTitle = info.getArtist();
//                }
//                if (!mediaTitle.isEmpty()) return mediaTitle;
//        }
//
//        IMetadata md = mf.getMetadata();
//        if (md != null) {
//                mediaTitle = md.getMediaTitle();
//                if (mediaTitle.isEmpty()) mediaTitle=null;
//        }
//
//        return Utils.returnNonNull(mediaTitle, mf.getTitle());
//    }

    //phoenix core now handles so this was removed 11/04/2012
//    public static String GetFanartArtifact(Object mediaObject, String mediaType, String mediaTitle, String artifactType, String artifactTitle,	Map<String, String> metadata) {
//        //check if the Metadata has SERIES/SEASON specific data and handle differently
//        Boolean IsTV = Boolean.FALSE;
//        Map<String, String> SeasonMetadata = resolveFanartMetadata(metadata, mediaType, mediaObject);
//        if (SeasonMetadata!=null){
//            IsTV = Boolean.TRUE;
//        }
//        
//        if (IsTV){
//            //check the default first and return it if any
//            String Default = GetDefaultArtifact(mediaObject, artifactType, metadata);
//            //if no default then get the first SEASON/SERIES specific Fanart 
//            //  - skipping the phoenix call as it will get a SERIES default if one exists
//            if (Default==null || Default.isEmpty()){
//                // grab first fanart artifact
//                //Log.debug("ImageCache","GetFanartArtifact: no default found so getting the first Season/Series based fanart item");
//                String files[] = phoenix.fanart.GetFanartArtifacts(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
//                if (files!=null && files.length>0) {
//                    // just use the first one
//                    Default = files[0];
//                }                
//                //Log.debug("ImageCache","GetFanartArtifact: returning first Season/Series fanart item '" + Default + "'");
//                return Default;
//            }else{
//                //Log.debug("ImageCache","GetFanartArtifact: returning Default '" + Default + "'");
//                return Default;
//            }
//        }else{
//            //Log.debug("ImageCache","GetFanartArtifact: using phoenix GetFanartArtifact - mediaObject '" + mediaObject + "' mediaType '" + mediaType + "' mediaTitle '" + mediaTitle + "' artifactType '" + artifactType + "' artifactTitle '" + artifactTitle + "' metadata '" + metadata + "'");
//            return phoenix.fanart.GetFanartArtifact(mediaObject, mediaType, mediaTitle, artifactType, artifactTitle, metadata);
//        }
//    }

    //phoenix core now handles so this was removed 11/04/2012
//    private static Map<String, String> resolveFanartMetadata(Map<String, String> metaadata, String mediaType, Object mediaObject) {
//        //this will either return the existing metadata or build the metadata from the mediafile
//
//        // if we are given a metadata map, then use use it, even if it's empty.
//        // this allows us to bypass the season specific fanart by passing in an empty metadata map
//        if (metaadata != null) return metaadata;
//
//        //if we don't have a mediaObject then there is nothing more we can do
//        if (mediaObject==null){
//            return null;
//        }
//        
//        //now based on the mediaObject see if we can determine SEASON based metadata for TV
//        IMediaFile mf = null;
//        if (mediaObject!=null){
//            mf = phoenix.media.GetMediaFile(mediaObject);
//            if (mf!=null){
//                if (phoenix.media.IsMediaType( mf.getMediaObject() , "TV" )) {
//                    IMetadata md = mf.getMetadata();
//                    Map<String, String> props = new HashMap<String, String>();
//                    if (md.getEpisodeNumber()>0) {
//                        props.put(FanartUtil.SEASON_NUMBER, String.valueOf(md.getSeasonNumber()));
//                        props.put(FanartUtil.EPISODE_NUMBER, String.valueOf(md.getEpisodeNumber()));
//                    }
//                    return props;
//                }
//            }
//        }
//        return null;
//    }

    //phoenix core now handles so this was removed 11/04/2012
//    //override of the phoenix method to handle TV Seasons - if not, call phoenix for others
//    public static void SetFanartArtifact(Object mediaObject,
//			File fanart,
//			MediaType mediaType,
//			String mediaTitle,
//			String artifactType,
//			String artifactTitle,
//			Map<String, String> metadata) {
//        //check if the Metadata has SEASON specific data and handle differently
//        Boolean IsTVSeason = Boolean.FALSE;
//        Map<String, String> SeasonMetadata = resolveFanartMetadata(metadata, mediaType.toString(), mediaObject);
//        if (SeasonMetadata!=null){
//            if (SeasonMetadata.containsKey(FanartUtil.SEASON_NUMBER)){
//                IsTVSeason = Boolean.TRUE;
//            }
//        }
//        //check if this is a SEASON item and then handle as special
//        if (IsTVSeason){
//            //special handling for SEASON Defaults
//            IMediaFile mf = phoenix.media.GetMediaFile(mediaObject);
//            String title = resolveMediaTitle(mf.getTitle(), mf);
//            String SeasonNumber = metadata.get(FanartUtil.SEASON_NUMBER);
//            String SeasonTitle = resolveMediaSeasonTitle(title, SeasonNumber);
//            Log.debug("ImageCache","SetFanartArtifact: using special TV SEASON logic for '" + SeasonTitle + "'");
//            String file = null;
//            try {
//                file = fanart.getCanonicalPath();
//            } catch (IOException ex) {
//                java.util.logging.Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            if (file!=null){
//                //Log.debug("ImageCache","SetFanartArtifact: calling UserRecordUtil with - SeasonTitle '" + SeasonTitle + "' artifactType '" + artifactType + "' file '" + file + "'");
//                UserRecordUtil.setField(STORE_SEASON_FANART, SeasonTitle, artifactType.toUpperCase(), file);
//            }
//        }else{
//            Log.debug("ImageCache","SetFanartArtifact: using - phoenix.fanart.SetFanartArtifact");
//            phoenix.fanart.SetFanartArtifact(mediaObject, fanart, mediaType.toString(), mediaTitle, artifactType, artifactTitle, metadata);
//        }
//        
//    }

//    //TODO: only used for testing purposes
//    public static void PrintUserRecord(String Title){
//        Object series = UserRecordAPI.GetUserRecord(STORE_SERIES_FANART, Title);
//        Object season = UserRecordAPI.GetUserRecord(STORE_SEASON_FANART, Title);
//        Log.debug("ImageCache","GetUserRecord: '" + Title + "' SERIES '" + series + "'");
//        Log.debug("ImageCache","GetUserRecord: '" + Title + "' SEASON '" + season + "'");
//
//    }

    //MediaKey used to match to a media item in the STV and Refresh a variable called MediaKey
    public static String GetMediaKey(Object imediaresource){
        return GetMediaKey(Source.ConvertToIMR(imediaresource));
    }
    public static String GetMediaKey(IMediaResource imediaresource){
        if (imediaresource==null){
            return "InvalidMediaItem";
        }
        IMediaResource kMediaresource = imediaresource;
        String kTitle = imediaresource.getTitle();
        String kType = "ITEM";
        String kMediaType = "MEDIA:TV";
        if (phoenix.media.IsMediaType( imediaresource , "FOLDER" )){
            kType = "FOLDER";
            kMediaresource = GetChild(imediaresource, Boolean.FALSE);
            kTitle = kMediaresource.getTitle();
            if (phoenix.media.IsMediaType( kMediaresource , "TV" )){
                kMediaType = "MEDIA:TV";
            }else{
                kMediaType = "MEDIA:OTHER";
            }
        }else{
            kType = "ITEM";
            if (phoenix.media.IsMediaType( imediaresource , "TV" )){
                kMediaType = "MEDIA:TV";
            }else{
                kMediaType = "MEDIA:OTHER";
            }
        }
        String tKey = "MEDIAKEY{" + kTitle + ":" + kType + ":" + kMediaType + "}";
        //Log.debug("ImageCache","GetMediaKey: '" + tKey + "'");
        return tKey;
    }
    
    public static String GetFanartKey(String FanartPath, Boolean OriginalSize){
        return GetFanartKey(FanartPath, OriginalSize, Boolean.FALSE);
    }
    public static String GetFanartKey(String FanartPath, Boolean OriginalSize, Boolean KeepFileName){
        String Key = null;
        String Size = "";
        if (OriginalSize){
            Size = "FULL";
        }else{
            Size = "PART";
        }
        //handle the special Key for DefaultEpisodeImages
        if (FanartPath.contains(FanartUtil.EPISODE_TITLE)){
            Key = FanartPath;
            //Log.debug("ImageCache","GetFanartKey: special key for DefaultEpisodeImages '" + Key + "' for FanartPath '" + FanartPath + "'");
        }else{
            File f = null;
            String central = null;
            try {
                central = (new File(phoenix.fanart.GetFanartCentralFolder())).getCanonicalPath();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Log.debug("ImageCache","GetFanartKey: central '" + central + "' FanartPath '" + FanartPath + "'");
            if (central!=null) {
                if (FanartPath.startsWith(central)) {
                    f = new File(FanartPath);
                    //Log.debug("ImageCache","GetFanartKey: starts with central - f = '" + f + "'");
                }else{
                    f = new File(phoenix.fanart.GetFanartCentralFolder(), FanartPath);
                    //Log.debug("ImageCache","GetFanartKey: does not start with central - f = '" + f + "'");
                }
            } else {
                f = new File(FanartPath);
                //Log.debug("ImageCache","GetFanartKey: central is null - f = '" + f + "'");
            }
            Key = f.getPath();
            //remove the filename from the key except for Episodes that need the filename to be unique (only 1 Episode background per episode)
            if (Key.contains("Episodes") || KeepFileName){
                //leave the filename on the Key so it is more unique
                //used for Episode backgrounds
                //used from Fanart Manager items so they are unique
            }else{
                //remove the File name from the path so we only have the path
                Key = f.getParent();
            }
            //Log.debug("ImageCache","GetFanartKey: Key path after Episode check '" + Key + "'");
            //now remove the central folder from the path
            if (central!=null){
                //Log.debug("ImageCache","GetFanartKey: before removed central '" + Key + "'");
                Key = Key.replace(central, "");
                //Log.debug("ImageCache","GetFanartKey: after removed central '" + Key + "'");
            }
        }
        Key = Key + util.ListToken + Size;
        //Log.debug("ImageCache","GetFanartKey: Key '" + Key + "'");
        return Key;
    }

    public static String GetFanartImageType(Object IMR){
        IMediaResource imediaresource = Source.GetTVIMediaResource(IMR);
        String specialType = Source.GetSpecialType(imediaresource);
        String fanartType = "poster";
        if ("tv".equals(specialType) || "airing".equals(specialType) || "recording".equals(specialType)){
            fanartType = "background";
        }
        return fanartType;
    }
    
    //returns a BufferedImage Object with the passed in Label text displayed
    public static BufferedImage GetImageLabel(String Label, String DisplayFont, int FontSize){
        return GetImageLabel(Label, 0, DisplayFont, FontSize, Font.PLAIN, Color.white);
    }
    public static BufferedImage GetImageLabel(String Label, int degreesRotate, String DisplayFont, int FontSize){
        return GetImageLabel(Label, degreesRotate, DisplayFont, FontSize, Font.PLAIN, Color.white);
    }
    public static BufferedImage GetImageLabel(String Label, String DisplayFont, int FontSize, int FontStyle){
        return GetImageLabel(Label, 0, DisplayFont, FontSize, FontStyle, Color.white);
    }
    public static BufferedImage GetImageLabel(String Label, int degreesRotate, String DisplayFont, int FontSize, int FontStyle){
        return GetImageLabel(Label, degreesRotate, DisplayFont, FontSize, FontStyle, Color.white);
    }
    public static BufferedImage GetImageLabel(String Label, String DisplayFont, int FontSize, Color FontColor){
        return GetImageLabel(Label, 0, DisplayFont, FontSize, Font.PLAIN, FontColor);
    }
    public static BufferedImage GetImageLabel(String Label, int degreesRotate, String DisplayFont, int FontSize, Color FontColor){
        return GetImageLabel(Label, degreesRotate, DisplayFont, FontSize, Font.PLAIN, FontColor);
    }
    public static BufferedImage GetImageLabel(String Label, int degreesRotate, String DisplayFont, int FontSize, int FontSytle, Color FontColor){
        //String FontPath = util.GetSageTVRootDir() + File.separator + "STVs" + File.separator + "SageTV7"  + File.separator + "Themes" +  File.separator + "Gemstone" +  File.separator + "Fonts";
        //String FontName = "";
        if (!DisplayFont.toLowerCase().endsWith(".ttf")){
            DisplayFont = DisplayFont + ".ttf";
        }
        FileInputStream fontFile = null;
        try {
            fontFile = new FileInputStream(DisplayFont);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        } catch (FontFormatException ex) {
            java.util.logging.Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        //we now have a font to work with
        font = font.deriveFont(FontSytle,FontSize);
        
        // create temporary 1x1 image to get FontRenderingContext needed to calculate image size
        BufferedImage buffer = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = buffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontRenderContext fc = g2.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(Label,fc);

        // calculate the size of the text
        int width = (int) bounds.getWidth();
        //int height = (int) bounds.getHeight();
        //force the box to be a square
        int height = width;

        // prepare final image with proper dimensions
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2 = buffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(font);

        //uncomment the next 2 lines to do testing with a solid background
        //g2.setColor(Color.BLUE);
        //g2.fillRect(0,0,width,height);

        // actually do the drawing
        g2.setColor(FontColor);
        int centerh = height/2 + ((int)-bounds.getY()/2); 
        //draw the text in the center of the square box
        g2.drawString(Label,0,centerh);

        // return the image
        if (degreesRotate>0){
            return rotateLabel(buffer, degreesRotate);
        }else{
            return buffer;
        }
        
    }
    
    private static BufferedImage rotateLabel(BufferedImage image, int degrees){
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(degrees), image.getWidth(), image.getHeight());
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    public static BufferedImage RotateImage(Object image, int degrees){
        return RotateImage(sagex.api.Utility.GetImageAsBufferedImage(image), degrees);
    }
    public static BufferedImage RotateImage(BufferedImage image, int degrees){
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(degrees), image.getWidth()/2, image.getHeight()/2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

}
