Implementing a DataLoader
=========================
Bulk data in the EzBake ecosystem resides in the Warehaus. In order to run Amino analytics on the data in Ezbake, the 
developer must write a couple of classes to process the data that is stored in the Warehaus.  The following is what it 
entails to process the data to create the chunks for Amino.

_NOTE: In the future, there will be an `archetype` to help facilitate the creation of this process_   

Dataloader Components
---------------------

The "dataloader" in the sense of this document is really comprised of 3 distinct parts - The MapReduce job for loading 
up the data and doing Amino analytics, the dataloader itself for processing the data in the Warehaus, and the Reducers 
for pulling out the Amino Analytics.  These can be found in the `dataloader`, `job`, and `reducer` subdirectories of the
 `amino-impl` project

### Dataloader

This is what loads the data from the Warehaus.  The developer should extend the `WarehausDataLoader` which will take care
of connecting to the Warehaus and grabbing the appropriate data.  

When the developer registers their application in Ezbake, they have the option of adding Amino Jobs.  If an Amino Job is
registered, the data loader will be able to access this job from INS at runtime and determine what data it needs to get
from the Warehaus.  It is possible to manually tell the dataloader what data to load from the Warehaus by setting the
`warehausDataLoader.datasource.uri` key/value in the configuration.  If this key is set, the dataloader will use this 
this value, otherwise it will call out to INS to determine what data it needs to process.

#### RAW vs Thrift

The data inside of the Warehaus is stored in both Thrift and RAW formats.  The developer is free to choose which
format of the data that they would like to process. To do so, the developer sets the `warehausDataLoader.dataType` key/value
in the config to either `PARSED` for Thrift data, or `RAW` to access the raw data that was stored.  

The `WarehausDataLoader` has two abstract methods that need need to be implemented

    void extractFromRaw(final byte[] rawData, final MapWritable outputMap) 
    void extractFromThrift(final byte[] rawThrift, final MapWritable outputMap)
    
Both methods take in data and chunk it out into pieces.  The outputMap is a mapping of Text fields representing what the
atom of data is, and a Writable for that value (for example <username, "Bob">).  The `byte[] rawThrift` will be the bytes
for the serialized Thrift object.  

#### All data vs Temporal data

There are two modes of fetching the data from the Warehaus.  The default is to fetch all of the records given by the 
registered job pipeline, or the values pointed to by `warehausDataLoader.datasource.uri`.  The second way of fetching 
data is temporally.  

If the config has the key `warehausDataLoader.timeRange.Type` set, then the dataloader will pull data from the Warehaus
from specific date ranges.  There are 3 modes of pulling data temporally from the Warehaus:
        
* **EXPLICIT_RANGE** - In this mode the dates to pull are explicitly given in the config
* **START_DATE** - All data from a given date to the present are fetched
* **RECENT_PERIOD** - All data from a period of time to the present are fetched (i.e. the last weeks worth of data)


#### Methods to Override

In addition to the two methods to implement for parsing the data, it is recommended that the developer also override the
following methods as appropriate

    public void setConfig(Configuration config)

Override this method to set any additional config parameters that you might need for later.

---

    public String getDataSourceName()
    
The name of the data source.  

---

    public String getDataSetName(MapWritable mw)
    
The name of the dataset inside of the datasource

---

    public String getVisibility()
    
This is classification string to use in the database for protecting the data.  This should be the maximum classification
of all of the data.  The default is whatever was registered for the application via the web site.  Example: U

---

    public String getHumanReadableVisibility()

This is the human readable version of the visibility string (i.e. UNCLASSIFIED vs U).  This string gets passed along so
that GUIs can display the classifications in a meaningful form.  The default is whatever was registered for the application
via the website

---

#### Overriding bucket names

In addition to overriding the above methods, the dataloader class should also set the bucket names for the loader.  This
can be done by statically loading the bucket names such as:

    private static final Text BUCKET1 = new Text("number");
    private static final Text BUCKET2 = new Text("number2");
    static {
        bucketsAndDisplayNames.put(BUCKET1, new Text("number"));
        bucketsAndDisplayNames.put(BUCKET2, new Text("number2"));
    }

### Reducer

The reducers are used to extract the Amino features from the data chunks of the mapper.  The reducers very simple and
should implement the `AminoReducer` interface for extracting the features from the data.  The reducer will go through all
of the chunks and produce `AminoWritable`s, which consist of a `Feature` describing the feature and a `FeatureFact`.

