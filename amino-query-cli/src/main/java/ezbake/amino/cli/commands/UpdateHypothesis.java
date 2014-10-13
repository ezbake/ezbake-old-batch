/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

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
