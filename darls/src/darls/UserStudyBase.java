package darls;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;


import darls.Darls.MyBackgroundRenderer;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeRealizer;

abstract class UserStudyBase {

	static final int NUMBER_OF_FACTORS = 3; //could be 4 (last one is version set)
	boolean animation_is_playing = false;
	static final String str_study_new = "selecting_new";
	static final String str_study_moved = "selecting_moved";
	static final String str_user_studies[] = {str_study_new,str_study_moved};
	static final int COMPARE_ACTION_KEY_CODE = KeyEvent.VK_LEFT;
	static final int SUBMIT_ACTION_KEY_CODE = KeyEvent.VK_RIGHT; 
	
	protected int participant;
	protected int current_trial = 0;
	protected String study = null;

	protected Darls darls = null;
	protected Graph2DView view = null;
	protected Graph2DView left_view = null;

	
	protected JFrame frame = null;
	
	abstract protected void setRight();
	abstract protected void setLeft();
	abstract protected void submit();
	
	abstract void onClearSelectionClick();
	abstract void onSeePreviousRelease();
	abstract void onSeePreviousPress();
	abstract void resultSubmitted(boolean trial_interrupted);
	//abstract protected void give_up_on_trial();
	
	public UserStudyLevel1Log userStudyLevel1Log = null;
	public UserStudyLevel2Log userStudyLevel2Log = null;
	
	final static protected URL audioURL_success = ClassLoader.getSystemResource("versioning/resource/success.wav");
	final static protected URL audioURL_failure = ClassLoader.getSystemResource("versioning/resource/failure.wav");
	final static protected URL audioURL_interrupt = ClassLoader.getSystemResource("versioning/resource/interrupt.wav");

	UserStudyBase(Darls _darls, int _participant, int _current_trial, String _study)
	{
		System.out.println("darls="+darls+",_darls="+_darls);

		init_darls_pointers(_darls);
		participant = _participant;
		current_trial 	= _current_trial;
		study = _study;
		
		init_experiment_animation_durations();
	}

	private void init_experiment_animation_durations() 
	{
	
		//for new study use the following
		if (str_study_new.equals(study))
		{
			  darls.duration_fade_in_comparison = 2000;
			  darls.duration_fade_out_comparison = 2000;
			  darls.duration_morph_comparison = 2000;	
		}
	}
	
	protected void init_darls_pointers(Darls _darls)
	{
		darls = _darls;
		view = _darls.view;
		left_view = _darls.left_view;		
		frame = _darls.frame;
	}
	public static void fix_for_color_blind(List<Graph2D> graphs)
	{
		for (int i = 0; i < graphs.size(); i++)
		{
			Graph2D graph = graphs.get(i);
			Node [] node_array = graph.getNodeArray();
			for (int j = 0; j < node_array.length; j++)
			{
				Node n = node_array[j];
				NodeRealizer nr = graph.getRealizer(n);
				nr.setFillColor2(Color.YELLOW); //second color is none
				nr.setFillColor(Color.YELLOW); //second color is none
				nr.setLineColor(Color.BLACK);
				//nr.getLabel().setTextColor(Color.WHITE); //this is dimming the text color

				
				//shift COMP 3412 (hardcoded)
				if (nr.getLabelText().equals("COMP 3412"))
				{
					double x = nr.getX();
					double y = nr.getY();
					x-=20;
					y+=20;
					nr.setX(x);
					nr.setY(y);
				}
			}
			graph.updateViews();
		}
	}

}


class UserStudy extends UserStudyBase
{
	boolean message_box_is_being_displayed = false; //to prevent keyboard events while the message is displayed
	
	final int trial_timeout = 1000*120; //in milliseconds (2 mins)
	final int visual_response_timeout = 125; //in milliseconds
	final int erase_memory_timeout = 5000; //in milliseconds
	
	final Graph2D empty_graph = new Graph2D();	
	


	public int attempt_no = 0;
	
	//private long attempt_time;
	//private long trial_time;
	
	//private long init_time;
	
	private int trial_since_started = 0;
	
	//the following three variables are used in single view condition
	private Graph2D single_view_graph1 = null;
	private Graph2D single_view_graph2 = null;
	
	private int single_view_left_index = -1;
	private int single_view_right_index = -1;
	private Graph2DView dummy_view = null;
	
	//boolean completed
	//we want same randomization for all graphs, therefore we dont do it this way anymore
	//final int seed = 19810306;
	
	//private Random random_dir_choice = null;
	//private Random random_shuffle = null;
	
	private Random versions_set_shuffle = null;
	

	//boolean for experiment conditions
	float node_shift_skip_percentage = 0.0f;
	
	//this variable can be removed now because it's meaning intersects with the meaning of node_shift_skip_percentage
	//factors
	 
	
	private boolean difference_map = true;
	private boolean single_view = false;
	private boolean animation = false;		
	private String layout_style = Darls.groupPolicy[1];
	
	private List<List<String>> trials_list = null;
	private List<List<Integer>> between_factors_list = null;

	private List<List<Integer>> binary_sets = null;
	private int current_binary_set = 0; //currently not used
	
	
	private Rectangle max_bbox = null;
	
	protected PilotUserStudyLevel3Log pilot_user_study_level3_log = null;
	
	
	private List<String> [] factors_and_levels = null;
	private int [] independently_counterbalanced = null; //for each factor indicate if cognitive shift between them is allowed or not (-1) not allowed, 1 - allowed
	private List<List<String>> factor_permutations = null;
	
	
	
	//factor level in string format for the pilot study
	static final String str_level_animation_and_underlay = "animation+underlay";
	static final String str_level_animation = "animation";
	static final String str_level_toggling = "toggling";
	static final String str_level_dual_view = "dual_view";
	static final String str_level_underlay = "underlays"; //using underlay graph (difference map)
	static final String str_level_incremental = "incremental";
	static final String str_level_optimal = "optimal";
	static final String str_level_highlight = "highlight";
	static final String str_level_randomization = "randomization";
	
	javax.swing.Timer trial_timer = null;
	long experiment_start_time = 0;
	
	long cummulative_attempt_start_time = 0;
	long last_attempt_start_time = 0;
	
	boolean isSelectMovedStudy = false;
	
	String str_last_within_factor1 = null; //common or new
	String str_last_within_factor2 = null; //dual or single
	String str_last_within_factor3 = null; //dual or single

	CompareGraphs compare_graphs = null;

	
	private void startNewTrialTimer()
	{
		cummulative_attempt_start_time = last_attempt_start_time = System.currentTimeMillis();
		
		/*trial_timer = new javax.swing.Timer(timeout, new ActionListener() 
		{
          public void actionPerformed(ActionEvent e) 
          {
        	  long time_elapsed = System.currentTimeMillis() - cummulative_attempt_start_time; 
        	  
        	  long sec = time_elapsed / 1000;
        	  long sec_remainder = sec % 60;
        	  long min = sec / 60;
        	  
        	  String str_time = min + ":" + (sec_remainder < 10 ? "0"+sec_remainder : sec_remainder); //put zero in front if its less than 10
        	  //str_time = darls.str_right_view_text + str_time;
        	  MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(str_time);
        	  view.updateView();
          }
		});
		trial_timer.start();	*/

		trial_timer.restart();
	}
	
	
	
	UserStudy(Darls _darls, int _participant, int _current_trial, String _study)
	{

		super(_darls,_participant,_current_trial, _study);
		
		init_experiment_keylistener();
			
		initBinarySets(); //initialize binary sets
		//init_time = System.currentTimeMillis(); //initialize timer
		if (single_view)
			init_single_view_condition();
		
		if (study.equals(UserStudy.str_study_moved))
			isSelectMovedStudy = true;
		/*if(isSelectMovedStudy)
			init_seed_for_random_layouter();*/
		
		//disable scrolls
		disable_scrollbars();
		
		generate_trials_and_between_factors_list();
		
		if (isSelectMovedStudy)
			calc_layout_max_bbox_zoom_selecting_moved();
		else
			calc_layout_max_bbox_zoom_selecting_new();

		trial_timer = new javax.swing.Timer(trial_timeout, new ActionListener() 
		{
          public void actionPerformed(ActionEvent e) 
          {
        	  
        	  resultSubmitted(true);
        	  System.out.println("timed out!");
        	  
        	 
          }
		});
		Log.connect_to_db();
		start();
		
	}

	private void init_experiment_keylistener() {
		
		class UserStudyDispatcher implements KeyEventDispatcher {
		    @Override
		    public boolean dispatchKeyEvent(KeyEvent e) 
		    {
		    	if (message_box_is_being_displayed) return false;
		        if (e.getID() == KeyEvent.KEY_PRESSED) 
		        {
		        	if (e.getKeyCode() == COMPARE_ACTION_KEY_CODE)
		        	{
		        		
		        		userStudyLevel1Log.log("COMPARE_ACTION_KEY","PRESSED",null);
		        		
			        		onSeePreviousPress();

		        	}
		        } 
		        else if (e.getID() == KeyEvent.KEY_RELEASED) 
		        {
		        	if (e.getKeyCode() == COMPARE_ACTION_KEY_CODE)
		        	{

		        		userStudyLevel1Log.log("COMPARE_ACTION_KEY","RELEASED",null);
		        		
		        			onSeePreviousRelease();
	
		        		//System.out.println("key released");
		        	}
		        	else if (e.getKeyCode() == SUBMIT_ACTION_KEY_CODE)
		        	{

		        		submit();
		        		userStudyLevel1Log.log("SUBMIT_ACTION_KEY","RELEASED",null);
		        		
		        	}
		        	else if (e.getKeyCode() == KeyEvent.VK_N && e.getModifiers() == 2)
		        	{
		        		resultSubmitted(true); //giving up
		        	}
		        } 
		        /*else if (e.getID() == KeyEvent.KEY_TYPED) 
		        {
		            System.out.println("key typed");
		        }*/
		        return false;
		    }
		};
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new UserStudyDispatcher());
		
