package ezbake.amino.cli.commands;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ezbake.services.amino.thrift.THypothesis;
import ezbake.services.amino.thrift.THypothesisFeature;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;

import java.io.IOException;
import java.util.*;

public class CreateHypothesis extends Command {
    public interface PropName {
        String displayValue();
        String pname();
    }

    public enum Props implements PropName {
        Justification("justification"),
        Name("name"),
        EditUsers("users.edit."),
        EditView("users.view."),
        Visibility("visibility"),
        BtVisibility("visibility.bt"),
        BucketId("bucketId"),
        DatasourceId("datasourceId"),
        FeaturesMetadata("features.<number>.metadataId"),
        FeaturesType("features.<number>.type"),
        FeaturesValue("features.<number>.value");

        String propName;

        Props(String pName) {
            propName = pName;
        }

        @Override
        public String displayValue() {
            if ( propName.endsWith(".") ) {
                return propName + "<number>";
            } else {
                return propName;
            }
        }

        @Override
        public String pname() {
            return propName;
        }
    }


    @Option(name="-P", aliases = "--property", metaVar = "k=v", usage = "define k=v as a hypothesis property", handler=MapOptionHandler.class)
    protected Map<String,String> propMap = new HashMap<>();

    @Option(name = "-f", aliases = "--filename", usage = "A properties file to load hypothesis properties",
            required = false)
    protected String propFileName = "";

    protected Properties loadedProps = new Properties();

    public void init() {
        super.init();
        loadIntoProperties(propMap, propFileName, loadedProps);
    }

    @Override
    public Integer execute() throws Exception {
        if (shouldShowUsage())
            return showUsage();

        THypothesis result = aminoClient.createHypothesis(createHypothesis(), securityToken);
        output.println(result.toString());
        return 0;
    }

    protected THypothesis createHypothesis() {
        final THypothesis hypothesis = new THypothesis();
        // long createTime = System.currentTimeMillis();
        hypothesis.setOwner(securityToken.getValidity().getIssuedTo());
        hypothesis.setJustification(loadedProps.getProperty(Props.Justification.pname()));
        hypothesis.setName(Props.Name.pname());
        hypothesis.setCanEdit(getAsList(Props.EditUsers));
        hypothesis.setCanView(getAsList(Props.EditView));
        hypothesis.setBtVisibility(loadedProps.getProperty(Props.BtVisibility.pname()));
        hypothesis.setVisibility(loadedProps.getProperty(Props.Visibility.pname()));
        hypothesis.setBucketid(loadedProps.getProperty(Props.BucketId.pname()));
        hypothesis.setDatasourceid(loadedProps.getProperty(Props.DatasourceId.pname()));
        hypothesis.setQueries(Collections.<String>emptySet());
        hypothesis.setHypothesisFeatures(getAllFeatures());
        return hypothesis;
    }

    private Set<THypothesisFeature> getAllFeatures() {
        Set<THypothesisFeature> features = Sets.newHashSet();
        for (int i = 0; i < 9999; i++) {
            String md = withNumber(Props.FeaturesMetadata, i);
            String type = withNumber(Props.FeaturesType, i);
            String value = withNumber(Props.FeaturesValue, i);
            if ( Strings.isNullOrEmpty(md) ||
                 Strings.isNullOrEmpty(type) ||
                 Strings.isNullOrEmpty(value) ) continue;
            features.add(new THypothesisFeature()
                    .setFeatureMetadataId(md)
                    .setType(type)
                    .setValue(value));
        }
        return features;
    }

    protected String withNumber(PropName prop, int value) {
        String pName = prop.pname().replace("<number>", Integer.toString(value));
        return loadedProps.getProperty(pName);
    }

    protected List<String> getAsList(PropName prop) {
        List<String> values = Lists.newArrayList();
        for (Map.Entry<Object, Object> p : loadedProps.entrySet()) {
            if (((String)p.getKey()).startsWith(prop.pname())) {
                String va = loadedProps.getProperty(prop.pname());
                if ( ! Strings.isNullOrEmpty(va)) {
                    values.add(va);
                }
            }
        }
        return values;
    }



    protected boolean shouldShowUsage() {
        return loadedProps.isEmpty();
    }

    protected List<? extends PropName> getPropNames() {
        return ImmutableList.copyOf(Props.values());
    }

    @Override
    protected String extraUsageHelp() {
        StringBuilder usage =  new StringBuilder("Required hypothesis properties (-P propName=propValue):");
        for (PropName prop : getPropNames() ) {
            usage.append("\n\t").append(prop.displayValue());
        }
        usage.append("\n");
        return usage.toString();


    }
}
