/****************************************************************************
 **
 ** This file is part of yFiles-2.7.0.1. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package darls;

import darls.Darls.AbstractToggledAction;
import darls.UserStudyBase;
import y.base.Node;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.option.OptionHandler;
import y.util.D;
import y.view.AutoDragViewMode;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.MovePortMode;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

//import demo.io.graphml.CustomNodeRealizerSerializer;


import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

//import demo.view.DemoDefaults;

/**
 * Abstract base class for GUI- and <code>Graph2DView</code>-based demos.
 * Provides useful callback methods. <p>To avoid problems with
 * "calls to overwritten method in constructor", do not initialize the demo
 * within the constructor of the subclass, use the method {@link #initialize()}
 * instead.</p>
 */
public abstract class DarlsBase {
  /**
   * Initializes to a "nice" look and feel.
   */
  public static void initLnF() {
    try {
      if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * The view component of this demo.
   */
  final int PRACTICE_PARTICIPANT = 100;
  protected Graph2DView view;

  protected JPanel contentPane;
  public JToolBar jtb;
  protected JMenuBar jmb;
  public JFrame frame;
  public static JMenuItem visualsMenu;
  public static JMenuItem animationMenu;
  
  public static JMenuItem layoutMenu;
  public static JMenuItem layoutOptionMenu;
  public static JMenuItem cLayoutMenu;
  public static JMenuItem rLayoutMenu;
  
  public static JMenuItem userStudyMenu;
  public static JMenuItem giveUpTrialMenu;
  //all the variables related to settings
  //for visual related
  protected boolean overlay_graph_in_left_view = true;
  protected boolean overlay_graph_in_right_view  = true;
  protected boolean enable_static_rectangle  = true;
  //for animaiton rejection
  protected boolean enable_animation_rejection = true;
  protected int duration_fade_in_rejection = 1000;
  protected int duration_fade_out_rejection = 1000;
  protected int duration_morph_rejection = 1000;
  //for animation comparison
  protected boolean enable_animation_comparison = true;
  public int duration_fade_in_comparison = 2000;
  public int duration_fade_out_comparison = 2000;
  public int duration_morph_comparison = 2000;
  
  //public IncrementalHierarchicLayouterModule rihlm = null; //commented out a line which initializes it in this class for experiment, uncomment it
  
  Graph2DViewMouseWheelZoomListener wheelZoomListener = null;
  protected Graph2DView left_view = null;


  
  /**
   * This constructor creates the {@link #view} and calls,
   * {@link #createToolBar()} {@link #registerViewModes()},
   * {@link #registerViewActions()}, and {@link #registerViewListeners()}
   */
  
  //moved it from the child class because these get nulled out after createToolBar() is called 
  protected AbstractToggledAction 	setSelectionModeAction 		= null;
  protected AbstractToggledAction 	setEditModeAction 			= null;
  protected AbstractToggledAction 	setExperimentModeAction		= null;
  
  protected DarlsBase() {
	  	visualsMenu = new JMenuItem("Visuals");
	    animationMenu = new JMenuItem("Animation");
	    
	    layoutMenu = new JMenuItem("Layout");
	    layoutOptionMenu = new JMenuItem("Layout Options");
	    cLayoutMenu = new JMenuItem("Complete Re-Layout");
	    rLayoutMenu = new JMenuItem("Relative Re-Layout");

	    userStudyMenu = new JMenuItem("User Study");
	    giveUpTrialMenu = new JMenuItem("Give Up Trial");
	    //System.out.println("enable rectangle in left: "+overlay_graph_in_left_view);
	    //System.out.println("enable rectangle in left: "+overlay_graph_in_right_view);
	
	    //initialize the variables
	    initialize_variables();
    
	    view = new Graph2DView();
	    view.setFitContentOnResize(true);
	    configureDefaultRealizers();

	    contentPane = new JPanel();
	    contentPane.setLayout(new BorderLayout());
    
	    initialize();
	    //contentPane.setLayout(new BorderLayout());
	    
	    //rihlm = new  IncrementalHierarchicLayouterModule(view); //commented out for experiment
	    
	    registerViewModes();
	    registerViewActions();

	    contentPane.add(view, BorderLayout.CENTER);
	    jtb = createToolBar();
    	//if (jtb != null) {
	    //contentPane.add(jtb, BorderLayout.NORTH);
	    contentPane.add(jtb, BorderLayout.NORTH);
	    jtb.setIgnoreRepaint(true);

	    registerViewListeners();
  }

  void initialize_variables()
  {
	  	overlay_graph_in_left_view = true;
	    overlay_graph_in_right_view = true;
	    enable_static_rectangle = true;
	    enable_animation_rejection = true;
	    enable_animation_comparison = true;
  }  
  /**
   * Configures the default node realizer and default edge realizer used by subclasses
   * of this demo. The default implementation delegates to {@link DarlsDefaults#configureDefaultRealizers(Graph2DView)}.
   */
  protected void configureDefaultRealizers() {
    DarlsDefaults.configureDefaultRealizers(view);    
  }

  /**
   * This method is called before the view modes and actions are registered and
   * the menu and toolbar is build.
   */
  protected void initialize() {
  }

  public void dispose() {
  }

  protected void loadGraph(URL resource) {

    if (resource == null) {
      String message = "Resource \"" + resource + "\" not found in classpath";
      D.showError(message);
      throw new RuntimeException(message);
    }

    try {
      IOHandler ioh = createGraphMLIOHandler();
      view.getGraph2D().clear();
      ioh.read(view.getGraph2D(), resource);
    } catch (IOException e) {
      String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
      D.bug(message);
      throw new RuntimeException(message, e);
    }
    view.getGraph2D().setURL(resource);
    view.fitContent();
    view.updateView();

  }

  protected GraphMLIOHandler createGraphMLIOHandler() {
	GraphMLIOHandler ioHandler = new GraphMLIOHandler();
	ioHandler.addNodeRealizerSerializer(new ClassNodeRealizerSerializer());
	  
    return ioHandler;
  }

  protected void loadGraph(Class aClass, String resourceString) {
    final URL resource = aClass.getResource(resourceString);
    if (resource == null) {
      String message = "Resource \"" + resourceString + "\" not found in classpath of " + aClass;
      D.showError(message);
      throw new RuntimeException(message);
    }
    loadGraph(resource);
  }

  protected void loadGraph(String resourceString) {
    loadGraph(getClass(), resourceString);
  }

  /**
   * Creates an application frame for this demo and displays it. The class name
   * is the title of the displayed frame.
   */
  public final void start() {
    //start(getClass().getName());
    start("DARLS");
  }

  /**
   * Creates an application frame for this demo and displays it. The given
   * string is the title of the displayed frame.
   */
  public final void start(String title) {
	frame = new JFrame(title);
    JOptionPane.setRootFrame(frame);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addContentTo(frame.getRootPane());
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    System.out.println(screenSize.getHeight()+"-----"+screenSize.getWidth());
    frame.setBounds(0,0,1920,1040);
    //frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public void addContentTo(final JRootPane rootPane) {
    jmb = createMenuBar();
    if (jmb != null) {
      rootPane.setJMenuBar(jmb);
    }
    rootPane.setContentPane(contentPane);
  }

  protected void registerViewActions() {
    // register keyboard actions
    Graph2DViewActions actions = new Graph2DViewActions(view);
   
    ActionMap amap = view.getCanvasComponent().getActionMap();
    if (amap != null) {
      InputMap imap = actions.createDefaultInputMap(amap);
      if (!isDeletionEnabled()) {
        amap.remove(Graph2DViewActions.DELETE_SELECTION);
      }
      view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
    }
  }

  /**
   * Adds the view modes to the view. This implementation adds a new EditMode
   * created by {@link #createEditMode()} a new {@link AutoDragViewMode}.
   */
  protected void registerViewModes() {
    // edit mode will show tool tips over nodes
    EditMode editMode = createEditMode();
    if (editMode != null) {
      view.addViewMode(editMode);
    }
    view.addViewMode(new AutoDragViewMode());
  }

  /**
   * Callback used by {@link #registerViewModes()} to create the default
   * EditMode.
   *
   * @return an instance of {@link EditMode} with showNodeTips enabled.
   */
  protected EditMode createEditMode() {
    EditMode editMode = new EditMode();
    // show the highlighting which is turned off by default
    if (editMode.getCreateEdgeMode() instanceof CreateEdgeMode) {
      ((CreateEdgeMode) editMode.getCreateEdgeMode()).setIndicatingTargetNode(true);
    }
    if (editMode.getMovePortMode() instanceof MovePortMode) {
      ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);
    }
    editMode.showNodeTips(true);
    
    //allow moving view port with right drag gesture
    editMode.allowMovingWithPopup(true);
    
    return editMode;
  }

  /**
   * Instantiates and registers the listeners for the view (e.g.
   * {@link y.view.Graph2DViewMouseWheelZoomListener}).
   */
  protected void registerViewListeners() {
	  //December 15, 2010 disabled it so that it doesnt interefere with userstudy
    /*wheelZoomListener = new MyGraph2DViewMouseWheelZoomListener();
    //zoom in/out at mouse pointer location 
    wheelZoomListener.setCenterZooming(false);    
    view.getCanvasComponent().addMouseWheelListener(wheelZoomListener);*/
  }
  class MyGraph2DViewMouseWheelZoomListener extends Graph2DViewMouseWheelZoomListener
  {
	  public void mouseWheelMoved(MouseWheelEvent e)
	  {
		  super.mouseWheelMoved(e);
		  if (view != null && left_view != null)
		  {
			left_view.setZoom(view.getZoom());
			left_view.updateView();
		  }
	  }
  }
  /**
   * Determines whether default actions for deletions will be added to the view
   * and toolbar.
   */
  protected boolean isDeletionEnabled() {
    return true;
  }

  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();
    if (isDeletionEnabled()) {
      toolBar.add(createDeleteSelectionAction());
    }
    toolBar.add(new Zoom(1.25));
    toolBar.add(new Zoom(0.8));
    toolBar.add(new ResetZoom());
    toolBar.add(new FitContent(view));

    return toolBar;
  }