		//the same thing but using event maps... doesnt work for released/pressed. only on strokes
		/*AbstractAction action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
			//Action that you want to do.
				System.out.println("coco jamboo");
			}
		};
		frame.getRootPane().getActionMap().put("on_space_stroke", action);
		InputMap im = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		// KeyEvent.VK_F10 or any key that you want.
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "on_space_stroke");
		*/
	}



	private void generate_trials_and_between_factors_list() {
		//generate initial trials list	
		List [] list_array = setup_and_counterbalance_trials(trials_list,between_factors_list, false);	//and	

		trials_list = list_array[0];
		between_factors_list = list_array[1]; 
		
		//add base conditions to it
		/*List<List<String>> l_base_trials = null;
		List<List<Integer>> l_base_between_factors = null;			

		list_array = setup_and_counterbalance_trials(l_base_trials,l_base_between_factors, true);	//and
		
		l_base_trials = list_array[0];
		l_base_between_factors = list_array[1]; 
		
		
		trials_list.addAll(l_base_trials);*/
		
		//fix trials list for incremental
		for (int i = 0; i < trials_list.size();i++)
		{
			List<String> trial_strs = trials_list.get(i);
			String str_layout = trial_strs.get(1);
			if(str_layout.equals(str_level_incremental))
			{
				for (int j = 0; j < darls.graphs.size()-1; j++)
				{
					trial_strs.set(2, j+"");
					i++;
					if (i < trials_list.size()) //if it so happens that last one is incremental, it wouldnt crash here
						trial_strs = trials_list.get(i);
					
					 //trials_list.set(i,)
					//trial_strs.set(j, str_technique);
				}
			}
			
		}
		
		//change the between factor variable for base to 3 (for this study
		/*for (int i  = 0; i < l_base_between_factors.size(); i++)
		{
			List<Integer> entry_in_base = l_base_between_factors.get(i);
			entry_in_base.set(0, 4);
			l_base_between_factors.set(i, entry_in_base);
		}
		between_factors_list.addAll(l_base_between_factors);
		*/
		int i=0;
		for (Iterator iter = trials_list.iterator(); iter.hasNext();)		
			System.out.println((i++)+","+"trials_list="+iter.next());
		for (Iterator iter = between_factors_list.iterator(); iter.hasNext();)
			System.out.println("between_factors="+iter.next());
	}
	
	
	void onClearSelectionClick()
	{
		/*if (isSelectMovedStudy)
		{
			Darls.selectBoth(view.getGraph2D());
	    	//Darls.selectBoth(left_view.getGraph2D());
			
		}
		else
		{*/
	    	Darls.deselectBoth(view.getGraph2D());
	    	Darls.deselectBoth(left_view.getGraph2D());
	    	Darls.deselectBoth(darls.RightBackGroundDrawer.backgroundGraph);
	    	Darls.deselectBoth(darls.LeftBackGroundDrawer.backgroundGraph);
		//}
    	view.updateView();
    	left_view.updateView();
    	System.out.println("Selection cleared");
    	
    	pilot_user_study_level3_log.n_clear_button_clicks++;
	}
	void onSeePreviousPress()
	{
		if (!animation)
			setLeft();
		//else
			//animate();
	}
	
	void onSeePreviousRelease()
	{
		System.out.println("animation:"+animation);
		if (!animation)
			setRight();
		else
			animate();
    	pilot_user_study_level3_log.n_see_previous_button_clicks++;

	}
	
	//a different version of the following two functions can be changed later for other studies
	private int [] define_factor_constraints()
	{
		//0 means cognitive shift allowed,
		//-1 means cognitive shift not allowed
		int [] l = new int[NUMBER_OF_FACTORS];	//list of levels
		l[0] = -1;
		l[1] = -1;
		l[2] = 0;//we only allow cognitive shift between the version sets which is the last attribute		
		return l;
	}
	private List [] define_pilot_study_factors(boolean isBase)
	{
		//populating factors for each levle
		List [] l = new List[NUMBER_OF_FACTORS];	//list of levels		
		
		
		l[0] = new ArrayList();	
		l[0].add(str_level_animation);l[0].add(str_level_underlay);
		
		if (isSelectMovedStudy)
			l[0].add(str_level_animation_and_underlay); //only in the selecting new study
		if (!isSelectMovedStudy)
		{
			l[0].add(str_level_dual_view); //only in the selecting new study
			l[0].add(str_level_toggling);
		}
		
		l[1] = new ArrayList();	
		if (study.equals(UserStudy.str_study_new))
		{
			l[1].add(str_level_optimal);l[1].add(str_level_incremental);
		}
		else if (isSelectMovedStudy)
		{
			l[1].add("22");l[1].add("44");
		}
		
		l[2] = new ArrayList();
		//tasks as factor
		for (int i = 0; i < darls.graphs.size()-1; i++)
		{
			l[2].add(i+"");
		}
		
		if (isBase)
		{
			l[0] = new ArrayList();	
			l[0].add(str_level_highlight);
		}
			
		return l;
	}
	//this for base condition at the end

	
	private List [] setup_and_counterbalance_trials(List<List<String>> l_trials, List<List<Integer>> l_between_factors, boolean isBase) {
		
		//for randomizing versions sets order (4th factor) within the latin square for different participants
		versions_set_shuffle = new Random(participant); 
		
		factors_and_levels = define_pilot_study_factors(isBase);
		independently_counterbalanced = define_factor_constraints();
		
		//outputting factors for each level
		System.out.println("-factors in each level-");
		for (int i = 0; i < factors_and_levels.length; i++)
		{
			for (int j = 0; j < factors_and_levels[i].size(); j++)
			{
				System.out.print(factors_and_levels[i].get(j)+",");
			}
			System.out.println("");
		}
		System.out.println("----permutations----");

		factor_permutations = new ArrayList<List<String>>();
		//calculating factor permutations
		permutate_list_of_lists(factors_and_levels,0,factor_permutations,new ArrayList<String>());

		//outputting permutations
		for (int i = 0; i < factor_permutations.size(); i++)
		{
			for (int j = 0; j < factor_permutations.get(i).size(); j++)
			{
				System.out.print(factor_permutations.get(i).get(j)+",");
			}
			System.out.println("");
		}
		
		//print all combinations
		System.out.println("---------all tasks--------");


		
		//generate unbalanced list of trials
		List<List<String>> l_unbalanced_trials = new ArrayList<List<String>>();			
		for (int i = 0, trial = 0; i < factor_permutations.size(); i++)
		{
			///factors
			List<String> str_factors = new ArrayList<String>();
			for (int j = 0; j < factor_permutations.get(i).size(); j++)
			{
				str_factors.add(factor_permutations.get(i).get(j));
			}
			l_unbalanced_trials.add(str_factors);
		}
		
		//print unbalanced list of trials
		//for (int i = 0; i < l_unbalanced_trials.size(); i++)
		//{
			//System.out.println((""+i)+l_unbalanced_trials.get(i));	
		//}
		
		//balance squares for each factor
		List ballatsqs = new ArrayList<Integer[][]>();
		for (int i = 0; i < factors_and_levels.length; i++)
		{
			List<String> levels = factors_and_levels[i];
			int [][] ballatsq = calcBallatsq(levels.size());
			ballatsqs.add(ballatsq);
		}
			
		
		//we simply added up all these branches in the end to make up l_balanced_trials
		//first we save the unbalanced trials copy for future references
		l_trials = new ArrayList<List<String>>(l_unbalanced_trials);		
		l_between_factors = new ArrayList<List<Integer>>();
		
		l_trials = balance_trials(0,ballatsqs,l_trials,null,l_between_factors);
		
		/*for (int i = 0; i < l_trials.size(); i++)
		{
			//System.out.println((""+i)+l_trials.get(i)+",bfactors:"+l_between_factors.get(i));	
		}*/
		
		List [] returnable = {l_trials,l_between_factors};
		return returnable;
	}

	//this function transposes participant_no key for counterbalancing with respect to current factor taking into account the product of all previous factor levels
	public int transpose(int participant_no, int product_of_levels_of_all_previous)
	{
		int j = 0;
		if (product_of_levels_of_all_previous < 2)
			return participant_no;
		
		for (int i = 0; i < participant_no+1;i++)
		{
			if ((i%product_of_levels_of_all_previous) == 0 && i > 0) 
				j++;
			//System.out.println(i+","+j);
		}
		//System.out.println("answer:"+j);
		return j;
	}
	
	private List<List<String>> balance_trials(int i,List ballatsqs,	List<List<String>> unbalanced_trials,List<Integer> between_factors,List<List<Integer>> between_factors_list) 
	{
		int n_factors = factors_and_levels.length;
		
		if (i < n_factors)
		//if (i < 2)
		{
			int [][] ballatsq = (int[][]) ballatsqs.get(i);
			//i added (participant+i) instead of just participant bececause i want
			//different randomization for each factor for the same particpant
			
			//int dist = i*factors_and_levels[i].size();
			int level_multiples = 1;
			for (int factor = 0; factor < i; factor++)
			{
				level_multiples*=factors_and_levels[factor].size();
			}
			//System.out.println("level_multiples:"+level_multiples);			
			int index = transpose(participant,level_multiples);
			
			//we choose group order from index rather than participant directly because we want the factors to counterbalance as well
			int []	group_order = ballatsq[index % ballatsq.length]; 

			//Added November 27, 2010 to account randomization
			//is cognitive shift allowed for this factor?
			int isAllowed = independently_counterbalanced[i];
			if (isAllowed != -1)
			{
				//group_order = ballatsq[isAllowed % ballatsq.length];
				List<int[]> latin_square_rows = new ArrayList<int[]>();
				for (int a = 0; a < ballatsq.length;a++)
					latin_square_rows.add(ballatsq[a]);				
				
				//shuffle will be different for different participants
				
				Collections.shuffle(latin_square_rows, versions_set_shuffle);
				group_order = latin_square_rows.get(0); //always pick the first one
				independently_counterbalanced[i] = ++isAllowed;
			}


			//the following doesnt work and it's not needed for this experiment
			//for odd squares do 2n x n

			/*if (ballatsq.length % 2 != 0)
			{
				int [][] oddsq = new int[ballatsq.length * 2][ballatsq.length];
				
				//fill first half
				for (int j = 0; j < ballatsq.length;j++)
					for (int k = 0; k < ballatsq[j].length;k++)
						oddsq[j][k] = ballatsq[j][k];
				//fill the rest
				for (int j = 0; j < ballatsq.length;j++)
					for (int k = 0; k < ballatsq[j].length;k++)
						oddsq[ j+ ballatsq.length][k] = ballatsq[j][ballatsq[j].length - k - 1];

				group_order = oddsq[index % oddsq.length];
				
				//print it
				System.out.println("--------------------------");
				for (int j = 0; j < oddsq.length; j++)
				{
					for (int k = 0; k < oddsq[j].length; k++)
					{
						System.out.print(oddsq[j][k]);
					}
					System.out.println();
					}
				System.out.println("old:"+index % ballatsq.length+",new:"+index % oddsq.length);
			}*/
			
			List<List<String>> [] split = new List[group_order.length];			
			int [] b_factors = new int[group_order.length];			

			for (int j = 0; j < group_order.length; j++)
			{
				List<List<String>> balanced_trials_chunk = new ArrayList<List<String>>();
				
				for (int k = 0; k < unbalanced_trials.size(); k++)
				{
					String str_factor = unbalanced_trials.get(k).get(i);
					
					int pos = group_order[j]-1;				

					if (str_factor.equals(factors_and_levels[i].get(pos)))
					{	
						balanced_trials_chunk.add(unbalanced_trials.get(k));
					}
				}

				split[j] = balanced_trials_chunk; 
				b_factors[j] = group_order[j]-1;
			}
			
			List<List<String>> ret = new ArrayList<List<String>>();
			
			for (int x = 0; x < split.length; x++)
			{
				//for between factors
				List<Integer> b_factors_recurse = new ArrayList<Integer>();
				if (between_factors != null)
					b_factors_recurse.addAll(between_factors);
				b_factors_recurse.add(b_factors[x]);
				
				ret.addAll(balance_trials(i+1,ballatsqs,split[x],b_factors_recurse,between_factors_list));					
			}
			return ret;
			
		}
		else
		{
			between_factors_list.add(between_factors);
			return unbalanced_trials;	
		}
	}

	private void permutate_list_of_lists(List<String> [] array_of_lists, int row,  List<List<String>> out_perm, List<String> str_list)
	{
		for (int i = 0; i < array_of_lists[row].size(); i++)
		{			
			List<String> str_list2 = new ArrayList<String>();
			str_list2.addAll(str_list);
			str_list2.add(array_of_lists[row].get(i));

			if (row < array_of_lists.length-1)
				permutate_list_of_lists(array_of_lists,row+1, out_perm, str_list2);
			else
			{					
				//System.out.print(str_list2);
				out_perm.add(str_list2);

			}
		}
	}
	/*private void permutate_list_of_lists(List<String> [] array_of_lists, int i, String out)
	{
		for (int j = 0; j < array_of_lists[i].size(); j++)
		{			
			String out2 = out + (String) array_of_lists[i].get(j);
			if (i < array_of_lists.length-1)
				permutate_list_of_lists(array_of_lists,i+1, out2);
			else
			{					
				System.out.print(out2);
				System.out.println("");
			
			}
		}
	}*/
	
	private void disable_scrollbars() {
		view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		 view.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		 
		 left_view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	 left_view.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	}
	public boolean isSingleView()
	{
		return single_view;
	}
	/*private void init_seed_for_random_layouter()
	{
		random_dir_choice = new Random(seed);
		random_shuffle = new Random(seed);
	}*/
	private void init_single_view_condition()
	{
		//left view should have empty graph
		left_view.setGraph2D(empty_graph);
		dummy_view = new Graph2DView();
	}
	private void clear_left_view() { //used for single vuew condition
		//clear left view
		left_view.setGraph2D(empty_graph);
		darls.clearLeftBackGroundDrawer();
		left_view.updateView();
	}
	private void start() { //clear views before experiment begins
		clear_views();
		
		String newline = System.getProperty("line.separator");
		String message = "LEFT CLICK to select and de-select objects."+newline+"Press the <Right Arrow> key to submit results."+newline+"Click <OK> whenever ready to begin the experiment";
		
		//MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(message);
		//custom title, no icon

				JOptionPane.showMessageDialog(frame,message,"Instruction", JOptionPane.INFORMATION_MESSAGE);
	}

	private void clear_views() {
		darls.clearLeftBackGroundDrawer();
		darls.clearRightBackGroundDrawer();
		
		left_view.setGraph2D(empty_graph);
		left_view.updateView();
		
		view.setGraph2D(empty_graph);
		view.updateView();
	}
	
	void initBinarySets()
	{
		binary_sets = new ArrayList<List<Integer>>();
		binary_sets = fillBinarySets(binary_sets);			
		binary_sets = counterBalanceBinarySets(binary_sets, participant); //successfullly overwritten
	}
	
	private int[] getFalsePositivesAndFalseNegativesSelectingNew(Graph2D graph, boolean isRightGraph) //false for edge related
	{
		int index = isRightGraph ? 1 : 2; //1 for right graph index, 2 for left graph index
		//if (isSelectMovedStudy)
			//index = 0;
		int false_positive_nodes = 0;
		int false_positive_edges = 0;
		
		int false_negative_edges = 0;
		int false_negative_nodes = 0;
		
		List<UUID>[] L = null;
		//check nodes first
		
			FalseObjectsLog fpn_log = new FalsePositiveNodeLog(this,study);
			FalseObjectsLog fnn_log = new FalseNegativeNodeLog(this,study);
			
			L = VNode.find_uuid(darls.v_node_list_1, darls.v_node_list_2);
			
			for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
			{
				Node n = nc.node();
				UUID uuid = VNode.getNodeVid(n, isRightGraph ? darls.v_node_list_2 : darls.v_node_list_1);
				
				if (!L[index].contains(uuid))	
				{
					false_positive_nodes++;
					System.out.println("False positive node:"+ n.toString());
					
					fpn_log.log(n.toString());
				}
			}
			
			for (Iterator iter = L[index].iterator(); iter.hasNext();)
			{
				UUID uuid = (UUID) iter.next();
				boolean found = false;				
				for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
				{
					Node n = nc.node();
					UUID right_uuid = VNode.getNodeVid(n, isRightGraph ? darls.v_node_list_2 : darls.v_node_list_1);
					if (uuid.equals(right_uuid))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					false_negative_nodes++;
					Node fnn = VNode.getNodeFromNodeList(uuid, isRightGraph ? darls.v_node_list_2 : darls.v_node_list_1);
					System.out.println("False negative node:"+ fnn);
					
					fnn_log.log(fnn.toString());
				}
			}
		
			L = VEdge.find_uuid(darls.v_edge_list_1, darls.v_edge_list_2);
			
			FalseObjectsLog fpe_log = new FalsePositiveEdgeLog(this,study);
			FalseObjectsLog fne_log = new FalseNegativeEdgeLog(this,study);
			
			for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				UUID uuid = VEdge.getEdgeVid(e, isRightGraph ? darls.v_edge_list_2 : darls.v_edge_list_1);
				
				if (!L[index].contains(uuid))
				{
					false_positive_edges++;
					System.out.println("False positive edge:"+ e.toString());
					fpe_log.log(e.toString());
				}
			}
			
			for (Iterator iter = L[index].iterator(); iter.hasNext();)
			{
				UUID uuid = (UUID) iter.next();
				boolean found = false;				
				for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next())
				{
					Edge e = ec.edge();
					UUID right_uuid = VEdge.getEdgeVid(e, isRightGraph ? darls.v_edge_list_2 : darls.v_edge_list_1);
					if (uuid.equals(right_uuid))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					false_negative_edges++;
					Edge fne = VEdge.getEdgeFromEdgeList(uuid, isRightGraph ? darls.v_edge_list_2 : darls.v_edge_list_1);
					System.out.println("False negative edge:"+fne.toString());
					fne_log.log(fne.toString());
				}
				//System.out.println("all edges:"+VEdge.getEdgeFromEdgeList(uuid, isRightGraph ? darls.v_edge_list_2 : darls.v_edge_list_1));
				
			}
			
		
		int [] returnable = {false_positive_nodes,false_negative_nodes,false_positive_edges,false_negative_edges};
		
		return returnable;
	}
	
	private int[] getFalsePositivesAndFalseNegativesSelectingMoved(Graph2D graph, boolean isRightGraph) //false for edge related
	{
		int false_positive_nodes = 0;		
		int false_negative_nodes = 0;
		pilot_user_study_level3_log.n_moved_nodes = 0; //reset this value
		
		List<UUID>[] L = null;
		//check nodes first
		
			FalseObjectsLog fpn_log = new FalsePositiveNodeLog(this,study);
			// we also log location difference for false negative nodes
			FalseNegativeNodeMovedLog fnnm_log = new FalseNegativeNodeMovedLog(this,study);
			
			L = VNode.find_uuid(darls.v_node_list_1, darls.v_node_list_2);
			
			
			for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
			{
				Node n = nc.node();
				UUID uuid = VNode.getNodeVid(n, isRightGraph ? darls.v_node_list_2 : darls.v_node_list_1);
				
								
				//see if this is a new node
				if (!L[0].contains(uuid))	
				{
					false_positive_nodes++;
					System.out.println("False positive node (new):"+ n.toString());
					
					fpn_log.log(n.toString());
				}
				else
				{
					//see if common but position changed
					Node [] nn = VNode.getNodesFromVid(uuid, darls.v_node_list_1, darls.v_node_list_2);
					Graph2D g0 = (Graph2D) nn[0].getGraph();
					Graph2D g1 = (Graph2D) nn[1].getGraph();
					NodeRealizer nr0 = g0.getRealizer(nn[0]);
					NodeRealizer nr1 = g1.getRealizer(nn[1]);
					double x0 = nr0.getX();
					double y0 = nr0.getY();
					
					double x1 = nr1.getX();
					double y1 = nr1.getY();
					
					 if (x0 == x1 && y0 == y1)
					 {
							false_positive_nodes++;
							System.out.println("False positive node(didn't shift):"+ n.toString());
							
							fpn_log.log(n.toString());
					 }

				}
			}
			
			for (Iterator iter = L[0].iterator(); iter.hasNext();)
			{
				UUID uuid = (UUID) iter.next();
				//see if positions match
				Node [] nn = VNode.getNodesFromVid(uuid, darls.v_node_list_1, darls.v_node_list_2);
				Graph2D g0 = (Graph2D) nn[0].getGraph();
				Graph2D g1 = (Graph2D) nn[1].getGraph();
				NodeRealizer nr0 = g0.getRealizer(nn[0]);
				NodeRealizer nr1 = g1.getRealizer(nn[1]);
				double x0 = nr0.getX();
				double y0 = nr0.getY();
				
				double x1 = nr1.getX();
				double y1 = nr1.getY();
				
				 if (x0 != x1 || y0 != y1)
				 {
					 	//log n_moved_nodes
					 	pilot_user_study_level3_log.n_moved_nodes++;
					 
						boolean found = false;				
						for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
						{
							Node n = nc.node();
							UUID right_uuid = VNode.getNodeVid(n, isRightGraph ? darls.v_node_list_2 : darls.v_node_list_1);
							if (uuid.equals(right_uuid))
							{
								//now see if positions match as well
								found = true;
								break;
							}
						}
						if (!found)
						{
							false_negative_nodes++;
							Node fnn = VNode.getNodeFromNodeList(uuid, isRightGraph ? darls.v_node_list_2 : darls.v_node_list_1);
							System.out.println("False negative node:"+ fnn);
							
							fnnm_log.log(fnn.toString(),x0,y0,x1,y1);
						}
				}
			}
		
		int [] returnable = {false_positive_nodes,false_negative_nodes,0,0};
		
		return returnable;
	}

	public void submit()
	{
		if (trial_since_started > 0)
		{
			resultSubmitted(false);
			System.out.println("Not first time");
		}
		else
		{
			//experiment start time logging (in level1) assumes that experiment was not interrupted!
			experiment_start_time = System.currentTimeMillis();
			run();	//this is first time so we just run
			System.out.println("First time");
		}
	}
	
	
	/*
	private void erase_participants_memory()
	{
		clear_views();
		javax.swing.Timer erase_memory_timer = new javax.swing.Timer(erase_memory_timeout, new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  run();
	          }
	       });
		erase_memory_timer.setRepeats(false);
		erase_memory_timer.restart();
		
	}*/


	private void reload_graphs_from_disk() {
		if (!darls.uml_diag_in_versions)				
			darls.load_graph_version_data(darls.last_resource,darls.last_str_dir,darls.last_str_file);
		else
			darls.load_uml_verison_data(darls.last_resource,darls.last_str_dir,darls.last_str_file);
	}
	
	
	
	void resultSubmitted(final boolean trial_interrupted) //interrupted either due to give up or other reasons
	{
		//stop animation first
		if (compare_graphs != null)
			compare_graphs.player.stop();
		compare_graphs = null;
		
		System.out.println("result submitted");
		//log.log_trial();
		//reload the graph and then run
		
		
		//Graph2D left_graph = left_view.getGraph2D();
		Graph2D right_graph = view.getGraph2D();



		//NodeCursor leftSelectedNodes = left_graph.selectedNodes();
		//EdgeCursor leftSelectedEdges = left_graph.selectedEdges();
		
		//the following will work for diagrams and not class diagrams
		int [] false_objects = null; 
		if (isSelectMovedStudy) 
			false_objects = getFalsePositivesAndFalseNegativesSelectingMoved(view.getGraph2D(), true);
		else
			false_objects = getFalsePositivesAndFalseNegativesSelectingNew(view.getGraph2D(), true);
		

		System.out.println("False negative nodes in right:"+false_objects[1]);
		System.out.println("False positive nodes in right:"+false_objects[0]);
		
		System.out.println("False negative edges in right:"+false_objects[3]);
		System.out.println("False positive edges in right:"+false_objects[2]);
		System.out.println("MOVED NODES:"+pilot_user_study_level3_log.n_moved_nodes);

		javax.swing.Timer paint_timer = null;
		
		if ((false_objects[1] > 0 || false_objects[0] > 0 || 
				false_objects[2] > 0 || false_objects[3] > 0) && !trial_interrupted)
		{
			//oopsie
			//paint red
			MyBackgroundRenderer.newInstance(view, Color.red, false);
        	view.updateView();

        	paint_timer = new javax.swing.Timer(visual_response_timeout, new ActionListener() {
		          public void actionPerformed(ActionEvent e) {
		        	//log
		        	pilot_user_study_level3_log.log_time_and_success("no");
		        	pilot_user_study_level3_log.insert_new_entry_into_db(); //log unsuccessful attempt
		        	last_attempt_start_time =  System.currentTimeMillis();; //reset last attempt time
		        	attempt_no++;
		        	pilot_user_study_level3_log.n_clear_button_clicks = 0; //has to be cleared for each attempt
		        	pilot_user_study_level3_log.n_see_previous_button_clicks = 0; //has to be cleared for each attempt
		        	
		        	//play the sound
		        	PlaySound sound = new PlaySound(audioURL_failure);
		        	sound.start();
		        	//draw the failure
	        	  	MyBackgroundRenderer.newInstance(view, Color.white, true).setText("Try again!");
		        	view.updateView();
		        	System.out.println("bad!");
		        	//update cummulative time and attempt time
		          }
		       });
        	paint_timer.setRepeats(false);
        	paint_timer.restart();
		}
		else
		{
			trial_timer.stop();
			//paint green and run
			MyBackgroundRenderer.newInstance(view, trial_interrupted ? Color.YELLOW : Color.GREEN , false);
        	view.updateView();
        	
			paint_timer = new javax.swing.Timer(visual_response_timeout, new ActionListener() {
		          
				public void actionPerformed(ActionEvent e) {
					//log
		        	pilot_user_study_level3_log.log_time_and_success(trial_interrupted ? "interrupts" : "yes");
		        	pilot_user_study_level3_log.insert_new_entry_into_db(); ///log successful attempt
		        	attempt_no = 0;	//reset attempt because it was successful
		        	
		        	if (trial_interrupted)
		        	{
						//play the sound
						PlaySound sound = new PlaySound(audioURL_interrupt);
			        	sound.start();		
						
			        	//draw the failure
		        	  	MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText("Timed out");
		        	}
		        	else
		        	{
						//play the sound
						PlaySound sound = new PlaySound(audioURL_success);
			        	sound.start();		
						
			        	//draw the failure
		        	  	MyBackgroundRenderer.newInstance(view, Color.white, true).setText("Good");
		        	}
		        	view.updateView();
		        	System.out.println("good!");
		        	

		        	run();
		        	
		        	//erase_participants_memory();
		        	
		          }
		       });
			paint_timer.setRepeats(false);
			paint_timer.restart();
		}
	
		//log node and edge data
		//log number of false positives and false negatives
		pilot_user_study_level3_log.n_false_negative_nodes = false_objects[1];
		pilot_user_study_level3_log.n_false_positive_nodes = false_objects[0];
		
		pilot_user_study_level3_log.n_false_negative_edges = false_objects[3];
		pilot_user_study_level3_log.n_false_positive_edges = false_objects[2];
				
		//log number of common, deleted and new nodes
		List<UUID>[] Nodes = VNode.find_uuid(darls.v_node_list_1, darls.v_node_list_2);
		List<UUID>[] Edges = VNode.find_uuid(darls.v_node_list_1, darls.v_node_list_2);
		
		pilot_user_study_level3_log.n_common_nodes = Nodes[0].size(); 
		pilot_user_study_level3_log.n_common_edges = Edges[0].size();
		
		pilot_user_study_level3_log.n_new_nodes = Nodes[0].size(); 
		pilot_user_study_level3_log.n_new_edges = Edges[0].size();
		
		pilot_user_study_level3_log.n_new_nodes = Nodes[1].size(); 
		pilot_user_study_level3_log.n_new_edges = Edges[1].size();
		
		pilot_user_study_level3_log.n_deleted_nodes = Nodes[2].size(); 
		pilot_user_study_level3_log.n_deleted_edges = Edges[2].size();
		
		//log number of nodes and number of edges
		pilot_user_study_level3_log.n_nodes_left = darls.v_node_list_1.size();
		pilot_user_study_level3_log.n_edges_left = darls.v_edge_list_1.size();
		
		pilot_user_study_level3_log.n_nodes_right = darls.v_node_list_2.size();
		pilot_user_study_level3_log.n_edges_right = darls.v_edge_list_2.size();
		
		//log number of selected nodes and edges
		pilot_user_study_level3_log.n_selected_nodes = right_graph.selectedNodes().size();
		pilot_user_study_level3_log.n_selected_edges = right_graph.selectedEdges().size();
		
		//log attempt
		pilot_user_study_level3_log.attempt_no = new Integer(attempt_no);
		
		//we also need to reset the editmode so that those bends are gone			
		
	}
	private void run()
	{

		//initialize pilot user study level 3 log
		pilot_user_study_level3_log = new PilotUserStudyLevel3Log(this,study);
		userStudyLevel1Log = new UserStudyLevel1Log(this,study);
		userStudyLevel2Log = new UserStudyLevel2Log(this,study);
		
		trial_since_started++;
		reload_graphs_from_disk();
		fix_for_color_blind(darls.graphs);
		 
		if (current_trial < trials_list.size())
		{			
			difference_map = false;
			animation = false;
			
			List<String> trial_strs = trials_list.get(current_trial);
			
			System.out.println("current trial:"+current_trial+","+trial_strs);
			
			//final String str_task = trial_strs.get(0);
			final String str_within_factor1 = trial_strs.get(0);
			final String str_within_factor2 = trial_strs.get(1);
			final String str_within_factor3 = trial_strs.get(2);
			
			
			//log factors
			//pilot_user_study_level3_log.within_factor1 = new String(str_task);
			//pilot_user_study_level3_log.within_factor1 = new String(str_task);
			pilot_user_study_level3_log.within_factor1 =  new String(str_within_factor1);
			pilot_user_study_level3_log.within_factor2 =  new String(str_within_factor2);
			pilot_user_study_level3_log.within_factor3 =  new String(str_within_factor3);
			
			//log between factors
			//List<Integer> current_trial_between_factors = between_factors_list.get(current_trial);
			List<Integer> current_trial_between_factors = between_factors_list.get(0);
			pilot_user_study_level3_log.between_factor1 =  new Integer(current_trial_between_factors.get(0));
			pilot_user_study_level3_log.between_factor2 =  new Integer(current_trial_between_factors.get(1));
			pilot_user_study_level3_log.between_factor3 =  new Integer(current_trial_between_factors.get(2));
			//pilot_user_study_level3_log.between_factor4 =  new Integer(current_trial_between_factors.get(3)); //useless for the pilot
			
			//log basics
			pilot_user_study_level3_log.participant_no = new Integer(participant);
			pilot_user_study_level3_log.trial_no = new Integer(current_trial);
			


			//view
			single_view = str_within_factor1.equals(str_level_toggling) || str_within_factor1.equals(str_level_animation) || str_within_factor1.equals(str_level_highlight);			
				
			if (single_view) 
				init_single_view_condition();
			else
				clear_views();
			
			//technique

			
			//task
			int version_set = Integer.parseInt(str_within_factor3);
			
			
			if (str_within_factor1.equals(str_level_underlay) || str_within_factor1.equals(str_level_animation_and_underlay))
			{
					difference_map = true;
			}
			else if(str_within_factor1.equals(str_level_animation))
			{
				animation = true;
			}
			
			if (str_within_factor1.equals(str_level_animation_and_underlay))
			{
				animation = true;
			}
			
			
			layout_style = darls.groupPolicy[2];
			
			if (study.equals(UserStudy.str_study_new))
			{
				
				if(str_within_factor2.equals(str_level_incremental))
				{
					layout_style = darls.groupPolicy[1];				
				}			
				else //none or base
				{
					
				}
			}
			else if (isSelectMovedStudy)
			{
					node_shift_skip_percentage = (100-Integer.parseInt(str_within_factor2))/100.f; 
					System.out.println("Randmozing with skip of:" +node_shift_skip_percentage);
				
			}
				
			
			if (!single_view)
			{
				//for this pilot we assume that task points to the previous version
				//so only tasks in the sequence are considered
				load_graphs(version_set,version_set+1);
				System.out.println("Loaded dual view graph");

			}
			else
			{
				//for this pilot we assume that task points to the previous version
				//so only tasks in the sequence are considered
				load_single_view_graphs(version_set,version_set+1);
				System.out.println("Loaded single view graph");
			}
			
			//task
			//if (str_task.equals(str_level_new))
			/*if (study.equals(str_study_new))
			{
				//darls.str_right_view_text = "Select all nodes and edges appearing ONLY in the newer version of the diagram";
				isSelectMovedStudy = false;
			}
			else
			{
					//darls.str_right_view_text = "Select all nodes and edges appearing in BOTH versions of the diagram";
					isSelectMovedStudy = true;
					Darls.selectBoth(view.getGraph2D());
					//if a switch occurs maybe should display a message box or something
			}*/
			
			//highlighting
			//this must be done after load graphs are called, thats why i call it here
			if(str_within_factor1.equals(str_level_highlight))
			{	
				highlight();
			}
			
			String message = null;
			message_box_is_being_displayed = false;
			/*final String next_factor_1_level_1_message = "Task:\nSelect objects that are new in the new version.\nAll objects are NOT SELECTED by default.\n<Clear> button deselects all.\n"; 
			final String next_factor_1_level_2_message = "Task:\nSelect objects that are common to both versions.\nAll objects are SELECTED by default.\n<Clear> button selects all.\n"; 
			final String next_factor_2_level_1_message = "Left view is disabled. Use <Previous> button on top\n to toggle between the two versions.\n";
			final String next_factor_2_level_2_message = "Dual view mode. <Previous> button doesn't work\n";
			
			boolean change_in_within_factor1 = str_last_within_factor1 != null && !str_last_within_factor1.equals(str_within_factor1) || str_last_within_factor1 == null; //last one after || in case it's the first trial
			boolean change_in_within_factor2 = str_last_within_factor2 != null && !str_last_within_factor2.equals(str_within_factor2) || str_last_within_factor2 == null; //last one after || in case it's the first trial
			//boolean change_in_within_factor3 = str_last_within_factor3 != null && !str_last_within_factor3.equals(str_within_factor2) || str_last_within_factor3 == null; //last one after || in case it's the first trial
			if (change_in_within_factor1 ^ change_in_within_factor2)
			{
				if (change_in_within_factor1)
				{
					if(study.equals(str_study_new))
						message = next_factor_1_level_1_message;
					else
						message = next_factor_1_level_2_message;
					System.out.println("change between tasks");
				}					
				else
				{
					if(str_within_factor1.equals(str_level_toggling))
						message = next_factor_2_level_1_message; 
					else
						message = next_factor_2_level_2_message;
					System.out.println("change between view styles");
				}
				message_box_is_being_displayed = true;
			}
			else if(change_in_within_factor1 && change_in_within_factor2)
			{
				if (message == null)
					message = "";
				
				if(study.equals(str_study_new))
				{
					message += next_factor_1_level_1_message;
				}
				else
				{
					message += next_factor_1_level_2_message;
				}
				
				if(str_within_factor1.equals(str_level_toggling))
				{
					message += next_factor_2_level_1_message;
				}
				else
				{
					message += next_factor_2_level_2_message;
				}
				message_box_is_being_displayed = true;
				
				System.out.println("change between tasks and view styles");
			}			
			else
			{				
				System.out.println("no change in first two factors");
			}
			if (message != null)
			{
				JOptionPane.showMessageDialog(frame,message,"Instruction", JOptionPane.INFORMATION_MESSAGE);
				message_box_is_being_displayed = false;
			}*/
			
			final String dual_view_message = "Dual-view mode. Pressing the <Left Arrow> key is not required in this mode and will not work\n";
			String single_view_message = "Single-view mode. Press the <Left Arrow> key to see previous version of the diagram\n";
			final String no_underlay_graphs_message = "There will be no underlay graphs displayed until you see this message again\n";

			boolean last_was_dual_view = str_last_within_factor1 != null && (str_last_within_factor1.equals(str_level_dual_view) ||  str_last_within_factor1.equals(str_level_underlay));
			boolean current_is_dual_view = str_last_within_factor1 != null && (str_within_factor1.equals(str_level_dual_view) ||  str_within_factor1.equals(str_level_underlay));
			
			boolean last_was_underlay = str_last_within_factor1 != null &&  str_last_within_factor1.equals(str_level_underlay);
			boolean current_is_underlay = str_last_within_factor1 != null &&  str_within_factor1.equals(str_level_underlay);
			
			if (isSelectMovedStudy && str_within_factor1.equals(str_level_animation_and_underlay))
			{
				single_view_message = "Dual-view mode with animation. Press the <Left Arrow> key to animate the changes\n";
			}
				
			if (last_was_dual_view && !current_is_dual_view)
			{
				message = single_view_message;
			}
			else if(!last_was_dual_view && current_is_dual_view)
			{
				message = dual_view_message;
				if (!current_is_underlay && last_was_underlay)
					message+=no_underlay_graphs_message;
			}
			else if (!current_is_underlay && last_was_underlay)
			{
				message=no_underlay_graphs_message;
			}
			//if its the first time
			else if (str_last_within_factor1 == null)
			{
				
				if (str_within_factor1.equals(str_level_dual_view) ||  str_within_factor1.equals(str_level_underlay))
				{
						message = dual_view_message;
						if (!str_within_factor1.equals(str_level_underlay))
						{
							message+=no_underlay_graphs_message;
						}
				}
				else
					message = single_view_message;
			}

			
			if (message != null)
			{
				JOptionPane.showMessageDialog(frame,message,"Instruction", JOptionPane.INFORMATION_MESSAGE);
				message_box_is_being_displayed = false;
			}
			
			/*//warn about no underlays and highlight
			String message2 = null;
			if (!str_last_within_factor1.equals(str_level_underlay) && str_within_factor1.equals(str_level_underlay))
			{
				message2 = "Underlay diagrams will not be displayed until you see them or this message again";				
			}
			else if (!str_last_within_factor1.equals(str_level_highlight) && str_within_factor1.equals(str_level_highlight))
			{
				message2 = "In this last part of the experiment the differences will be highlighted for you";
			}*/
				
			
			//animate once dual view + animation condition is loaded
			if (str_within_factor1.equals(str_level_animation_and_underlay))
					animate();
			startNewTrialTimer();
			
			//increment current trial			
			current_trial++;
			str_last_within_factor1 = str_within_factor1;
			str_last_within_factor2 = str_within_factor2; //used to display message box
			str_last_within_factor3 = str_within_factor3; //used to display message box
			
		}

		else
		{
			System.out.println("Experiment over");
			JOptionPane.showMessageDialog(frame,"Experiment is over","Finished!", JOptionPane.PLAIN_MESSAGE);
			
			//delete all practise participant data
			/*if (participant == darls.PRACTICE_PARTICIPANT)
			{
				//str_table_names
				//str_database_name
				String query = "";
				for (int i = 0; i < Log.str_table_names.length; i++)
				{
					query += "DELETE FROM " +Log.str_table_names[i]+" WHERE participant_no = " + darls.PRACTICE_PARTICIPANT+ ";\n";
				}
				System.out.println("query:"+query);
	            //int val = st.executeUpdate(query);
	            System.out.println("deleted tables");
		            	
			}*/
			Log.disconnect_from_db();				
		}
	}



	private void highlight()
	{
		compare_graphs =  new CompareGraphs(darls);
		compare_graphs.setNodesHighlight(true, !isSelectMovedStudy); //wrong now, because the called function highlights common nodes, not moved
		compare_graphs.setEdgesHighlight(true, !isSelectMovedStudy);
	}
	
	private void animate()
	{
		
		
		if (!animation_is_playing)
		{
			//compare_graphs.player.setBlocking(true);
			compare_graphs =  new CompareGraphs(darls);
			compare_graphs.animateComparison();
		}
		else
		{
			compare_graphs.player.stop();
			compare_graphs.animateComparison();
		}
			
	}
	
	/*private void run()
	{
		System.out.println("run");
		trial_since_started++;

		reload_graphs_from_disk();

		//t.start();
		
		
		//loading
		if (current_binary_set < binary_sets.size())
		{
			List<Integer> set = binary_sets.get(current_binary_set);
			

			if (!single_view)
			{
				load_graphs(set.get(0),set.get(1));
				System.out.println("Loaded dual view graph");

			}
			else
			{
				load_single_view_graphs(set.get(0),set.get(1));
				System.out.println("Loaded single view graph");
			}
			
			
			System.out.println("Set:"+set.get(0)+","+set.get(1));
			current_binary_set++;
			
			

		}
		else
		{
			System.out.println("Experiment over");
			JOptionPane.showMessageDialog(frame,"Experiment is over","Finished!", JOptionPane.PLAIN_MESSAGE);
			log.disconnect_from_db();				
		}
	}*/

	private List<List<Integer>> fillBinarySets(List<List<Integer>> binary_sets) {
		for (int i = 1; i < darls.graphs.size(); i++)
		{
			List<List<Integer>> temp_sets = calcComparisonSets(i);
			
			for (Iterator iter = temp_sets.iterator(); iter.hasNext();)
			{
				List<Integer> set = (List<Integer>) iter.next();
				binary_sets.add(set);
				//System.out.println("Set:"+set.get(0)+","+set.get(1));
			}
		}
		return binary_sets;
	}
	

	//the same thing as the original function in Darls but changes to work with single view condition
	/*private void setMaxZoomLevelSingleView()
	{
		Rectangle union = graphs.get(0).getBoundingBox();
		
		for (int i = 1; i < graphs.size(); i++)
		{
			Graph2D g = graphs.get(i);
			Rectangle temp_bbox = g.getBoundingBox();
			union = union.union(temp_bbox);
			
		}
		dummy_view.zoomToArea(union.x, union.y, union.width, union.height);
		view.zoomToArea(union.x, union.y, union.width, union.height);
		view.updateView();
		dummy_view.updateView();	
	}*/
	//load graphs for experiment trial
	public void setLeft() //this for single view
	{
		if (single_view)
		{
			set_vlists(single_view_left_index,single_view_right_index);
			dummy_view.setGraph2D(single_view_graph2);
			dummy_view.updateView();
			
			view.setGraph2D(single_view_graph1);
			
			darls.RightBackGroundDrawer = darls.init_background_drawer(darls.RightBackGroundDrawer,view,dummy_view,difference_map);
			//setMaxZoomLevelSingleView();
			set_max_bbox_zoom_single_view();
		}
		else
		{
			System.out.println("Didn't set the left graph because single_view is disabled");
		}
	}
	public void setRight()
	{
		if (single_view)
		{
			set_vlists(single_view_right_index,single_view_left_index);
			dummy_view.setGraph2D(single_view_graph1);
			dummy_view.updateView();
			
			view.setGraph2D(single_view_graph2);

			darls.RightBackGroundDrawer = darls.init_background_drawer(darls.RightBackGroundDrawer,view,dummy_view,difference_map);
			//setMaxZoomLevelSingleView();
			set_max_bbox_zoom_single_view();
			set_vlists(single_view_left_index,single_view_right_index);
		}
		else
		{
			System.out.println("Didn't set the right graph because single_view is disabled");
		}
	}
	
