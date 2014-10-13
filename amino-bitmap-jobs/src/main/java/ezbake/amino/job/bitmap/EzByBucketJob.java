package ezbake.amino.job.bitmap;


import com._42six.amino.bitmap.ByBucketJob;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class EzByBucketJob {
    public static void main(String[] args) throws Exception {
        final Configuration conf = new Configuration();
        EzJobUtil.loadEzConfigurations(conf);

        int res = ToolRunner.run(conf, new ByBucketJob(), args);
        System.exit(res);
    }
}
