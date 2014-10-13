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