/*	private void reverse_vlists()
	{
		List <VNode> temp_vnode = v_node_list_1;
		List <VEdge> temp_vedge = v_edge_list_1;
		
		v_node_list_1 = v_node_list_2;
		v_edge_list_1 = v_edge_list_2;
		
		v_node_list_2 = temp_vnode;
		v_edge_list_2 = temp_vedge;	

	}*/
	private void set_vlists(int left_index, int right_index)
	{
		if (!darls.uml_diag_in_versions)
		{
			darls.v_node_list_1 = (List <VNode>)darls.v_nodes.get(left_index);
			darls.v_edge_list_1 = (List <VEdge>)darls.v_edges.get(left_index);
			
			darls.v_node_list_2 = (List <VNode>)darls.v_nodes.get(right_index);
			darls.v_edge_list_2 = (List <VEdge>)darls.v_edges.get(right_index);
		}
		else
		{
			System.out.println("There is no need to call this because UML Diagrams are being used");
		}
	}
	private void load_single_view_graphs(int left_index, int right_index)
	{
		//moved it from the bottom of the function November 25, 2010
		darls.str_right_view_text = ""; //put timer here later instead?
		MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(darls.str_right_view_text);	
		
		darls.clear_before_load_view();
		single_view_graph1 = darls.graphs.get(left_index);
		single_view_graph2  = darls.graphs.get(right_index);

		//clear_views();

		
		single_view_left_index = left_index;
		single_view_right_index = right_index;

		
		
		//set single view graphs (to be used for switching later
		left_view.setGraph2D(single_view_graph1);
		view.setGraph2D(single_view_graph2);
		
		set_vlists(single_view_left_index,single_view_right_index);
		
		darls.reLayoutAction(layout_style);
		single_view_graph1 = left_view.getGraph2D();
		single_view_graph2 = view.getGraph2D();
		
		/*Graph2D [] graph = reLayoutExperiment(layout_style, single_view_graph1, single_view_graph2);
		single_view_graph1 = graph[0];
		single_view_graph2 = graph[1];
		*/
		
		if (isSelectMovedStudy)
		{
			randomize_two_graphs(single_view_graph1, single_view_graph2,left_index, right_index); //left index->version set->seed
		}

		//clear left
		clear_left_view();
		setRight();
		//str_right_view_text = "Version " + (right_index + 1);

		
	}
	
	
	private void load_graphs(int left_index, int right_index) {

		darls.clear_before_load_view();
		
		Graph2D left_graph = darls.graphs.get(left_index);
		Graph2D right_graph = darls.graphs.get(right_index);
		
		clear_views();
		
		//setViewToGraph(right_graph,view);
		//setViewToGraph(left_graph,left_view);
		//						
		view.setGraph2D(right_graph);
		left_view.setGraph2D(left_graph);
		
		
	  	//str_left_view_text = "Version " + (left_index + 1); 
	  	//str_right_view_text = "Version " + (right_index + 1);
	  	
		darls.str_left_view_text = "";	//put timer here later 
		darls.str_right_view_text = "";

	  	MyBackgroundRenderer.newInstance(left_view, Color.WHITE, true).setText(darls.str_left_view_text);
	  	MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(darls.str_right_view_text);
		
		set_vlists(left_index,right_index);
		
		//reLayoutAction(layout_style); //complete relayout, ignore groups
		//Graph2D [] g = reLayoutExperiment(layout_style,left_graph,right_graph);
		darls.reLayoutAction(layout_style); 
		if (isSelectMovedStudy)
		{
			randomize_two_graphs(left_graph, right_graph,left_index, right_index); //left index -> version set -> seed
		}
		//view.setGraph2D(g[1]);
		//left_view.setGraph2D(g[0]);
		
		darls.LeftBackGroundDrawer=darls.init_background_drawer(darls.LeftBackGroundDrawer,left_view,view,difference_map);
		darls.RightBackGroundDrawer = darls.init_background_drawer(darls.RightBackGroundDrawer,view,left_view,difference_map);
		//setMaxZoomLevel();
		System.out.println("max bbox:"+max_bbox);
		set_max_bbox_zoom(); //instead of max zoom which only considers graphs as if they were untouched, this method sets max zoom considering all possible layout combinations as well

	}
	//the older function considers all permutations of version sets
	//but since for the pilot we only assume only versions in the sequence, e.g. 1->2, 2->3...etc
	//we can just consider graphs in those sequences and make zoom level higher potentially
	private void calc_layout_max_bbox_zoom_selecting_moved() //sets max bbox considering all possible combinations of layouts in the run from binary_sets
	{

		//this is for complete layout
		//for (int i = 0; i < binary_sets.size();i++) //old statement Nov 24, 2010
		for (int i = 0; i < darls.graphs.size()-1;i++)
		{
			//jay stands for two layout styles [1] and [2]
			for (int j = 0; j < factors_and_levels[1].size() ; j++) //for selecting new study there is no incremental
			{
				//we do it for each randomization factor
				node_shift_skip_percentage = (100-Integer.parseInt(factors_and_levels[1].get(j)))/100.f; 
				
				reload_graphs_from_disk();
				fix_for_color_blind(darls.graphs);
				
				int left_index = i;
				int right_index = i+1;
				
				
				Graph2D left_graph = darls.graphs.get(left_index);
				Graph2D right_graph = darls.graphs.get(right_index);
				
				view.setGraph2D(right_graph);
				left_view.setGraph2D(left_graph);

				
				set_vlists(left_index,right_index);
				
				if (isSelectMovedStudy)
				{
					darls.reLayoutAction(darls.groupPolicy[2]); //complete relayout, ignore groups
					randomize_two_graphs(left_graph, right_graph,left_index, right_index); //left index -> version set -> seed
					
					view.setGraph2D(right_graph);
					view.updateView();
					
					left_view.setGraph2D(left_graph);
					left_view.updateView();
				}
				else
					darls.reLayoutAction(darls.groupPolicy[j+1]); //complete relayout, ignore groups

				Rectangle left_rect = left_view.calculateContentBounds();
				Rectangle right_rect = view.calculateContentBounds(); 					
				Rectangle left_u_right = left_rect.union(right_rect);
				
				max_bbox = (max_bbox == null) ? left_u_right : max_bbox.union(left_u_right); //first time just store the lur, else u it with previous
				
			}
		}
		//init_seed_for_random_layouter(); //reset the seed
	}
	private void calc_layout_max_bbox_zoom_selecting_new() //sets max bbox considering all possible combinations of layouts in the run from binary_sets
	{

		//this is for complete layout
		//for (int i = 0; i < binary_sets.size();i++) //old statement Nov 24, 2010
		for (int i = 0; i < darls.graphs.size()-1;i++)
		{
			//jay stands for two layout styles [1] and [2]
			for (int j = 0; j < 2; j++)
			{
				//List<Integer> set = binary_sets.get(i); //old statement Nov 24, 2010
				reload_graphs_from_disk();
				fix_for_color_blind(darls.graphs);
				
				//int left_index = set.get(0);  //old statement Nov 24, 2010
				//int right_index = set.get(1); //old statement Nov 24, 2010
				
				int left_index = i;
				int right_index = i+1;
				
				
				Graph2D left_graph = darls.graphs.get(left_index);
				Graph2D right_graph = darls.graphs.get(right_index);
				
				view.setGraph2D(right_graph);
				left_view.setGraph2D(left_graph);

				/*
				if (!darls.uml_diag_in_versions)
				{
					darls.v_node_list_1 = (List <VNode>)darls.v_nodes.get(left_index);
					darls.v_edge_list_1 = (List <VEdge>)darls.v_edges.get(left_index);
					
					darls.v_node_list_2 = (List <VNode>)darls.v_nodes.get(right_index);
					darls.v_edge_list_2 = (List <VEdge>)darls.v_edges.get(right_index);
				}*/
				
				set_vlists(left_index,right_index);
				
				//Graph2D [] g = reLayoutExperiment(layout_style,left_graph,right_graph);
				
				darls.reLayoutAction(darls.groupPolicy[j+1]); //complete relayout, ignore groups
				
				//Rectangle left_rect = left_graph.getBoundingBox();
				//Rectangle right_rect = right_graph.getBoundingBox();
				
				//this is more correct then graph getBoundingBox
				Rectangle left_rect = left_view.calculateContentBounds();
				Rectangle right_rect = view.calculateContentBounds(); 
					
				Rectangle left_u_right = left_rect.union(right_rect);
				
				//System.out.println("graphs:"+i+","+(i+1)+",layout:"+j+"left_bb:"+left_rect.toString()+","+"right_bb:"+right_rect.toString());
				//System.out.println("graphs:"+i+","+(i+1)+",layout:"+j+"left_view_bb:"+left_view.calculateContentBounds().toString()+","+"right_view_bb:"+view.calculateContentBounds().toString());
				
				
				//add their randomized versions to the union as well
				
				//System.out.println("init box:"+left_u_right);
				//System.out.println("random box:"+left_u_right2);
				max_bbox = (max_bbox == null) ? left_u_right : max_bbox.union(left_u_right); //first time just store the lur, else u it with previous
				
			}
		}
		//init_seed_for_random_layouter(); //reset the seed
	}
	/*
	private void calc_layout_max_bbox_zoom() //sets max bbox considering all possible combinations of layouts in the run from binary_sets
	{

		//this is for complete layout
		for (int i = 0; i < binary_sets.size();i++)
		{
			//jay stands for two layout styles [1] and [2]
			for (int j = 0; j < 2; j++)
			{
				List<Integer> set = binary_sets.get(i);
				reload_graphs_from_disk();
				
				int left_index = set.get(0);
				int right_index = set.get(1);
				
				
				Graph2D left_graph = graphs.get(left_index);
				Graph2D right_graph = graphs.get(right_index);
				
				view.setGraph2D(right_graph);
				left_view.setGraph2D(left_graph);

				//if (!uml_diag_in_versions)
				//{
				//	v_node_list_1 = (List <VNode>)v_nodes.get(left_index);
				//	v_edge_list_1 = (List <VEdge>)v_edges.get(left_index);
					
				//	v_node_list_2 = (List <VNode>)v_nodes.get(right_index);
				//	v_edge_list_2 = (List <VEdge>)v_edges.get(right_index);
				//}
				set_vlists(left_index,right_index);
				//Graph2D [] g = reLayoutExperiment(layout_style,left_graph,right_graph);
				
				reLayoutAction(groupPolicy[j+1]); //complete relayout, ignore groups
				
				Rectangle left_rect = left_graph.getBoundingBox();
				Rectangle right_rect = right_graph.getBoundingBox();
				Rectangle left_u_right = left_rect.union(right_rect);
				
				//add their randomized versions to the union as well
				
				if (randomize)
				{
					randomize_two_graphs(left_graph, right_graph);
					Rectangle left_rect2 = left_graph.getBoundingBox();
					Rectangle right_rect2 = right_graph.getBoundingBox();
					Rectangle left_u_right2 = left_rect2.union(right_rect2);
					
					//System.out.println("init box:"+left_u_right);
					
					left_u_right = left_u_right.union(left_u_right2);
				}
				//System.out.println("init box:"+left_u_right);
				//System.out.println("random box:"+left_u_right2);
				max_bbox = (max_bbox == null) ? left_u_right : max_bbox.union(left_u_right); //first time just store the lur, else u it with previous
				
			}
		}
		init_seed_for_random_layouter(); //reset the seed
	}*/
	private void set_max_bbox_zoom()
	{
		left_view.zoomToArea(max_bbox.x, max_bbox.y, max_bbox.width, max_bbox.height);
		view.zoomToArea(max_bbox.x, max_bbox.y, max_bbox.width, max_bbox.height);
		view.updateView();
		left_view.updateView();
	}
	
	private void set_max_bbox_zoom_single_view()
	{

		dummy_view.zoomToArea(max_bbox.x, max_bbox.y, max_bbox.width, max_bbox.height);
		view.zoomToArea(max_bbox.x, max_bbox.y, max_bbox.width, max_bbox.height);
		view.updateView();
		dummy_view.updateView();	
	}
	
	//the zoom level should be the same across all the graphs in the repository
	/*protected void setMaxZoomLevel() //this function needs to be called whenever relayouting happens because the world rectangle changes as well 
	{
		
		 //This solves the navigation issue
		 //The only issue is that the graphs can get smaller
		 
		
		//this assumes that there is at least one graph in repository, otherwise crash
		Rectangle union = graphs.get(0).getBoundingBox();
		
		for (int i = 1; i < graphs.size(); i++)
		{
			Graph2D g = graphs.get(i);
			Rectangle temp_bbox = g.getBoundingBox();
			union = union.union(temp_bbox);
			
		}

		left_view.zoomToArea(union.x, union.y, union.width, union.height);
		view.zoomToArea(union.x, union.y, union.width, union.height);
		view.updateView();
		left_view.updateView();	
		System.out.println("universal zoom level was set");
	}*/
	private void randomize_two_graphs(Graph2D left_graph,Graph2D right_graph, int seed1, int seed2) 
	{
		//set layout bounds of the union of the bb of the two graphs and then randomize
		/*Rectangle left_bb = left_graph.getBoundingBox();
		Rectangle right_bb = right_graph.getBoundingBox();
		Rectangle union_bb = left_bb.union(right_bb);*/
		

		//the first pass just picks the random positions nearby,
		//while the second pass ensures no nodes overlap and maintains a minimum distance between the nodes
		
		
		
		//Random temp_rand = new Random();
		List<UUID>[] L = VObject.find_uuid(darls.v_node_list_1, darls.v_node_list_2);
		 
		Collections.shuffle(L[0], new Random(seed1)); //i arbitrarily picked seed one for the
		List percentage_list = L[0].subList(0,(int)(node_shift_skip_percentage*L[0].size()));
		//System.out.println("random shuffle:"+(int)(percentage*L[0].size()));
		
		randomize_graph_pass1(left_graph,percentage_list, seed1);
		randomize_graph_pass2(left_graph,percentage_list);
		
		randomize_graph_pass1(right_graph,percentage_list, seed2);
		randomize_graph_pass2(right_graph,percentage_list);
		
	}
	private boolean should_exclude(Node n, List<UUID> percentageList)
	{
		for (int i = 0; i < percentageList.size(); i++)
		{
			UUID uuid = percentageList.get(i);
			Node [] nn  = VNode.getNodesFromVid(uuid, darls.v_node_list_1, darls.v_node_list_2);
			if (n == nn[0] || n == nn[1])
			{
				return true;
			}				
		}
		return false;
	}
	private void randomize_graph_pass1(Graph2D graph, List<UUID> percentageList, int seed)
	{
		Random random =  new Random(seed);
		for (NodeCursor nc = graph.nodes(); nc.ok();)
		{
			
			Node n = nc.node();

			if (should_exclude(n,percentageList))
			{
				nc.next();
				continue;
			}
			
			NodeRealizer nr = graph.getRealizer(n);

			double width = nr.getWidth();
			double height = nr.getHeight();

			double [][]directions = {{width,0},{width,height},{0,height},
									 {-width,0},{-width,height},{0,-height},
									 {width,-height},{-width,-height}
									 };
			int next_x = random.nextInt(directions.length);
			int next_y = random.nextInt(directions.length);
			
			//double delta = random.nextDouble() + 1;
			
			//double delta = 1;

			double old_x = nr.getX();
			double old_y = nr.getY();
			
			double x = old_x + directions[next_x][0];
			double y = old_y + directions[next_y][1];
			
			/*boolean intersects = false;
			
			for (NodeCursor nc2 = graph.nodes(); nc2.ok(); nc2.next())
			{
				Node n2 = nc2.node();
				if (n != n2) //we dont compare the same nodes
				{
					NodeRealizer nr2 = graph.getRealizer(n2);
					
					//if realizers intersect
					if (nr2.intersects(x,y,width,height))
					{
						intersects = true;
						break;
					}
				}
			}
			if (!intersects)
			{*/
				nr.setX(x);
				nr.setY(y);
				
				nc.next();
				//System.out.println("doesn't intersect!");
			//}

		}
	}
	
	//this is the pass that is supposed to get rid of all overlaps
	private void randomize_graph_pass2(Graph2D graph, List<UUID> percentageList)
	{
		final double treshold = 30;
		for (NodeCursor nc = graph.nodes(); nc.ok();)
		{
			Node n = nc.node();
			if (should_exclude(n,percentageList))
			{
				nc.next();
				continue;
			}
			NodeRealizer nr = graph.getRealizer(n);
			Rectangle.Double bb = nr.getBoundingBox();
		
			
			double x = 0,y = 0;
			
			double theta = 0;
			int counter = 0;
			double radius_inc = 1;
			final int denominator = 16;

			boolean still_intersects = true;

			while (still_intersects)
			{
				still_intersects = false;
				
				//System.out.println("theta="+theta);
				x = nr.getX() + radius_inc*bb.width*Math.cos(theta+=Math.PI/denominator);
				y = nr.getY() - radius_inc*bb.height*Math.sin(theta+=Math.PI/denominator);

				if (counter % denominator == 0)
					radius_inc+=0.25;
					
				for (NodeCursor nc2 = graph.nodes(); nc2.ok(); nc2.next())
				{
					Node n2 = nc2.node();
					if (n != n2) //we dont compare the same nodes
					{

						Rectangle.Double rect = nr.getBoundingBox();
						rect.x=x;
						rect.y=y;
						rect.height+= treshold;
						rect.width += treshold;
						
						NodeRealizer nr2 = graph.getRealizer(n2);
						
						Rectangle.Double rect2 = nr2.getBoundingBox();
						rect2.height+= treshold;
						rect2.width += treshold;
													
						//if (nr2.intersects(x - treshold,y + treshold,bb.width+treshold*2,bb.height+treshold*2))
						if (rect.intersects(rect2))
						{
							still_intersects = true;								
							//System.out.println("still intersects!");
							break;
						}
					}
				}
				counter++;					
			}
			counter = 0;
			nr.setX(x);
			nr.setY(y);
			nc.next();
			//System.out.println("doesn't intersect!");


		}
	}

	//this is the crappy way
	/*private void randomize_graph(Graph2D graph, Rectangle bb)
	{
		for (NodeCursor nc = graph.nodes(); nc.ok();)
		{
			Node n = nc.node();		
			boolean intersects_nothing = false;
			for (NodeCursor nc2 = graph.nodes(); nc2.ok(); nc2.next())
			{
				Node n2 = nc2.node();
				if (n != n2) //we dont compare the same nodes
				{
					NodeRealizer nr = graph.getRealizer(n);
					NodeRealizer nr2 = graph.getRealizer(n2);
					
					//if realizers intersect
					if (nr.intersects(nr2.getX(),nr2.getY(),nr2.getWidth(),nr2.getHeight()))
					{
						double x = bb.getX() + random.nextDouble()*bb.getWidth();
						if (x > bb.getWidth())
							x -= bb.getWidth();
						
						double y = bb.getX() + random.nextDouble()*bb.getHeight();
						if (y > bb.getHeight())
							y -= bb.getHeight();
						nr.setX(x);
						nr.setY(y);
						intersects_nothing = true;
						System.out.println("intersects!");
					}
					
				}

			}
			if (!intersects_nothing)
			{
				nc.next();
				System.out.println("doesn't intersect!");
			}

		}
	}*/



	
	//this method can be converted into static
	private List<List<Integer>> counterBalanceBinarySets(List<List<Integer>> binary_sets, int participant_no )
	{
		int size = binary_sets.size();
		int [][] ballatsq = calcBallatsq(size);
		
		List<List<Integer>> counter_balanced_binary_sets = new ArrayList<List<Integer>>();
		for (int i = 0; i < size; i++)
		{
			int counter_balanced_index = ballatsq[participant_no % size][i] - 1;
			List<Integer> tuple = binary_sets.get(counter_balanced_index);
			counter_balanced_binary_sets.add(tuple);
			
		}
		return counter_balanced_binary_sets;
		
	}

	//this method can be converted into static
	private int [][] calcBallatsq(int size)
	{
		int [][] ballatsq = new int[size][size];
		
		if (size > 1)
		{
			ballatsq[0][0] = 1;
			ballatsq[0][1] = 2;
		}
		else
		{
			ballatsq[0][0] = 1;
			return ballatsq;
		}
		
		for (int i = 2, j = size; i <size; i+=2,j--)
		{
			ballatsq[0][i] = j;
		}
		
		for (int i = 3, j = 3; i < size; i+=2,j++)
		{
			ballatsq[0][i] = j;
		}
		
		//Initial values are generated, now we need to create the remaining rows using circular shift			
	
		for (int col = 0; col < size; col++)
		{
			for (int row = 1; row < size; row++)
			{
				ballatsq[row][col] = 1 + (ballatsq[row-1][col]) % size; 
			}
		}
		
		//print
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				System.out.print(ballatsq[i][j]+((j==size-1)?"":","));
			}
			System.out.println();
		}
		return ballatsq;
		

	}
	private List<List<Integer>> calcComparisonSets(int v_distance) //must be more than 0
	{
		int n_versions = darls.graphs.size();
		List<List<Integer>> binary_sets = new ArrayList<List<Integer>>();
		
		for (int i = 0; i < v_distance; i++)
		{
			for (int j = i; j < n_versions-v_distance; j+=v_distance)
			{
				System.out.println("set variation:"+i+",set distance:"+v_distance+",set:"+j+","+(j+v_distance));
				List<Integer> tuple = new ArrayList<Integer>();
				tuple.add(j);
				tuple.add(j+v_distance);
				binary_sets.add(tuple);
				
			}
		}
		return binary_sets;
		
	}







	

	
	/*public class Permuation
	{
		int n = 0;
		int r = 0;
		
		
		Permuation(int _n, int _r)
		{
			n = _n;
			r = _r;
		}
	}
	 public void perumateString(String input, int depth, StringBuffer output) 
		{
			if (depth == 0) 
			{
				System.out.println(output);		
			} 
			else 
			{
				for (int i = 0; i < input.length(); i++) 
				{
					output.append(input.charAt(i));
					perumateString(input, depth - 1, output);
					output.deleteCharAt(output.length() - 1);
				}
			}
		}
	 */

	}

