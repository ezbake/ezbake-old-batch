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
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzDatabasePrepJob -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzByBucketJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzBitLookupJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzStatsJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzHypothesisJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzReverseBitmapJob  -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzReverseFeatureLookupJob -d $DIR_BASE/config &&
hadoop jar $BITMAP_JAR ezbake.amino.job.bitmap.EzFeatureMetadataJob -d $DIR_BASE/config
