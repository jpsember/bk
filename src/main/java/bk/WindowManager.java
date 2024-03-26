package bk;

import static js.base.Tools.*;

import java.util.List;
import java.util.SortedMap;

import js.base.BaseObject;
import js.data.DataUtil;
import js.geometry.IPoint;
import js.parsing.RegExp;

public final class WindowManager extends BaseObject {

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  /**
   * Determine if widget events should be propagated to listeners. False while
   * user interface is still being constructed
   */
  public boolean active() {
    return mActive;
  }

  public void setActive(boolean state) {
    log("set active:", state);
    if (mActive == state)
      log("...unchanged!");
    mActive = state;
  }

  public boolean exists(String id) {
    return (find(id) != null);
  }

  public JWindow get(String id) {
    var w = find(id);
    if (w == null)
      badState("Can't find widget with id:", id);
    return w;
  }

  private JWindow find(String id) {
    return mWidgetMap.get(id);
  }

  // ------------------------------------------------------------------
  // Accessing widget values
  // ------------------------------------------------------------------

  // ---------------------------------------------------------------------
  // Composing
  // ---------------------------------------------------------------------

  public static final int ALIGNMENT_DEFAULT = -1;
  public static final int ALIGNMENT_LEFT = 0;
  public static final int ALIGNMENT_CENTER = 1;
  public static final int ALIGNMENT_RIGHT = 2;

  /**
   * <pre>
   *
   * Set the number of columns, and which ones can grow, for the next view in
   * the hierarchy. The columns expression is a string of column expressions,
   * which may be one of:
   * 
   *     "."   a column with weight zero
   *     "x"   a column with weight 100
   *     "\d+" column with integer weight
   * 
   * Spaces are ignored, except to separate integer weights from each other.
   * </pre>
   */
  public final WindowManager columns(String columnsExpr) {
    checkState(mPendingColumnWeights == null, "previous column weights were never used");

    List<Integer> columnSizes = arrayList();
    for (String word : split(columnsExpr, ' ')) {
      if (RegExp.patternMatchesString("\\d+", word)) {
        columnSizes.add(Integer.parseInt(word));
      } else {
        for (int i = 0; i < word.length(); i++) {
          char c = columnsExpr.charAt(i);
          int size;
          if (c == '.') {
            size = 0;
          } else if (c == 'x') {
            size = 100;
          } else {
            throw new IllegalArgumentException(columnsExpr);
          }
          columnSizes.add(size);
        }
      }
    }
    mPendingColumnWeights = DataUtil.intArray(columnSizes);
    return this;
  }

  /**
   * Make next component added occupy remaining columns in its row
   */
  public final WindowManager spanx() {
    mSpanXCount = -1;
    return this;
  }

  /**
   * Make next component added occupy some number of columns in its row
   */
  public WindowManager spanx(int count) {
    checkArgument(count > 0);
    mSpanXCount = count;
    return this;
  }

  /**
   * Skip a single cell
   */
  public WindowManager skip() {
    return skip(1);
  }

  /**
   * Skip one or more cells
   */
  public WindowManager skip(int count) {
    spanx(count);
    todo("skip cells:", count);
    return this;
  }

  /**
   * Set pending component, and the column it occupies, as 'growable'. This can
   * also be accomplished by using an 'x' when declaring the columns.
   * <p>
   * Calls growX(100)...
   */
  public WindowManager growX() {
    return growX(100);
  }

  /**
   * Set pending component, and the column it occupies, as 'growable'. This can
   * also be accomplished by using an 'x' when declaring the columns.
   * <p>
   * Calls growY(100)...
   */
  public WindowManager growY() {
    return growY(100);
  }

  /**
   * Set pending component's horizontal weight to a value > 0 (if it is already
   * less than this value)
   */
  public WindowManager growX(int weight) {
    mGrowXFlag = Math.max(mGrowXFlag, weight);
    return this;
  }

  /**
   * Set pending component's vertical weight to a value > 0 (if it is already
   * less than this value)
   */
  public WindowManager growY(int weight) {
    mGrowYFlag = Math.max(mGrowYFlag, weight);
    return this;
  }

  /**
   * Specify the component to use for the next open() call, instead of
   * generating one
   */
  public WindowManager setPendingContainer(JWindow component) {
    checkState(mPanelStack.isEmpty(), "current panel stack isn't empty");
    mPendingContainer = component;
    return this;
  }

  private WindowManager setPendingAlignment(int value) {
    mPendingAlignment = value;
    return this;
  }

  public WindowManager left() {
    return setPendingAlignment(ALIGNMENT_LEFT);
  }

  public WindowManager right() {
    return setPendingAlignment(ALIGNMENT_RIGHT);
  }

  public WindowManager center() {
    return setPendingAlignment(ALIGNMENT_CENTER);
  }

  public WindowManager minWidth(float ems) {
    mPendingMinWidthEm = ems;
    return this;
  }