The `FeatureFact` is the core type of information in Amino.  Currently, it can be one of the following types:

* **DateFeatureFact** - A simple date
* **DateHourFeatureFact** - A date and time
* **IntervalFeatureFact** - Represents and interval of values (dates, ranges, etc)
* **NominalFeatureFact** - One of a discrete list of values (e.g. city names)
* **OrdinalFeatureFact** - A collection of values that has order (e.g. first, second, third)
* **PointFeatureFact** - A lat/long
* **PolygonFeatureFact** - A collection of PointFeatureFacts
* **RatioFeatureFact** -  Something that could be counted in a range of values (e.g looking for number starting with 1-3)

Adding a new feature to your dataset is as simple as implementing a class with the signature:

    public class MyFeature extends AminoConfiguredReducer implements AminoReducer

Your new class must implement a single method:

    public Iterable<AminoWritable> reduce(DatasetCollection datasets)

Here's an example of a reducer that decides whether a number is even or odd: https://github.com/amino-cloud/amino/blob/master/amino-impl/reducer/src/main/java/com/_42six/amino/impl/reducer/number/EvenOrOdd.java

### Job

This is the MapReduce job that ties together the dataloader and reducer to produce the Amino analytics from the Warehaus
data.  It should extend from `WarehausAminoJob` and typically only overrides the following two methods:

* **getDataLoaderClass()** - Simply returns the current class. If this is not overriden, it will attempt to find the class 
name from the config in the `AminoJob.dataLoader` key
* **getAminoReducerClasses()** - This simply creates an Iterable of all of the AminoReducers that the job should run

Running an AminoJob requires that all of the associated jars are bundled together into one uber jar.  To help facilitate
this, the POMs are set up to create one uber jar which is also conveniently packaged into a .tar.gz for uploading to the
Ezbake deployment website.  The typical layout of the job heirarchy is:

* **project/pom.xml** - Sets up the project to build the uberjar and .tar.gz
* **project/src/assembly/package.xml** - The directives for packaging the output
* **project/src/configs/projectConfig.xml** - Any config values needed for the job, such as raw/thrift or temporal
* **project/src/non-packaged-resources** - The yml file for uploading to the Ezbake website.  Gets put in the target directory on build
* **project/src/main/java/ezbake/amino/job/TheJob.java** - The actual job file
* **project/src/main/resources/META-INF/services/com._42six.amino.api.job.AminoJob** - Tells Amino what class it should be running 

Running the Dataloader
----------------------

When the developer registers their job, it will be scheduled to run once every 24 hours by default.  This will
read the data from the Warehaus using the dataloader and be passed to a Mapreduce job for processing.  The developer's
reducers will be used to extract Amino features from the data and place them in HDFS to be aggregated with the output of
other batch jobs.  Periodically, Ezbake will run Amino analytics against the aggregated data and populate the Ezbake 
Amino tables. 

### Uploading and executing

In order for the dataloader to run, it needs to be uploaded to the Ezbake system.  Use the following steps to deploy a dataloader to the Ezbake system

1. Log into the Ezbake deployment web page
2. Create a new application if one does not already exist.  It is important to "Add Amino Job" at the very end.  This will register a job in INS and allow the dataloader to know what rows from the Warehaus it needs to process for each data source
3. Submit the application and wait for it to be approved
4. Compile your job.  Make sure that in the manifest.yml that you have the `name` key set to the name of your application and that the `job_name` is set to what you named the aminoJob
5. Under your application, click on the deployments section and click "Add New Deployment Package"
6. A modal will appear.  Under "Add Application File (.JAR, .WAR, or .EAR) Or use advanced option" click 'use advanced' 
7. For "Add Deployer tar.gz" click on "Choose File" and browse to the target directory of the job.  You will find the .tar.gz to upload
8. For "Add Manifest File (.YML)", click on "Choose File" and browse to the target directory of the job.  You should find the .yml file there as well.
9. Click "Deploy Application".
10. Wait the for deployment to be approved.  Once the deployment is approved it will be run within 2 minutes and will be scheduled to run every 24 hours (by default). Note that the results will not be in the Amino index until the interal Amino bitmap jobs have executed
    
Dataloader Example
------------------
An example dataloader solution exists in ezbake-amino/amino-impl.  This is based on the numbers sample dataset which is
a simple dataset that contains the numbers 1 - N.  There are simple features that already exist for processing this data.