  protected  abstract   Action createGraphLoadVersionAction();
  protected  abstract   Action createGraphCommitVersionAction();

  protected  abstract   Action createUMLLoadVersionAction();
  protected  abstract   Action createUMLCommitVersionAction();
  protected  abstract   void give_up_trial();



  /**
   * Create a menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    Action action;
    action = createLoadAction();
    if (action != null) {
      menu.add(action);
    }

    action = createSaveAction();
    if (action != null) {
      menu.add(action);
    }
    menu.addSeparator();
    //----------------------------------------
    //for graphs
    action = createGraphLoadVersionAction();
    if (action != null) {
      menu.add(action);
    }
    action = createGraphCommitVersionAction();
    if (action != null) {
      menu.add(action);
    }
    menu.addSeparator();
    //----------------------------------------
    //----------------------------------------
    //for uml
    action = createUMLLoadVersionAction();
    if (action != null) {
      menu.add(action);
    }
    action = createUMLCommitVersionAction();
    if (action != null) {
      menu.add(action);
    }
    menu.addSeparator();
    //----------------------------------------

    menu.add(new PrintAction());
    menu.addSeparator();
    menu.add(new ExitAction());
    
    MenuListener menuListener = new MenuListener();
    
    JMenu settingsMenu = new JMenu("Settings");
    settingsMenu.add(visualsMenu);
    settingsMenu.add(animationMenu);
    visualsMenu.addActionListener(menuListener);
    animationMenu.addActionListener(menuListener);
    visualsMenu.setActionCommand("visual_settings");
    animationMenu.setActionCommand("animation_settings");

    menuBar.add(menu);
    menuBar.add(settingsMenu);
    //-------------------------------------------
    //Layout
    JMenu layoutTopMenu = new JMenu("Layout");
    layoutTopMenu.add(layoutMenu);
    layoutTopMenu.add(layoutOptionMenu);
    layoutTopMenu.addSeparator();
    layoutTopMenu.add(cLayoutMenu);
    layoutTopMenu.add(rLayoutMenu);
    layoutMenu.addActionListener(menuListener);
    layoutOptionMenu.addActionListener(menuListener);
    cLayoutMenu.addActionListener(menuListener);
    rLayoutMenu.addActionListener(menuListener);
    layoutMenu.setActionCommand("layout");
    layoutOptionMenu.setActionCommand("layout_option");
    cLayoutMenu.setActionCommand("c_layout");
    rLayoutMenu.setActionCommand("r_layout");

    JMenu experiemntsMenu = new JMenu("Experiment");
    experiemntsMenu.add(userStudyMenu);
    //experiemntsMenu.add(giveUpTrialMenu);
	
    userStudyMenu.addActionListener(menuListener);
	userStudyMenu.setActionCommand("user_study");
	
	//giveUpTrialMenu.addActionListener(menuListener);
	//giveUpTrialMenu.setActionCommand("give_up_trial");
    
    menuBar.add(menu);
    menuBar.add(settingsMenu);
    menuBar.add(layoutTopMenu);
    menuBar.add(experiemntsMenu);
    
    return menuBar;
  }

  protected Action createLoadAction() {
    return new LoadAction();
  }

  protected Action createSaveAction() {
    return new SaveAction();
  }

  //abstract protected Action createLoadVersionAction();
  //abstract protected Action createCommitVersionAction();
  
 


  protected Action createDeleteSelectionAction() {
    return new DeleteSelection(view);
  }

  public JPanel getContentPane() {
    return contentPane;
  }

  /**
   * Action that prints the contents of the view
   */
  