  public WindowManager minHeight(float ems) {
    mPendingMinHeightEm = ems;
    return this;
  }

  /**
   * Specify listener to add to next widget
   */
  public WindowManager listener(WidgetListener listener) {
    checkState(mPendingListener == null, "already a pending listener");
    mPendingListener = listener;
    return this;
  }

  /**
   * Specify listener to add to following widgets. Must be balanced by call to
   * popListener()
   */
  public WindowManager pushListener(WidgetListener listener) {
    checkState(mPendingListener == null, "already a pending listener");
    mListenerStack.add(listener);
    return this;
  }

  public WindowManager popListener() {
    checkState(mPendingListener == null, "already a pending listener");
    checkState(!mListenerStack.isEmpty(), "listener stack underflow");
    pop(mListenerStack);
    return this;
  }

  private WidgetListener consumePendingListener() {
    WidgetListener listener = mPendingListener;
    mPendingListener = null;
    if (listener == null && !mListenerStack.isEmpty()) {
      listener = last(mListenerStack);
    }
    return listener;
  }

  private void verifyUsed(Object value, String name) {
    if (value == null)
      return;
    String dispName = chompPrefix(name.trim(), "m");
    dispName = DataUtil.convertCamelCaseToUnderscores(dispName);
    throw badState("unused value:", dispName);
  }

  private void clearPendingComponentFields() {
    // If some values were not used, issue warnings
    verifyUsed(mPendingContainer, "pending container");
    verifyUsed(mPendingColumnWeights, "pending column weights");

    mPendingContainer = null;
    mPendingColumnWeights = null;
    mSpanXCount = 0;
    mGrowXFlag = mGrowYFlag = 0;
    mPendingAlignment = ALIGNMENT_DEFAULT;
    mPendingMinWidthEm = 0;
    mPendingMinHeightEm = 0;
  }

  // ------------------------------------------------------------------
  // Layout logic
  // ------------------------------------------------------------------

  private int[] mPendingColumnWeights;

  /**
   * Call widget listener, setting up event source beforehand
   */
  public void notifyWidgetListener(JWindow widget, WidgetListener listener) {
    if (!active())
      return;
    JWindow previousListener = mListenerWidget;
    try {
      mListenerWidget = widget;
      listener.widgetEvent(widget.id());
    } finally {
      mListenerWidget = previousListener;
    }
  }

  /**
   * Get widget associated with listener event
   */
  public JWindow eventSource() {
    checkNotNull(mListenerWidget, "no event source found");
    return mListenerWidget;
  }

  //  /**
  //   * Wrap a system-specific element within a Widget
  //   *
  //   * @param component
  //   *          component, or null to represent a gap in the layout
  //   */
  //  public Widget wrap(Object component) {
  //    if (component == null || component instanceof JComponent) {
  //      return new ComponentWidget((JComponent) component); //.setId(getAnonId());
  //    }
  //    if (component instanceof Widget)
  //      return (Widget) component;
  //    throw new IllegalArgumentException("cannot create Widget wrapper for: " + component);
  //  }

  public WindowManager open() {
    return open("<no context>");
  }

  private void log2(Object... messages) {
    if (!verbose())
      return;
    String indent = tab();
    Object[] msg = insertStringToFront(indent, messages);
    log(msg);
  }

  private String tab() {
    if (!verbose())
      return "";
    String dots = "................................................................................";
    int len = mPanelStack.size() * 4;
    len = Math.min(len, dots.length());
    return "|" + dots.substring(0, len);
  }

  /**
   * Create a child view and push onto stack
   */
  public WindowManager open(String debugContext) {
    notFinished("open");
//    log2("open", debugContext);
//
//    Grid grid = new Grid();
//    grid.setContext(debugContext);
//
//    {
//      if (mPendingColumnWeights == null)
//        columns("x");
//      grid.setColumnSizes(mPendingColumnWeights);
//      mPendingColumnWeights = null;
//
//      JComponent panel;
//      if (mPendingContainer != null) {
//        panel = mPendingContainer;
//        log2("pending container:", panel.getClass());
//        mPendingContainer = null;
//      } else {
//        log2("constructing JPanel");
//        panel = new JPanel();
//        applyMinDimensions(panel, mPendingMinWidthEm, mPendingMinHeightEm);
//      }
//      notFinished("buildLayout");
////      panel.setLayout(buildLayout());
//      addStandardBorderForSpacing(panel);
//      grid.setWidget(wrap(panel));
//    }
//    add(grid.widget());
//    mPanelStack.add(grid);
//    log2("added grid to panel stack, its widget:", grid.widget().getClass());
    return this;
  }

  /**
   * Pop view from the stack
   */
  public WindowManager close() {
    return close("<no context>");
  }

  /**
   * Pop view from the stack
   */
  public WindowManager close(String debugContext) {
    log2("about to close", debugContext);

    Grid parent = pop(mPanelStack);
    endRow();
    assignViewsToGridLayout(parent);
    return this;
  }

