package ezbake.amino.job.bitmap;


import com._42six.amino.bitmap.BitLookupJob;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class EzBitLookupJob {
    public static void main(String[] args) throws Exception {
        final Configuration conf = new Configuration();
        EzJobUtil.loadEzConfigurations(conf);

        int res = ToolRunner.run(conf, new BitLookupJob(), args);
        System.exit(res);
    }
}
