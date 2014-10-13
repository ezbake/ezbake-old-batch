#!/bin/sh

VERSION=0.0.2

JOB_JAR=number-$VERSION-SNAPSHOT.jar
BITMAP_JAR=amino-bitmap-jobs-$VERSION-SNAPSHOT.jar
DIR_BASE=/amino/numbers

while getopts "b:d:j:" opt; do
  case $opt in
    b)
        echo "Setting Bitmap jar to $OPTARG"
        BITMAP_JAR=$OPTARG
        ;;
    d)
        echo "Setting directory base to $OPTARG"
        DIR_BASE=$OPTARG
        ;;
    j)
        echo "Setting Job jar to $OPTARG"
        JOB_JAR=$OPTARG
        ;;
    v)
        echo "Setting version number to $OPTARG"
        VERSION=$OPTARG
        ;;
    \?)
        echo "Invalid option -$OPTARG" >&2
        exit 1
        ;;
  esac
done

echo "JOB JAR set to $JOB_JAR"
echo "Bitmap JAR set to $BITMAP_JAR"
echo "Directory base set to $DIR_BASE"
echo "Version $VERSION"

hadoop jar $JOB_JAR com._42six.amino.api.framework.FrameworkDriver -d $DIR_BASE/config -c ./NumbersJob.xml &&
#   Copyright (C) 2013-2014 Computer Sciences Corporation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzDatabasePrepJob -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzByBucketJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzBitLookupJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzStatsJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzHypothesisJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzReverseBitmapJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzReverseFeatureLookupJob -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzFeatureMetadataJob -d $DIR_BASE/config
