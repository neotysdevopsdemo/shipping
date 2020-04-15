package works.weave.socks.shipping.loadtest;

import com.neotys.testing.framework.BaseNeoLoadDesign;
import com.neotys.testing.framework.NeoLoadTest;

import java.io.FileNotFoundException;
import java.util.Optional;

public class ShippingLoadTest extends NeoLoadTest {
    @Override
    protected BaseNeoLoadDesign design() {
        try {
            return new TestingDesign(Optional.empty());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String projectName() {
        return "Shipping_NeoLoad";
    }

    @Override
    public void createComplexPopulation() {

    }

    @Override
    public void createComplexScenario() {

    }

    @Override
    public void execute() {

        createSimpleConstantLoadScenario("Shipping_Load","ShippingUserPath",600,49,10, Optional.empty());
        createSimpleConstantIterationScenario("DynatraceSanityCheck","BasicCheckTesting",15,1,0,Optional.empty());
        createSanityCheckScenario();
    }
}
