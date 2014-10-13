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

package ezbake.amino.job;

import com._42six.amino.api.job.AminoReducer;
import com._42six.amino.data.DataLoader;
import com._42six.amino.impl.reducer.number.*;
import ezbake.amino.impl.dataloader.WarehausNumberLoader;

import java.util.ArrayList;

/**
 * An Amino Job for accessing the numbers dataset from the Warehaus
 */
public class WarehausNumberJob extends WarehausAminoJob{
    @Override
    public Class<? extends DataLoader> getDataLoaderClass(){
        return WarehausNumberLoader.class;
    }

    @Override
    public Iterable<Class<? extends AminoReducer>> getAminoReducerClasses(){
        final ArrayList<Class<? extends AminoReducer>> ars = new ArrayList<>();
        ars.add(FirstDigit.class);
        ars.add(EvenOrOdd.class);
        ars.add(IsNumber.class);
        ars.add(HasDigitRatio.class);
        ars.add(HasDigitNominal.class);
        ars.add(PerfectSquare.class);
        return ars;
    }
}
