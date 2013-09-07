package thesisplot.core;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.labels.*;
import org.jfree.data.category.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.Paint;
import java.awt.color.ColorSpace;
import java.text.NumberFormat;
import java.text.MessageFormat;
import org.jfree.data.xy.XYDataset;

public class CustomLineRenderer extends LineAndShapeRenderer {
    public class CustomGenerator extends StandardCategoryItemLabelGenerator {
        public CustomGenerator() {
            super("{2}", NumberFormat.getInstance());
        }

        @Override
        protected String generateLabelString(CategoryDataset dataset,
                int row, int column) {
            if (dataset == null) {
                throw new IllegalArgumentException("Null 'dataset' argument.");
            }
            String result = null;
            Object[] items = createItemArray(dataset, row, column);
            items[2] = dataset.getValue(row, column);
            result = MessageFormat.format("{2,number,#.00}", items);
            return result;

        }
    }

    public CustomLineRenderer() {
        super();
        this.setDrawOutlines(true);
        this.setUseOutlinePaint(true);
        this.setBaseItemLabelFont(new Font("Serif", Font.PLAIN, 17));
        this.setBaseItemLabelGenerator(new CustomGenerator());
    }

    @Override
    public Paint lookupSeriesPaint(final int series) {
        float maxColors = 10;
        float shift = (float)series / maxColors;
        return new Color(Color.HSBtoRGB(0.522222222222f - (shift * 0.2f - (series % 2 == 0 ? 0.3f : 0f)), 0.6f - (shift * 0.4f), 0.833333f));
    }
}