class FalsePositiveNodeLog extends FalseObjectsLog 
{

	FalsePositiveNodeLog(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study, _database_name);
		// TODO Auto-generated constructor stub
		str_table_name = "pilot_false_positive_nodes";
		String [] str_array = 
		{
			"participant_no",
			"trial_no",
			"attempt_no",					 	 	 	 	 	 	
			"false_positive_node"
		};
		log_variables_strings = str_array;
		init_str_var_names();
	}

	
}
class FalsePositiveEdgeLog extends FalseObjectsLog 
{

	FalsePositiveEdgeLog(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study, _database_name);
		// TODO Auto-generated constructor stub
		str_table_name = "pilot_false_positive_edges";
		String [] str_array = 
		{
			"participant_no",
			"trial_no",
			"attempt_no",					 	 	 	 	 	 	
			"false_positive_edge"
		};
		log_variables_strings = str_array;
		init_str_var_names();
	}

	
}

class FalseNegativeNodeMovedLog extends FalseNegativeNodeLog
{
	FalseNegativeNodeMovedLog(UserStudy _pilot_user_study, String _database_name)
	{
		super(_pilot_user_study, _database_name);
		// TODO Auto-generated constructor stub
		str_table_name = "pilot_false_negative_nodes";
		String [] str_array = 
		{
			"participant_no",
			"trial_no",
			"attempt_no",					 	 	 	 	 	 	
			"false_negative_node",
			"x0",
			"y0",
			"x1",
			"y1"
		};
		log_variables_strings = str_array;
		init_str_var_names();
	}
	//overriding
	public void log(String object, double x0, double y0, double x1, double y1)
	{
		try
    	{	
            Statement st = db_connection.createStatement();
            String query = "INSERT INTO "
            	+str_database_name+"."
            	+str_table_name	+" (timestamp," 
            	+str_all_var_names +	            				
            			") VALUES (CURRENT_TIMESTAMP," +            			
            		pilot_user_study.participant 			+ "," +
            		(pilot_user_study.current_trial-1)		+ "," + //-1 because it get incremented before this
            		pilot_user_study.attempt_no				+ "," +
            		"\'" + object + "\'"					+ "," +
            		x0 										+ "," +
            		y0 										+ "," +
            		x1 										+ "," +
            		y1 
            		+	");";
            
            System.out.println("query:"+query);
            int val = st.executeUpdate(query);
            System.out.println("1 row affected");
        }
        catch (SQLException s)
        {
            System.out.println("SQL statement is not executed!"+s);
        }
	}
	
}
class FalseNegativeNodeLog extends FalseObjectsLog 
{

