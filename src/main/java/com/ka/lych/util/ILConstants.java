package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public interface ILConstants {

    public final static String MAIN_VIEW = "mainView";
    
    public final static String CHILD = "child";    
    public final static String DOT = ".";
    public final static String UNDL = "_";
    public final static String LEFT = "left";
    public final static String RIGHT = "right";
    public final static String CENTER = "center";
    public final static String PARENT = "parent";
    public final static String TOP = "top";
    public final static String BOTTOM = "bottom";
    public final static String GAPLEFT = "gapleft";
    public final static String GAPRIGHT = "gapright";

    public final static int NORTH = 1;
    public final static int NORTH_EAST = 2;
    public final static int EAST = 3;
    public final static int SOUTH_EAST = 4;
    public final static int SOUTH = 5;
    public final static int SOUTH_WEST = 6;
    public final static int WEST = 7;
    public final static int NORTH_WEST = 8;

    public final static int FEEDBACK_OK = 0;
    public final static int FEEDBACK_WAIT = 1;
    public final static int FEEDBACK_ERROR = 2;
    public final static String OK = "ok";

    public final static int DRAG_NONE = -1;
    public final static int DRAG_MOVE = 0;
    public final static int DRAG_ROTATE = 1;
    public final static int DRAG_NE_RESIZE = 2;
    public final static int DRAG_SE_RESIZE = 3;
    public final static int DRAG_SW_RESIZE = 4;
    public final static int DRAG_NW_RESIZE = 5;

    public final static int PRIORITY_MIN = 1;
    public final static int PRIORITY_NORM = 5;
    public final static int PRIORITY_MAX = 10;

    public final static int ANIMATION_DURATION = 200;
    
    public final static String KEYWORD_ID = "fx:id";
    //public final static String KEYWORD_BEAN = "fx:bean";
    public final static String KEYWORD_CONTROLLER = "fx:controller";
    public final static String KEYWORD_EVENTHANDLER = "#";
    public final static String KEYWORD_RESOURCE_STRING = "$";    
    public final static String KEYWORD_RESOURCE_URL = "@";
    public final static char KEYWORD_HEX = '#';
    public final static String KEYWORD_COMPONENT = "component";
    public final static String KEYWORD_HTML_TAG = "<html>";
    public final static String KEYWORD_STATE_SEPARATOR = "|";
    public static final String KEYWORD_FILE_SVG_SUFFIX = "svg";
    public static final String KEYWORD_FILE_XML_SUFFIX = "xml";
    public static final String KEYWORD_FILE_JSON_SUFFIX = "json";

    public final static String ACTION_EXIT_PROGRAM = "exitProgram";
    public final static String ACTION_SHOW_ABOUT_INFORMATION = "showAboutInformation";
    public final static String ACTION_SHOW_PREFERENCES = "showPreferences";        
    public final static String BRACKET_CURLY_CLOSE = "}";
    public final static String BRACKET_CURLY_OPEN = "{";
    public final static String BRACKET_ROUND_CLOSE = ")";    
    public final static String BRACKET_ROUND_OPEN = "(";
    public final static String BRACKET_SQUARE_CLOSE = "]";
    public final static String BRACKET_SQUARE_OPEN = "[";
    public final static String CANCEL = "cancel";    
    public final static String CHANGE_LISTENERS = "changeListeners";
    public final static String CHANGED = "changed";    
    public final static String COLOR_FOREGROUND = "foreground";
    public final static String COLOR_BACKGROUND = "background";
    public final static String COLOR_TAB_COMPONENT = "colorTabComponent";
    public final static String CONTENT_AREA_FILLED = "contentAreaFilled";
    public final static String CONTEXT_MENU = "contextMenu";
    public final static String CONTROLLER = "controller";
    public final static String DARK_MODE = "darkMode";
    public final static String DATABASE = "database";
    public final static String DATABASE_TYPE = "databaseType";
    public final static String DATE = "date";
    public final static String DEFAULT = "default";
    public final static String DEFAULT_BR = BRACKET_SQUARE_OPEN + DEFAULT + BRACKET_SQUARE_CLOSE;
    public final static String DISABLED = "disabled";
    public final static String EDITABLE = "editable";
    public final static String ENABLED = "enabled";
    public final static String EXCEPTION = "exception";
    public final static String FOCUS_PAINTED = "focusPainted";
    public final static String FONT = "font";
    public final static String HASH_KEY = "hashKey";
    public final static String ICON = "icon";    
    public final static String INDETERMINATE = "indeterminate";
    public final static String ITEM = "item";    
    public final static String ITEMS = "items";    
    public final static String LAYOUT_UPDATE = "layoutUpdate";
    public final static String LOADING_STATE_VISIBLE = "loadingStateVisible";
    public final static String MAXIMUM = "maximum";
    public final static String MENU = "menu";    
    public final static String MENU_ITEM = "menuItem";    
    public final static String MINIMUM = "minimum";    
    public final static String MULTI_LINE = "multiLine";
    public final static String NOT_QUERIED_CHILDS = "notQueriedChilds";
    public final static String NULL = "null";    
    public final static String NULL_VALUE = "<null>";    
    public final static String ON_ACTION = "onAction";    
    public final static String ON_TAB_CHANGED = "onTabChanged";
    public final static String ON_ROLLOVER = "onRollover";   
    public final static String OWNER = "owner"; 
    public final static String PAGE = "page";    
    public final static String PASSWORD = "password";
    public final static String PREFERRED_HEIGHT = "preferredHeight";
    public final static String PREFERRED_WIDTH = "preferredWidth";
    public final static String PREFERRED_SIZE_USED = "preferredSizeUsed";
    public final static String PREFERRED_SPAN_X = "preferredSpanX";
    public final static String PRESSED = "pressed";
    public final static String PROMPT_TEXT = "promptText";
    public final static String QUOTATION_MARK_OPEN = "'";
    public final static String QUOTATION_MARK_CLOSE = "'";
    public final static String RENDERER = "renderer";
    public final static String RESOURCE_OPEN = KEYWORD_RESOURCE_STRING + BRACKET_CURLY_OPEN;
    public final static String RESOURCE_CLOSE = BRACKET_CURLY_CLOSE;
    public final static String ROLLOVER = "rollover";
    public final static String ROLLOVER_ENABLED = "rolloverEnabled";
    public final static String ROW_RENDERER = "rowRenderer";
    public final static String ROW_SPACER_BACKGROUND = "rowSpacerBackground";
    public final static String SHOW_MENU = "showMenu";
    public final static String SELECTED = "selected";
    public final static String SELECTED_PAGE = "selectedPage";
    public final static String SELECTION_BACKGROUND = "selectionBackground";
    public final static String SERVER = "server";
    public final static String SLASH = "/";
    public final static String SPACER_BACKGROUND = "spacerBackground";    
    public final static String STATE = "state";
    public final static String TAB_AREA_PLACEMENT = "tabAreaPlacement";
    public final static String TEXT = "text";    
    public final static String TITLE = "title";    
    public final static String TOOLTIP = "tooltip";    
    public final static String USER = "user";
    public final static String VALUE = "value";
    public final static String VISIBLE = "visible";
    public final static String WRAP_TEXT = "wrapText";
    public final static String YOSO = "yoso";

    public final static int DIALOGTYPE_OPEN = 0;
    public final static int DIALOGTYPE_SAVE = 1;

}
