package works.weave.socks.shipping.loadtest;

import com.neotys.neoload.model.v3.project.variable.ConstantVariable;
import com.neotys.neoload.model.v3.project.variable.Variable;
import com.neotys.testing.framework.BaseNeoLoadDesign;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Optional;

import static com.neotys.testing.framework.utils.NeoLoadHelper.*;

public class TestingDesign extends BaseNeoLoadDesign {
    protected TestingDesign(Optional<String> jsonSLAProfile) throws FileNotFoundException {
        super(jsonSLAProfile);
    }

    @java.lang.Override
    public void createNeoLoadUserPaths() {
        this.addVirtualUser(new BasicUserPath(this));
        this.addVirtualUser(new ShippingUserPath(this));


    }

    @java.lang.Override
    public void createVariables() {
        final com.neotys.neoload.model.v3.project.variable.ConstantVariable server = createConstantVariable("host", "shipping,");
        final com.neotys.neoload.model.v3.project.variable.ConstantVariable port = createConstantVariable("port", "80");
        final com.neotys.neoload.model.v3.project.variable.ConstantVariable cartserver = createConstantVariable("carts_host", "shipping");
        final com.neotys.neoload.model.v3.project.variable.ConstantVariable cartsport = createConstantVariable("carts_port", "80");
        //TODO take care about file path, perhaps we should use a mechanism to copy the source file to the NeoLoad project folder ?
        final com.neotys.neoload.model.v3.project.variable.ConstantVariable basicPath = createConstantVariable("basicPath", "/");
        final ConstantVariable shippingPath = createConstantVariable("shippingPath", "/shipping");
        this.addVariables(server, port,cartserver,cartsport,basicPath,shippingPath);
    }

    @java.lang.Override
    public void createServers() {
            final com.neotys.neoload.model.v3.project.variable.Variable server = getVariableByName("host");
        final com.neotys.neoload.model.v3.project.variable.Variable port = getVariableByName("port");
        final com.neotys.neoload.model.v3.project.variable.Variable cartsserver = getVariableByName("carts_host");
        final Variable cartsport = getVariableByName("carts_port");
        this.addServer(createServer(variabilize(server), variabilize(port)));
        this.addServer(createServer(variabilize( cartsserver), variabilize(cartsport)));
    }
}