	FalseNegativeNodeLog(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study, _database_name);
		// TODO Auto-generated constructor stub
		str_table_name = "pilot_false_negative_nodes";
		String [] str_array = 
		{
			"participant_no",
			"trial_no",
			"attempt_no",					 	 	 	 	 	 	
			"false_negative_node"
		};
		log_variables_strings = str_array;
		init_str_var_names();
	}

}
class FalseNegativeEdgeLog extends FalseObjectsLog 
{

	FalseNegativeEdgeLog(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study,_database_name);
		// TODO Auto-generated constructor stub
		str_table_name = "pilot_false_negative_edges";
		String [] str_array = 
		{
			"participant_no",
			"trial_no",
			"attempt_no",					 	 	 	 	 	 	
			"false_negative_edge"
		};
		log_variables_strings = str_array;
		init_str_var_names();
	}


}
abstract class FalseObjectsLog extends PilotUserStudyLog
{
	FalseObjectsLog(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study,_database_name);
		// TODO Auto-generated constructor stub
	}
	
	public void log(String object)
	{
		try
    	{	
            Statement st = db_connection.createStatement();
            String query = "INSERT INTO "
            	+str_database_name+"."
            	+str_table_name	+" (timestamp," 
            	+str_all_var_names +	            				
            			") VALUES (CURRENT_TIMESTAMP," +            			
            		pilot_user_study.participant 			+ "," +
            		(pilot_user_study.current_trial-1)		+ "," + //-1 because it get incremented before this
            		pilot_user_study.attempt_no				+ "," +
            		"\'" + object + "\'"
            		+	");";
            
            System.out.println("query:"+query);
            int val = st.executeUpdate(query);
            System.out.println("1 row affected");
        }
        catch (SQLException s)
        {
            System.out.println("SQL statement is not executed!"+s);
        }
	}
	

}