  /**
   * Verify that no unused 'pending' arguments exist, calls are balanced, etc
   */
  public WindowManager finish() {
    clearPendingComponentFields();
    if (!mPanelStack.isEmpty())
      badState("panel stack nonempty; size:", mPanelStack.size());
    if (!mListenerStack.isEmpty())
      badState("listener stack nonempty; size:", mListenerStack.size());
    return this;
  }

  /**
   * If current row is only partially complete, add space to its end
   */
  public WindowManager endRow() {
    if (mPanelStack.isEmpty())
      return this;
    Grid parent = last(mPanelStack);
    if (parent.nextCellLocation().x != 0)
      spanx().addHorzSpace();
    return this;
  }

  /**
   * Add a horizontal space to occupy cell(s) in place of other widgets
   */
  public WindowManager addHorzSpace() {
    notFinished("addHorzSpace");
    // add(wrap(new JPanel()));
    return this;
  }

  /**
   * Add a row that can stretch vertically to occupy the available space
   */
  public WindowManager addVertGrow() {
    JWindow panel;
    panel = new JWindow();
    spanx().growY();
    add(panel);
    return this;
  }

  /**
   * Add widget to view hierarchy
   */
  public WindowManager add(JWindow widget) {
    String id = null;
    if (widget.hasId())
      id = widget.id();
    log2("add widget", id != null ? id : "<anon>");

    if (id != null) {
      if (exists(widget.id()))
        badState("attempt to add widget id:", widget.id(), "that already exists");
      mWidgetMap.put(id, widget);
    }
    addView(widget);
    return this;
  }

  /**
   * Add a component to the current panel. Process pending constraints
   */
  private WindowManager addView(JWindow widget) {

    if (!mPanelStack.isEmpty())
      auxAddComponent(widget);

    clearPendingComponentFields();
    return this;
  }

  private void auxAddComponent(JWindow widget) {
    // JComponent component = widget.swingComponent();

    // If the parent grid's widget is a tabbed pane,
    // add the component to it

    Grid grid = last(mPanelStack);

    GridCell cell = new GridCell();
    cell.view = widget;
    IPoint nextGridCellLocation = grid.nextCellLocation();
    cell.x = nextGridCellLocation.x;
    cell.y = nextGridCellLocation.y;

    // determine location and size, in cells, of component
    int cols = 1;
    if (mSpanXCount != 0) {
      int remainingCols = grid.numColumns() - cell.x;
      if (mSpanXCount < 0)
        cols = remainingCols;
      else {
        if (mSpanXCount > remainingCols)
          throw new IllegalStateException(
              "requested span of " + mSpanXCount + " yet only " + remainingCols + " remain");
        cols = mSpanXCount;
      }
    }
    cell.width = cols;

    cell.growX = mGrowXFlag;
    cell.growY = mGrowYFlag;

    // If any of the spanned columns have 'grow' flag set, set it for this component
    for (int i = cell.x; i < cell.x + cell.width; i++) {
      int colSize = grid.columnSizes()[i];
      cell.growX = Math.max(cell.growX, colSize);
    }

    // "paint" the cells this view occupies by storing a copy of the entry in each cell
    for (int i = 0; i < cols; i++)
      grid.addCell(cell);
  }

  private static String compInfo(JWindow c) {
    String s = c.getClass().getSimpleName();
    return s;
  }
 

  private List<Grid> mPanelStack = arrayList();

  // ------------------------------------------------------------------
  // Layout manager
  // ------------------------------------------------------------------

//  private LayoutManager buildLayout() {
//    return new GridBagLayout();
//  }

  private void assignViewsToGridLayout(Grid grid) {
    todo("refactor this to simplify");
    //    grid.propagateGrowFlags();
    //    JWindow container = grid.widget();
    //
    //    int gridWidth = grid.numColumns();
    //    int gridHeight = grid.numRows();
    //    for (int gridY = 0; gridY < gridHeight; gridY++) {
    //      for (int gridX = 0; gridX < gridWidth; gridX++) {
    //        GridCell cell = grid.cellAt(gridX, gridY);
    //        if (cell.isEmpty())
    //          continue;
    //
    //        // If cell's coordinates don't match our iteration coordinates, we've
    //        // already added this cell
    //        if (cell.x != gridX || cell.y != gridY)
    //          continue;
    //
    //        JWindow widget = (JWindow) cell.view;
    //
    //        // Not using gravity
    //        container.add(widget.swingComponent(), gc);
    //      }
    //    }
  }

  private SortedMap<String, JWindow> mWidgetMap = treeMap();
  private boolean mActive;

  private WidgetListener mPendingListener;
  private JWindow mListenerWidget;

  private JWindow mPendingContainer;
  private int mSpanXCount;
  private int mGrowXFlag, mGrowYFlag;
  private int mPendingAlignment;
  private float mPendingMinWidthEm;
  private float mPendingMinHeightEm;
  private List<WidgetListener> mListenerStack = arrayList();
}
