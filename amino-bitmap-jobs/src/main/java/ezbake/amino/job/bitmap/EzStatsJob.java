package ezbake.amino.job.bitmap;


import com._42six.amino.bitmap.StatsJob;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class EzStatsJob {
    public static void main(String[] args) throws Exception {
        final Configuration conf = new Configuration();
        EzJobUtil.loadEzConfigurations(conf);

        int res = ToolRunner.run(conf, new StatsJob(), args);
        System.exit(res);
    }
}
