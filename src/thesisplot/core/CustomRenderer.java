package thesisplot.core;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.labels.*;
import java.awt.Font;
public class CustomRenderer extends StackedBarRenderer {
    public CustomRenderer() {
        this.setDrawBarOutline(false);
        this.setShadowVisible(false);
        this.setBarPainter(new StandardBarPainter());
        this.setBaseItemLabelsVisible(true);
        this.setBaseItemLabelFont(new Font("Serif", Font.BOLD, 15));
        this.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
    }
}