class UserStudyLevel2Log extends PilotUserStudyLog
{
	UserStudyLevel2Log(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study,_database_name);
		// TODO Auto-generated constructor stub
		str_table_name = "pilot_level2";
		String [] str_array = 
		{
			"time_since_experiment_start",
			"time_since_attempt_start",
			"participant_no",
			"trial_no",
			"attempt_no",
			"transaction",
			"object"
		};
		log_variables_strings = str_array;
		init_str_var_names();
	}
	public void log(String transaction, String object)
	{
		long time_since_experiment_start =  System.currentTimeMillis() - pilot_user_study.experiment_start_time;
		long time_since_attempt_start = System.currentTimeMillis() - pilot_user_study.last_attempt_start_time;

		try
    	{	
            Statement st = db_connection.createStatement();
            String query = "INSERT INTO "
            	+str_database_name+"."
            	+str_table_name	+" (timestamp," 
            	+str_all_var_names +	            				
            			") VALUES (CURRENT_TIMESTAMP," +
            		time_since_experiment_start				+ "," +
                	time_since_attempt_start				+ "," +
                	pilot_user_study.participant 			+ "," +
                	(pilot_user_study.current_trial-1)		+ "," +
                	pilot_user_study.attempt_no				+ "," +
                	"\'" + transaction + "\'"				+ "," +
            		"\'" + object + "\'"
            		+	");";
            
            System.out.println("query:"+query);
            int val = st.executeUpdate(query);
            System.out.println("1 row affected");
        }
        catch (SQLException s)
        {
            System.out.println("SQL statement is not executed!"+s);
        }
	}
	
}
//level one log will be the same for all user studies
class UserStudyLevel1Log extends PilotUserStudyLog
{
	static class LogData
	{
		public long time_since_experiment_start;
		public long time_since_attempt_start;
		
