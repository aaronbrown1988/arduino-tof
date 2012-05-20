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
public class Chart extends ApplicationFrame {
	private String axisLabelDomain_;
	private String axisLabelRange_;
	private CategoryDataset dataset_;
	private String title_;
	
    public Chart(final String title, double[] values, String[] names, String axisLabelDomain, String axisLabelRange) {
        super(title);
        
		//Set the initial variables. 
		title_ = title;
		
		//Create the internal dataset from the provided data.
        dataset_ = createDataset(values, names);
        //final JFreeChart chart = createChart();
        //final ChartPanel chartPanel = new ChartPanel(chart);
        //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        //setContentPane(chartPanel);
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
   
   public void updateDataset(double[] values, String[] names) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for (int i = 0; i < values.length; i++) {
			dataset.addValue(values[i], names[i], "Category 1"); 
		}
		
        dataset_ = dataset;
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
        renderer.setItemLabelsVisible(true);
        final BarRenderer r = (BarRenderer) renderer;
        //r.setMaxBarWidth(0.05);
        
        return chart;
    }
}