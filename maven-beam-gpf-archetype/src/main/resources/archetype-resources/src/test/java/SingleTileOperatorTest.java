package $groupId;

import junit.framework.TestCase;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;

import javax.media.jai.operator.ConstantDescriptor;
import java.awt.image.RenderedImage;

/**
 * Unit test for SingleTileOperator.
 */
public class SingleTileOperatorTest extends TestCase {


    public void testSingleTileOperator() {
        int w = 32;
        int h = 16;
        RenderedImage imageA = ConstantDescriptor.create((float) w, (float) h, new Float[]{2.5f}, null);

        Product sourceProduct = new Product("A", "AType", w, h);
        Band bandA = sourceProduct.addBand("A", ProductData.TYPE_FLOAT32);
        bandA.setSourceImage(imageA);

        MultiTileOperator op = new MultiTileOperator();
        op.setSourceProduct(sourceProduct);
        op.setParameter("sourceBandName", "A");
        op.setParameter("targetBandName1", "X");
        op.setParameter("targetBandName2", "Y");

        Product targetProduct = op.getTargetProduct();
        assertNotNull(targetProduct);
        assertEquals(w, targetProduct.getSceneRasterWidth());
        assertEquals(h, targetProduct.getSceneRasterHeight());

        assertEquals(2, targetProduct.getNumBands());

        Band bandX = targetProduct.getBand("X");
        assertNotNull(bandX);
        RenderedImage imageX = bandX.getSourceImage();
        assertEquals(0.25f, imageX.getData().getSampleFloat(0, 0, 0));

        Band bandY = targetProduct.getBand("Y");
        assertNotNull(bandY);
        RenderedImage imageY = bandY.getSourceImage();
        assertEquals(0.5f, imageY.getData().getSampleFloat(0, 0, 0));

        op.dispose();
    }
}
