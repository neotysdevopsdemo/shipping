package works.weave.socks.shipping.loadtest;

import com.google.common.collect.ImmutableList;
import com.neotys.neoload.model.repository.*;
import com.neotys.neoload.model.v3.project.server.Server;
import com.neotys.neoload.model.v3.project.userpath.ImmutableRequest;
import com.neotys.neoload.model.v3.project.userpath.ThinkTime;
import com.neotys.neoload.model.v3.project.variable.Variable;
import com.neotys.testing.framework.BaseNeoLoadDesign;
import com.neotys.testing.framework.BaseNeoLoadUserPath;

import java.util.Optional;

import static com.neotys.testing.framework.utils.NeoLoadHelper.variabilize;
import static java.util.Collections.emptyList;


public class ShippingUserPath extends BaseNeoLoadUserPath {
    public ShippingUserPath(BaseNeoLoadDesign design) {
        super(design);
    }

    @Override
    public com.neotys.neoload.model.v3.project.userpath.UserPath createVirtualUser(BaseNeoLoadDesign baseNeoLoadDesign) {
        final Server server = baseNeoLoadDesign.getServerByName("carts_host");
        final Variable constantpath= baseNeoLoadDesign.getVariableByName("shippingPath");
        final String jsonPayload="{\n" +
                "    \"id\":\"42\",\n" +
                "    \"name\":\"ArthurDent\"\n" +
                " }";
        final ImmutableList<com.neotys.neoload.model.v3.project.userpath.Header> headerList= ImmutableList.of(
                header("Cache-Control","no-cache"),
                header("Content-Type","application/json"),
                header("json","true")
        );
        final ImmutableRequest postRequest = postTextBuilderWithHeaders(server,headerList, variabilize(constantpath),emptyList(),jsonPayload,emptyList(),Optional.empty()).build();

        final ThinkTime delay = thinkTime(250);
        final com.neotys.neoload.model.v3.project.userpath.ImmutableContainer actionsContainer = actionsContainerBuilder()
                .addSteps(container("Shipping", Optional.empty(), postRequest, delay))
                .build();

        return userPathBuilder("ShippingUserPath")
                .actions(actionsContainer)
                .build();
    }
}
