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

package ezbake.amino.job.bitmap;


import com._42six.amino.bitmap.reverse.ReverseBitmapJob;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class EzReverseBitmapJob {
    public static void main(String[] args) throws Exception {
        final Configuration conf = new Configuration();
        EzJobUtil.loadEzConfigurations(conf);

        int res = ToolRunner.run(conf, new ReverseBitmapJob(), args);
        System.exit(res);
    }
}