		public String GUI_element;
		public String mouse_event;
		public int relative_position_x = Integer.MIN_VALUE;
		public int relative_position_y = Integer.MIN_VALUE;
		public int absolute_position_x = Integer.MIN_VALUE;
		public int absolute_position_y = Integer.MIN_VALUE;
		public int button = Integer.MIN_VALUE;
		public int clickCount = Integer.MIN_VALUE;
		
		public int modifiers = Integer.MIN_VALUE;
		public int extModifiers = Integer.MIN_VALUE;
		
		public String str_modifiers="";
		public String str_extModifiers="";
	

	}
	LogData log_data = null;
	
	UserStudyLevel1Log(UserStudy _pilot_user_study, String _databse_name) 
	{
		super(_pilot_user_study,_databse_name );
		str_table_name = "pilot_level1";
		String [] str_array = 
		{
			"time_since_experiment_start",
			"time_since_attempt_start",
			"participant_no",
			"trial_no",
			"attempt_no",
			"GUI_element",
			"mouse_event",
			"relative_position_x",
			"relative_position_y",
			"absolute_position_x",
			"absolute_position_y",
			"button",
			"clickCount",
			"modifiers",
			"exModifiers",
			"str_modifiers",
			"str_exModifiers"
		};
		log_variables_strings = str_array; 
		
		init_str_var_names();
		
	}
	public void log(String GUI_object, String mouse_action, MouseEvent e)
	{
		log_data = new LogData();
		
		log_data.time_since_experiment_start =  System.currentTimeMillis() - pilot_user_study.experiment_start_time;
		log_data.time_since_attempt_start = System.currentTimeMillis() - pilot_user_study.last_attempt_start_time;

		 
		log_data.GUI_element = GUI_object; //right view
		log_data.mouse_event = mouse_action;
		if (e != null) //if this is a mouse event and not a key event
		{
			log_data.relative_position_x = e.getX();
			log_data.relative_position_y = e.getY();
			log_data.absolute_position_x = e.getXOnScreen();
			log_data.absolute_position_y = e.getYOnScreen();
			log_data.button = e.getButton();
			log_data.clickCount = e.getClickCount();
			log_data.modifiers = e.getModifiers();
			log_data.extModifiers = e.getModifiersEx();
			log_data.str_modifiers = e.getMouseModifiersText( e.getModifiers());
			log_data.str_extModifiers = e.getModifiersExText(e.getModifiersEx());
		}
		//at this point i decided not to do anything for key event logging
		//-------------------------------------------------------------------
		//now we insert this entry into db
		try
    	{	
            Statement st = db_connection.createStatement();
            String query = "INSERT INTO "
            	+str_database_name+"."
            	+str_table_name	+" (timestamp," 
            	+str_all_var_names +	            				
            			") VALUES (CURRENT_TIMESTAMP," +            			
            		log_data.time_since_experiment_start	+ "," +
            		log_data.time_since_attempt_start		+ "," +
            		pilot_user_study.participant 			+ "," +
            		(pilot_user_study.current_trial-1)		+ "," + //check it!
            		pilot_user_study.attempt_no				+ "," +
            		"\'" + log_data.GUI_element + "\'"		+ "," +	//strings need to be in quotes
            		"\'" + log_data.mouse_event + "\'"  	+ "," +
            		log_data.relative_position_x			+ "," +
            		log_data.relative_position_y			+ "," +
            		log_data.absolute_position_x			+ "," +
            		log_data.absolute_position_y			+ "," +
            		log_data.button							+ "," +
            		log_data.clickCount						+ "," +
            		log_data.modifiers						+ "," +
            		log_data.extModifiers					+ "," +
               		"\'" + log_data.str_modifiers + "\'"	+ "," +	//strings need to be in quotes
            		"\'" + log_data.str_extModifiers + "\'"  	
            		+	");";
            
            //System.out.println("query:"+query);
            int val = st.executeUpdate(query);
            //System.out.println("1 row affected");
        }
        catch (SQLException s)
        {
            System.out.println("SQL statement is not executed!"+s);
        }
	}

}
class PilotUserStudyLevel3Log extends PilotUserStudyLog
{
	
	
	public int participant_no;
	public int trial_no;
	public int attempt_no;
	public String within_factor1;
	public String within_factor2;
	public String within_factor3;
	public String within_factor4;
	public int between_factor1;
	public int between_factor2;
	public int between_factor3;
	public int between_factor4;
	public long time_since_experiment_start;
	public long cummulative_attempt_time;
	public long attempt_time;
	public int n_false_positive_nodes;
	public int n_false_negative_nodes;
	public int n_false_positive_edges;
	public int n_false_negative_edges;
	public int n_common_nodes;
	public int n_common_edges;
	public int n_new_nodes;	
	public int n_new_edges;	
	public int n_deleted_nodes;	
	public int n_deleted_edges;	
	public int n_moved_nodes=0;
	public int n_nodes_left;	
	public int n_edges_left;	
	public int n_nodes_right;	
	public int n_edges_right;
	public int n_selected_nodes;
	public int n_selected_edges;
	public float percentage_randomized = 0.f;	//irrelevant for the pilot
	public int n_see_previous_button_clicks = 0;
	public int n_clear_button_clicks = 0;
	public String attempt_completes_trial;
	

	
	PilotUserStudyLevel3Log(UserStudy _pilot_user_study, String _database_name) {
		super(_pilot_user_study,_database_name);
		str_table_name = "pilot_level3";		
		String [] str_array = 
		{
				"participant_no",
				"trial_no",
				"attempt_no",
				"within_factor1",
				"within_factor2",
				"within_factor3",
				"within_factor4",
				"between_factor1",
				"between_factor2",
				"between_factor3",
				"between_factor4",
				"time_since_experiment_start",
				"cummulative_attempt_time",
				"attempt_time",
				"n_false_positive_nodes",
				"n_false_negative_nodes",
				"n_false_positive_edges",
				"n_false_negative_edges",
				"n_common_nodes",
				"n_common_edges",
				"n_new_nodes",	
				"n_new_edges",	
				"n_deleted_nodes",	
				"n_deleted_edges",
				"n_moved_nodes",
				"n_nodes_left",	
				"n_edges_left",	
				"n_nodes_right",	
				"n_edges_right",
				"n_selected_nodes",
				"n_selected_edges",
				"percentage_randomized",
				"n_see_previous_button_clicks",
				"n_clear_button_clicks",
				"attempt_completes_trial"
			};
		log_variables_strings = str_array;
		init_str_var_names();
		// TODO Auto-generated constructor stub
	}
	