  public abstract void layoutAction();
  public abstract void layoutOptionAction();
  public static final String[] groupPolicy = { "LAYOUT_GROUPS", "FIX_GROUPS", "IGNORE_GROUPS" };
  public abstract void reLayoutAction(String groupPolicy);
  
  public class MenuListener implements ActionListener
  {
	  MenuListener()
	  {
		  
	  }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("layout"))
		{
			layoutAction();
		}
		else if (e.getActionCommand().equals("layout_option"))
		{
			layoutOptionAction();

		}
		else if (e.getActionCommand().equals("c_layout")) 
		{
			reLayoutAction(groupPolicy[2]); //complete relayout, ignore groups

		}
		else if (e.getActionCommand().equals("r_layout")) 
		{
			reLayoutAction(groupPolicy[1]); //relative relaout

		}
		else if(e.getActionCommand().equals("visual_settings"))
		{
			//System.out.println("this is inside the animation related setting");
			final JFrame animationFrame = new JFrame("Visual Settings");
			animationFrame.setSize(300, 300);
			JLabel on = new JLabel("on");
			JLabel off = new JLabel("off");
			//animationFrame.setLayout(new GridLayout(5,3,2,2));
			animationFrame.setLayout(new GridLayout(5,3,2,2));
			//animationFrame.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			animationFrame.add(new JLabel());
			animationFrame.add(on);
			animationFrame.add(off);
			animationFrame.add(new JLabel("Left view overlay graph"));
			final JRadioButton button1 = new JRadioButton();
			JRadioButton button2 = new JRadioButton();

			ButtonGroup bg1 = new ButtonGroup();
			bg1.add(button1);
			bg1.add(button2);

			if(overlay_graph_in_left_view == true)
			{
				button1.setSelected(true);
			}
			else
			{
				button2.setSelected(true);
			}
			animationFrame.add(button1);
			animationFrame.add(button2);
			animationFrame.add(new JLabel("Right view overlay graph"));
			final JRadioButton button3 = new JRadioButton();
			JRadioButton button4 = new JRadioButton();
			ButtonGroup bg2 = new ButtonGroup();
			bg2.add(button3);
			bg2.add(button4);
			if(overlay_graph_in_right_view == true)
			{
				button3.setSelected(true);
			}
			else
			{
				button4.setSelected(true);
			}
			animationFrame.add(button3);
			animationFrame.add(button4);
			animationFrame.add(new JLabel("Static rectangle"));
			final JRadioButton button5 = new JRadioButton();
			JRadioButton button6 = new JRadioButton();

			ButtonGroup bg3 = new ButtonGroup();
			bg3.add(button5);
			bg3.add(button6);

			if(enable_static_rectangle == true)
			{
				button5.setSelected(true);
			}
			else
			{
				button6.setSelected(true);
			}
			animationFrame.add(button5);
			animationFrame.add(button6);
			animationFrame.add(new JLabel());
			JButton ok = new JButton("ok");
			JButton b_default = new JButton("default");
			b_default.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
	                System.out.println("You clicked the button");
	                button1.setSelected(true);
	                button3.setSelected(true);
	                button5.setSelected(true);
	            }
	        });
			ok.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
	                System.out.println("You clicked the button");
	                if(button1.isSelected())
	                	overlay_graph_in_left_view = true;
	                else
	                	overlay_graph_in_left_view = false;
	                System.out.println("enable gray nodes in left: "+overlay_graph_in_left_view);
	                if(button3.isSelected())
	                	overlay_graph_in_right_view = true;
	                else
	                	overlay_graph_in_right_view = false;
	                System.out.println("enable gray nodes in right: "+overlay_graph_in_right_view);
	                if(button5.isSelected())
	                	enable_static_rectangle = true;
	                else
	                	enable_static_rectangle = false;
	                System.out.println("enable static rectangle: "+enable_static_rectangle);
	                animationFrame.setVisible(false);
	                
	            }
	        });
			animationFrame.add(ok);
			animationFrame.add(b_default);
			animationFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			animationFrame.setVisible(true);

		}
		else if(e.getActionCommand().equals("animation_settings"))
		{
			//System.out.println("this is inside the visual related setting");
			final JDialog visualFrame = new JDialog(frame,"Animation Settings");
			JTabbedPane tabbedPane = new JTabbedPane();
			JPanel panel_animation_rejection = new JPanel();
			JPanel panel_animation_comparison = new JPanel();
			panel_animation_rejection.setLayout(new GridLayout(11,2));
			panel_animation_comparison.setLayout(new GridLayout(11,2));
			
			//for animation rejection
			JButton ok_rejection = new JButton("ok");
			JButton b_default_rejection = new JButton("default");
			final JTextField fade_in_text_rejection = new JTextField(""+duration_fade_in_rejection);
			final JTextField fade_out_text_rejection = new JTextField(""+duration_fade_out_rejection);
			final JTextField morph_text_rejection = new JTextField(""+duration_morph_rejection);
			panel_animation_rejection.add(new JLabel());
			JPanel stringpanel = new JPanel();
			stringpanel.add(new JLabel("on"));
			stringpanel.add(new JLabel("off"));
			panel_animation_rejection.add(stringpanel);
			panel_animation_rejection.add(new JLabel("Animation Rejection"));
			final JRadioButton on = new JRadioButton();
			JRadioButton off = new JRadioButton();
			ButtonGroup bg = new ButtonGroup();
			bg.add(on);
			bg.add(off);
			
			if (enable_animation_rejection)
				on.setSelected(true);
			else
				off.setSelected(true);
			
			JPanel onoffpanel = new JPanel();
			onoffpanel.add(on);
			onoffpanel.add(off);
			panel_animation_rejection.add(onoffpanel);
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel("Duration"));
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel("Fade-In"));
			panel_animation_rejection.add(fade_in_text_rejection);
			panel_animation_rejection.add(new JLabel("Fade-Out"));
			panel_animation_rejection.add(fade_out_text_rejection);
			panel_animation_rejection.add(new JLabel("Morph"));
			panel_animation_rejection.add(morph_text_rejection);
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(new JLabel());
			panel_animation_rejection.add(ok_rejection);
			panel_animation_rejection.add(b_default_rejection);
			b_default_rejection.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
	                fade_in_text_rejection.setText(""+duration_fade_in_rejection);
	                fade_out_text_rejection.setText(""+duration_fade_out_rejection);
	                morph_text_rejection.setText(""+duration_morph_rejection);
	                on.setSelected(true);
	            }
	        });
			ok_rejection.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
	                //System.out.println("You clicked the button");
	            	enable_animation_rejection = on.isSelected();

	                //System.out.println("enable animation rejection: "+enable_animation_rejection);
	                duration_fade_in_rejection = Integer.parseInt(fade_in_text_rejection.getText());
	                //System.out.println("duration fade in rejection: "+duration_fade_in_rejection);
	                duration_fade_out_rejection = Integer.parseInt(fade_out_text_rejection.getText());
	                //System.out.println("duration fade out rejection: "+duration_fade_out_rejection);
	                duration_morph_rejection = Integer.parseInt(morph_text_rejection.getText());
	                //System.out.println("duration morph rejection: "+duration_morph_rejection);
	                
	                visualFrame.setVisible(false); //hide the menu
	            }
	        });
			
			
			//for animaiton comparison
			JButton ok_comparison = new JButton("ok");
			JButton b_default_comparison = new JButton("default");
			final JTextField fade_in_text_comparison = new JTextField(""+duration_fade_in_comparison);
			final JTextField fade_out_text_comparison = new JTextField(""+duration_fade_out_comparison);
			final JTextField morph_text_comparison = new JTextField(""+duration_morph_comparison);
			panel_animation_comparison.add(new JLabel());
			JPanel stringpanel_2 = new JPanel();
			stringpanel_2.add(new JLabel("on"));
			stringpanel_2.add(new JLabel("off"));
			panel_animation_comparison.add(stringpanel_2);
			panel_animation_comparison.add(new JLabel("Animation Comparison"));
			final JRadioButton on_2 = new JRadioButton();
			JRadioButton off_2 = new JRadioButton();
			ButtonGroup bg_2 = new ButtonGroup();
			bg_2.add(on_2);
			bg_2.add(off_2);
			JPanel onoffpanel_2 = new JPanel();
			onoffpanel_2.add(on_2);
			onoffpanel_2.add(off_2);
			
			if (enable_animation_comparison)
				on_2.setSelected(true);
			else
				off_2.setSelected(true);
			
			panel_animation_comparison.add(onoffpanel_2);
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel("Duration"));
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel("Fade-In"));
			panel_animation_comparison.add(fade_in_text_comparison);
			panel_animation_comparison.add(new JLabel("Fade-Out"));
			panel_animation_comparison.add(fade_out_text_comparison);
			panel_animation_comparison.add(new JLabel("Morph"));
			panel_animation_comparison.add(morph_text_comparison);
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(new JLabel());
			panel_animation_comparison.add(ok_comparison);
			panel_animation_comparison.add(b_default_comparison);
			b_default_comparison.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
	                //System.out.println("You clicked the button");
	                fade_in_text_comparison.setText(""+duration_fade_in_comparison);
	                fade_out_text_comparison.setText(""+duration_fade_out_comparison);
	                morph_text_comparison.setText(""+duration_morph_comparison);
	                on_2.setSelected(true);
	            }
	        });
			ok_comparison.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                //Execute when button is pressed
	                //System.out.println("You clicked the button");
	                enable_animation_comparison = on_2.isSelected();

	                //System.out.println("enable animation comparison: "+enable_animation_comparison);
	                duration_fade_in_comparison = Integer.parseInt(fade_in_text_comparison.getText());
	                //System.out.println("duration fade in comparison: "+duration_fade_in_comparison);
	                duration_fade_out_comparison = Integer.parseInt(fade_out_text_comparison.getText());
	                //System.out.println("duration fade out comparison: "+duration_fade_in_comparison);
	                duration_morph_comparison = Integer.parseInt(morph_text_comparison.getText());
	                //System.out.println("duration morph comparison: "+duration_morph_comparison);
	                visualFrame.setVisible(false);
	            }
	        });
			
			tabbedPane.addTab("Animation Rejection", panel_animation_rejection);
	        tabbedPane.addTab("Animation Comparison", panel_animation_comparison);
	        
	        visualFrame.add(tabbedPane);
			visualFrame.setSize(400, 400);
			visualFrame.setVisible(true);
			visualFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		}
		else if(e.getActionCommand().equals("give_up_trial"))
		{
			give_up_trial();
		}
		else if(e.getActionCommand().equals("user_study"))
		{

			UserStudyFrame usf = new UserStudyFrame();
			usf.setVisible(true);
			
			/*final JFrame userStudyFrame = new JFrame("User Study");
			userStudyFrame.setSize(300, 300);
			userStudyFrame.setLayout(new GridLayout(1,2,0,0));

			JButton b_run = new JButton("run");
			JButton b_continue = new JButton("continue");
			b_continue.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                System.out.println("You clicked the continue button");
	                userStudyFrame.setVisible(false);
	            }
	        });
			b_run.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                System.out.println("You clicked the run button");
	                userStudyFrame.setVisible(false);
	                
	            }
	        });
			
			userStudyFrame.add(b_continue);
			userStudyFrame.add(b_run);

			userStudyFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			userStudyFrame.setVisible(true);*/
		}
	}
  }
  
  protected abstract void startExperiment(int participant,int trial, String study); //this will be implemented in the bottom class
  
  private class UserStudyFrame extends javax.swing.JFrame {

	    /** Creates new form UserStudyUI */
	    public UserStudyFrame() {
	    	super("User Study");
	        initComponents();
	    }

	    /** This method is called from within the constructor to
	     * initialize the form.
	     * WARNING: Do NOT modify this code. The content of this method is
	     * always regenerated by the Form Editor.
	     */
	    @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	    private void initComponents() {

	        jPanel1 = new javax.swing.JPanel();
	        jLabel1 = new javax.swing.JLabel();
	        jComboBox1 = new javax.swing.JComboBox();
	        jLabel2 = new javax.swing.JLabel();
	        jComboBox2 = new javax.swing.JComboBox();
	        jLabel3 = new javax.swing.JLabel();
	        jTextField1 = new javax.swing.JTextField();
	        jButton1 = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

	        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("User Study"));

	        jLabel1.setText("Participant No.:");

	        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }));

	        jLabel2.setText("Study:");

	        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(UserStudyBase.str_user_studies));

	        jLabel3.setText("From Sequence:");

	        jTextField1.setText("0");

	        jButton1.setText("Run");

	        jButton1.addActionListener(new ActionListener() {
	               
	            public void actionPerformed(ActionEvent e)
	            {
	                System.out.println("You clicked the run button");
	                String str_condition_no = jTextField1.getText();
	                int _current_trial = Integer.parseInt(str_condition_no);
	                //System.out.println("codition:"+sequence_no);
	                //fix below
	                String str_selected_item = (String) jComboBox1.getSelectedItem();
	                int participant_no = Integer.parseInt(str_selected_item); 
	                
	                //System.out.println("particiapnt:"+participant_no);

	                
	                //if this is a practise session we set participant number to 99;
	                String str_study = (String) jComboBox2.getSelectedItem();
	                
	                startExperiment(participant_no, _current_trial, str_study);
	                
	                setVisible(false);
	                	            }
	        });
	        
	        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
	        jPanel1.setLayout(jPanel1Layout);
	        jPanel1Layout.setHorizontalGroup(
	            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(jPanel1Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
	                        .addComponent(jLabel2)
	                        .addGap(61, 61, 61)
	                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
	                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            .addComponent(jLabel3)
	                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
	                .addContainerGap(23, Short.MAX_VALUE))
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
	                .addContainerGap(266, Short.MAX_VALUE)
	                .addComponent(jButton1)
	                .addContainerGap())
	        );
	        jPanel1Layout.setVerticalGroup(
	            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(jPanel1Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(jLabel1))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(jLabel2))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel3)
	                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
	                .addComponent(jButton1)
	                .addContainerGap())
	        );

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addContainerGap())
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(32, 32, 32)
	                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        pack();
	    }// </editor-fold>//GEN-END:initComponents

	    // Variables declaration - do not modify//GEN-BEGIN:variables
	    private javax.swing.JButton jButton1;
	    private javax.swing.JComboBox jComboBox1;
	    private javax.swing.JComboBox jComboBox2;
	    private javax.swing.JLabel jLabel1;
	    private javax.swing.JLabel jLabel2;
	    private javax.swing.JLabel jLabel3;
	    private javax.swing.JPanel jPanel1;
	    private javax.swing.JTextField jTextField1;
	    // End of variables declaration//GEN-END:variables

	}
  protected class PrintAction extends AbstractAction {
    PageFormat pageFormat;

    OptionHandler printOptions;

    public PrintAction() {
      super("Print");

      // setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.addInt("Poster Rows", 1);
      printOptions.addInt("Poster Columns", 1);
      printOptions.addBool("Add Poster Coords", false);
      final String[] area = {"View", "Graph"};
      printOptions.addEnum("Clip Area", area, 1);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DPrinter gprinter = new Graph2DPrinter(view);

      // show custom print dialog and adopt values
      if (!printOptions.showEditor()) {
        return;
      }
      gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
      gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
      gprinter.setPrintPosterCoords(printOptions.getBool("Add Poster Coords"));
      if ("Graph".equals(printOptions.get("Clip Area"))) {
        gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
      } else {
        gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
      }

      // show default print dialogs
      PrinterJob printJob = PrinterJob.getPrinterJob();
      if (pageFormat == null) {
        pageFormat = printJob.defaultPage();
      }
      PageFormat pf = printJob.pageDialog(pageFormat);
      if (pf == pageFormat) {
        return;
      } else {
        pageFormat = pf;
      }

      // setup print job.
      // Graph2DPrinter is of type Printable
      printJob.setPrintable(gprinter, pageFormat);

      if (printJob.printDialog()) {
        try {
          printJob.print();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * Action that terminates the application
   */
  protected static class ExitAction extends AbstractAction {
    public ExitAction() {
      super("Exit");
    }

    public void actionPerformed(ActionEvent e) {
      System.exit(0);
    }
  }

  /**
   * Action that saves the current graph to a file in YGF format.
   */


  protected class SaveAction extends AbstractAction {
    JFileChooser chooser;

    public SaveAction() {
      super("Save...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        URL url = view.getGraph2D().getURL();
        if (url != null && "file".equals(url.getProtocol())) {
          try {
            chooser.setSelectedFile(new File(new URI(url.toString())));
          } catch (URISyntaxException e1) {
            // ignore
          }
        }
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
      }
      if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        if(!name.endsWith(".graphml")) {
          name += ".graphml";
        }
        IOHandler ioh = createGraphMLIOHandler();

        try {
          ioh.write(view.getGraph2D(), name);
          //saveVersionData(name);
        } catch (IOException ioe) {
          D.show(ioe);
        }
      }
    }
  }

  /**
   * Action that loads the current graph from a file in GraphML format.
   */
  protected class LoadAction extends AbstractAction {
    JFileChooser chooser;

    public LoadAction() {
      super("Load...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
      }
      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        URL resource = null;
        try {
          resource = chooser.getSelectedFile().toURI().toURL();
        } catch (MalformedURLException urlex) {
          urlex.printStackTrace();
        }
        loadGraph(resource);
        //loadVersionData(chooser.getSelectedFile().toString());
      }
    }
  }

  /**
   * Action that deletes the selected parts of the graph.
   */
  protected static class DeleteSelection extends AbstractAction {
    private final Graph2DView view;

    public DeleteSelection(final Graph2DView view) {
      super("Delete Selection");
      this.view = view;
      URL imageURL = ClassLoader.getSystemResource("versioning/resource/delete.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    }

    public void actionPerformed(ActionEvent e) {

      view.getGraph2D().removeSelection();
      view.getGraph2D().updateViews();
      System.out.println("delete!");
    }
  }

  /**
   * Action that resets the view's zoom level to <code>1.0</code>.
   */
  protected class ResetZoom extends AbstractAction {
    public ResetZoom() {
      super("Reset Zoom");
      final URL imageURL = ClassLoader.getSystemResource("versioning/resource/zoomOriginal.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.putValue(Action.SHORT_DESCRIPTION, "Reset Zoom");
    }

    public void actionPerformed( final ActionEvent e ) {
     view.setZoom(1);
      // optional code that adjusts the size of the
      // view's world rectangle. The world rectangle
      // defines the region of the canvas that is
      // accessible by using the scroll bars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that applies a specified zoom level to the view.
   */
  protected class Zoom extends AbstractAction {
    double factor;

    public Zoom(double factor) {
      super("Zoom " + (factor > 1.0 ? "In" : "Out"));
      URL imageURL;
      if (factor > 1.0d) {
        imageURL = ClassLoader.getSystemResource("versioning/resource/zoomIn.png");
      } else {
        imageURL = ClassLoader.getSystemResource("versioning/resource/zoomOut.png");
      }
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.putValue(Action.SHORT_DESCRIPTION, "Zoom " + (factor > 1.0 ? "In" : "Out"));
      this.factor = factor;
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      // optional code that adjusts the size of the
      // view's world rectangle. The world rectangle
      // defines the region of the canvas that is
      // accessible by using the scroll bars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that fits the content nicely inside the view.
   */
  protected class FitContent extends AbstractAction {
    public FitContent(final Graph2DView view) {
      super("Fit Content");
      URL imageURL = ClassLoader.getSystemResource("versioning/resource/zoomFit.png");
      if (imageURL != null) {
        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
      }
      this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
    }

    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }

}