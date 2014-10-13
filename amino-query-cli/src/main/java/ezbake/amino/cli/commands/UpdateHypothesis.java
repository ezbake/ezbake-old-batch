package ezbake.amino.cli.commands;

import com.google.common.collect.ImmutableList;
import ezbake.services.amino.thrift.THypothesis;

import java.util.List;

public class UpdateHypothesis extends CreateHypothesis {

    protected PropName ID_PROP = new PropName() {
        @Override
        public String displayValue() {
            return "id";
        }

        @Override
        public String pname() {
            return "id";
        }
    };

    @Override
    protected THypothesis createHypothesis() {
        return super.createHypothesis().setId(loadedProps.getProperty(ID_PROP.pname()));
    }

    @Override
    protected List<? extends PropName> getPropNames() {
        return ImmutableList.<PropName>builder().addAll(super.getPropNames()).add(ID_PROP).build();
    }
}