	public void insert_new_entry_into_db()
	{
    	try
    	{
            Statement st = db_connection.createStatement();
            String query = "INSERT INTO "
            	+str_database_name+"."
            	+str_table_name	+" (timestamp," 
            	+str_all_var_names +	            				
            			") VALUES (CURRENT_TIMESTAMP," +
            			participant_no 				+ "," +
            			trial_no	   				+ "," +
            			attempt_no 	   				+ "," +
            		"\'" + within_factor1 + "\'"	+ "," +	//strings need to be in quotes
            		"\'" + within_factor2 + "\'"  	+ "," +
            		"\'" + within_factor3 + "\'"  	+ "," +
            		"\'" + within_factor4 + "\'"	+ "," +
            			between_factor1				+ "," +	
            			between_factor2			  	+ "," +
            			between_factor3 		  	+ "," +
            			between_factor4				+ "," +
            			time_since_experiment_start + "," +
            			cummulative_attempt_time 	+ "," +
            			attempt_time 				+ "," +
            			n_false_positive_nodes	    + "," +
            			n_false_negative_nodes		+ "," +
            			n_false_positive_edges		+ "," +
            			n_false_negative_edges	    + "," +
            			n_common_nodes				+ "," +
            			n_common_edges				+ "," +
            			n_new_nodes					+ "," +
            			n_new_edges 				+ "," +
            			n_deleted_nodes				+ "," +
            			n_deleted_edges				+ "," +
            			n_moved_nodes				+ "," +
            			n_nodes_left				+ "," +
            			n_edges_left				+ "," +
            			n_nodes_right				+ "," +
            			n_edges_right				+ "," +
            			n_selected_nodes			+ "," +
            			n_selected_edges			+ "," +
            			percentage_randomized		+ "," +
            			n_see_previous_button_clicks+ "," +
            			n_clear_button_clicks		+ "," +
            			"\'" + attempt_completes_trial+ "\'" +
            			");";
            System.out.println("query:"+query);
            int val = st.executeUpdate(query);
            System.out.println("1 row affected");
        }
        catch (SQLException s)
        {
            System.out.println("SQL statement is not executed!"+s);
        }
	}
	public void log_time_and_success(String success) 
	{
		long curr_time = System.currentTimeMillis();
		time_since_experiment_start = curr_time - pilot_user_study.experiment_start_time;
		cummulative_attempt_time = curr_time - pilot_user_study.cummulative_attempt_start_time ;
		attempt_time 			 = curr_time - pilot_user_study.last_attempt_start_time ;
		attempt_completes_trial = success;
	}
}

class PilotUserStudyLog extends Log
{
	protected String str_all_var_names = null;
	protected String str_table_name = null;
	protected String [] log_variables_strings = null;

	PilotUserStudyLog(UserStudy _pilot_user_study, String _str_database_name)
	{
		super(_pilot_user_study,_str_database_name);
		//pilot_user_study = _pilot_user_study;
		//System.out.println("Initializing log and connecting to database");
		//connect_to_db();
	}
	protected void init_str_var_names()
	{
		str_all_var_names = new String(""); 
		for (int i = 0; i < log_variables_strings.length; i++)
		{
			str_all_var_names+=log_variables_strings[i];
			//if not the last one
			if (i < log_variables_strings.length -1)
				str_all_var_names+=", ";
		}
		

	}
}
class Log
{			
	public String str_database_name = null;	
	UserStudy pilot_user_study;

	static final public String str_table_names [] = 
	{	
		"pilot_level1",
		"pilot_level2",
		"pilot_level3",
		"pilot_false_negative_nodes",
		"pilot_false_negative_edges",		
		"pilot_false_positive_nodes",
		"pilot_false_positive_edges"
	};
	/*static enum table_names {
		PILOT_LEVEL1,
		PILOT_LEVEL2,
		PILOT_LEVEL3,
		PILOT_FALSE_NEGATIVE_NODES,
		PILOT_FALSE_NEGATIVE_EDGES,
		PILOT_FALSE_POSITIVE_NODES,
		PILOT_FALSE_POSITIVE_EDGES
		};*/
	//----------------------------------------------------
	Log(UserStudy _pilot_user_study, String _str_database_name)
	{
		str_database_name = _str_database_name;
		pilot_user_study = _pilot_user_study;
	}
	static Connection db_connection = null;

	static void connect_to_db()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver"); //Load the driver
			db_connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", ""); //Connect
			System.out.println("Connected to the database");
		}
		catch (Exception err)
		{
			System.out.println("Problem:"+err);
		}
	}
	static void disconnect_from_db()
	{
		try
		{
			db_connection.close();
			System.out.println("Disconnected from database");
		}
	    catch (Exception err)
	    {
	    	System.out.println("Problem:"+err);
	    }
	}
}