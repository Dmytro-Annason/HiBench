# MapR Spark HiBench

## Prepare cluster

* Change YARN site

Append the following properties to `yarn-site` on each node in order to change default memory configs:
```
$ vim /opt/mapr/hadoop/hadoop-2.7.6/etc/hadoop/yarn-site.xml
------------------------------------------------------------

...

<property>
  <name>yarn.nodemanager.vmem-pmem-ratio</name>
  <value>4.0</value>
</property>
<property>
  <name>yarn.scheduler.maximum-allocation-mb</name>
  <value>65536</value>
</property>
<property>
  <name>yarn.nodemanager.pmem-check-enabled</name>
  <value>false</value>
</property>
<property>
  <name>yarn.nodemanager.vmem-check-enabled</name>
  <value>false</value>
</property>

```


* Change mapred-site

Append the following properties to `mapred-site` on each node in order to change default memory configs:
```
$ vim /opt/mapr/hadoop/hadoop-2.7.6/etc/hadoop/mapred-site.xml
------------------------------------------------------------

...

<property>
  <name>mapreduce.map.memory.mb</name>
  <value>8192</value>
</property>
<property>
  <name>mapreduce.reduce.memory.mb</name>
  <value>8192</value>
</property>

```


* Restart ResourceManager and NodeManager

Restart ResourceManager on each RM node. The command below lists all performance cluster node, so depending on the 
cluster's topology you will face warnings in the command output.

```
maprcli node services -action restart -name resourcemanager -nodes atsqa8c232.qa.lab,atsqa8c233.qa.lab,atsqa8c234.qa.lab,atsqa8c235.qa.lab,atsqa8c236.qa.lab,atsqa8c237.qa.lab,atsqa8c238.qa.lab,atsqa8c239qa.lab,atsqa8c240.qa.lab,atsqa8c241.qa.lab,atsqa8c242.qa.lab
```

Restart Nodemanager in the same way:
```
maprcli node services -action restart -name nodemanager -nodes atsqa8c232.qa.lab,atsqa8c233.qa.lab,atsqa8c234.qa.lab,atsqa8c235.qa.lab,atsqa8c236.qa.lab,atsqa8c237.qa.lab,atsqa8c238.qa.lab,atsqa8c239qa.lab,atsqa8c240.qa.lab,atsqa8c241.qa.lab,atsqa8c242.qa.lab
```


## Build

* Install git, maven, screen 

On the node with Spark and Kafka installed execute the following command to install `git`, `maven` and `screen`:

```
$ sudo yum install -y git maven screen
```

* Get HiBench


```
$ git clone https://github.com/dsergeevmapr/HiBench.git

$ cd HiBench

$ git checkout maprspark-branch-3.1.1
```

* Configuration

`maprspark-branch-3.1.1` contains HiBench configured for Spark 3.1.1 on 6.2.0 core. If you want to run tests for 
another version of Spark or MapR core you have to define new Maven profile in 
[HiBench root pom](https://github.com/dsergeevmapr/HiBench/blob/maprspark-branch-3.1.1/pom.xml#L202) and modify 
[build.sh](https://github.com/dsergeevmapr/HiBench/blob/maprspark-branch-3.1.1/build.sh#L5) to use your newly-created 
profile instead of `spark2.4.4.0`.

Also [hibench.conf](https://github.com/dsergeevmapr/HiBench/blob/maprspark-branch-3.1.1/conf/hibench.conf#L88) and 
[spark.conf](https://github.com/dsergeevmapr/HiBench/blob/maprspark-branch-3.1.1/conf/spark.conf#L2) contain Kafka and 
Spark location, which must be changed in the case of version change.

Note that `hibench.streambench.zkHost` property of 
[hibench.conf](https://github.com/dsergeevmapr/HiBench/blob/maprspark-branch-3.1.1/conf/hibench.conf) specifies 
ZooKeeper nodes.

Other properties can be left unchanged.

* Build HiBench

Build HiBench with the following command:
```
$ ./build.sh spark
```

## Run

* Obtain MapR ticket 

```
$ maprlogin password
```

* Run tests

Use screen utility, since your connection to performance node can be interrupted with `Broken pipe` error:
```
packet_write_wait: Connection to x.x.x.x port 22: Broken pipe
```

Run tests as follows:
```
$ screen

$ ./bin/run_mapr_spark.sh
```

## Collect reports

Test results will be contained at `HiBench/report` directory.
