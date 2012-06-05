package trampoline;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * This demonstration shows a 3D bar chart with item labels displayed.
 *
 */
public class Chart {
	private String axisLabelDomain_;
	private String axisLabelRange_;
	private CategoryDataset dataset_;
	private String[] names_;
	private int numberOfItems_;
	private String title_;
	private double[] values_;
	
    public Chart(final String title, double[] values, String[] names, String axisLabelDomain, String axisLabelRange) {
        //Set the initial variables. 
		title_ = title;
		values_ = values;
		names_ = names;
		numberOfItems_ = Math.min(values.length, names.length);
		
		//Create the internal dataset from the provided data.
        dataset_ = createDataset(values, names);
        //final JFreeChart chart = createChart();
        //final ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        //setContentPane(chartPanel);
    }
	
	public Chart(String title, Routine r) {
		System.out.println("Initialising Chart with title "+title);
		
		Jump[] jumpList = r.getJumps();
		
		title_ = title;
		numberOfItems_ = r.getNumberOfJumps();
		
		values_ = new double[numberOfItems_];
		names_  = new String[numberOfItems_];
		
		for (int i = 0; i < numberOfItems_; i++) {
			values_[i] = jumpList[i].getTof();
			names_[i]  = "Jump "+i;
		}
		
        dataset_ = createDataset(values_, names_);
	}
	
	public Chart(Routine r) {
		this("Routine "+r.getID()+" with "+r.getNumberOfJumps()+" jumps.", r);
	}
    
    /**
     * Creates a sample dataset.
     *
     * @return a sample dataset.
     */
   private CategoryDataset createDataset(double[] values, String[] names) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for (int i = 0; i < values.length; i++) {
			dataset.addValue(values[i], names[i], "Category 1"); 
		}
		
        return dataset;
    }
   
   public int getNumberOfItems() {
	   return numberOfItems_;
   }
   
   public void setTitle(String title) {
	   title_ = title;
   }
   
   public void updateDataset(double[] values, String[] names) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		System.out.println("starting dataset update with "+numberOfItems_+" items");
		for (int i = 0; i < numberOfItems_; i++) {
			System.out.println("update: "+values[i]+" name: "+names[i]);
			dataset.addValue(values[i], names[i], "Category 1"); 
		}
		System.out.println("ending update");
		
        dataset_ = dataset;
    }
   
   //numberToUpdate should start from zero.
   public void updateValue(double value, String name, int numberToUpdate) {
		double[] values = new double[numberOfItems_];
		String[] names  = new String[numberOfItems_];
	   
	   for (int i = 0; i < values_.length; i++) {
		   values[i] = values_[i];
		   names[i]  = names_[i];
	   }
	   
		values[numberToUpdate] = value;
		names[numberToUpdate] = name;
	   
	   values_ = values;
	   names_  = names;
	   
	   updateDataset(values, names);
    }
   
   //Shortcut Function for the Above
   public void updateValue(double value, int numberToUpdate) {
	   int i = numberToUpdate+1;
	   updateValue(value, "Jump "+i, numberToUpdate);
   }
   
   public void addValue(double value, String name) {
	   System.out.println("values_ length is "+values_.length+" (try at start in chart.java)");
	   System.out.println("NOI is "+numberOfItems_+" (try at start in chart.java)");
	   numberOfItems_++;
	   
	   double[] values = new double[numberOfItems_];
	   String[] names  = new String[numberOfItems_];
	   
	   for (int i = 0; i < values_.length; i++) {
		   values[i] = values_[i];
		   names[i]  = names_[i];
	   }
	   
	   values[numberOfItems_-1]  = value;
	   names[numberOfItems_-1]   = name;
	   
	   values_ = values;
	   names_  = names;
	   
	   updateDataset(values, names);
	   System.out.println("values_ length is "+values_.length+" (try at end in chart.java)");
	   System.out.println("NOI is "+numberOfItems_+" (try at end in chart.java)");
   }
   
   public void addValue(double value) {
	   int finalJump = numberOfItems_+1;
	   addValue(value, "Jump "+finalJump);
   }
    
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    public JFreeChart createChart() {
        
        final JFreeChart chart = ChartFactory.createBarChart3D(
            title_,      // chart title
            axisLabelDomain_,               // domain axis label
            axisLabelRange_,                  // range axis label
            dataset_,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        final CategoryPlot plot = chart.getCategoryPlot();
        final CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 8.0)
        );
        
        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelsVisible(true);
        final BarRenderer r = (BarRenderer) renderer;
        //r.setMaxBarWidth(0.05);
        
        return chart;
    }
}