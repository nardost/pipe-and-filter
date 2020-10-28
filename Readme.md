
# A Text Processing Application
## (Pipe-and-Filter Architectural Pattern)

Github Repository: [_https://github.com/nardost/pipe-and-filter_](https://github.com/nardost/pipe-and-filter)

### 1. What it Does

The application:

1. Reads a text file line by line
2. Splits the lines into words
3. Removes non-alphabetic characters (including numeric characters)
4. Transforms the text to lower case
5. Removes stop words
6. Stems words into their root terms
7. Counts the frequency of occurrence of the root terms
8. Collects terms together by frequency of occurence
9. Prints the ten most frequently occurring terms.

### 2. Testing/Building/Running

The application is a Maven application that produces a single executable JAR file.

**Running Unit Tests**

```$ mvn test```

**Packaging**

```$ mvn clean package```

**Running**

```$ java -jar target/pipe-filter-1.0.0-jar-with-dependencies.jar file-name.txt```

### 3. Components

The building-blocks of the application are pumps, filters, pipes, and sinks. The assembly of the building blocks as one unit is represented by a pipeline object.

Interfaces: ```Pipe, Pump, Filter, Sink, Pipeline```

#### 3.1 Pipe

Pipes are buffers that use a blocking queue as their underlying data structure. All pipes implement the ```Pipe``` interface.
```java
public interface Pipe<T> {
   T take() throws InterruptedException;
   void put(T t) throws InterruptedException;
}
```

A ```Pipe``` factory uses the Java reflection API to build ```Pipe``` objects dynamically. The PipeFactory takes the type of the ```Pipe``` as an input parameter to determine which specific type of ```Pipe``` to create. The type parameter in the interface definition represents the data type that the ```Pipe``` accommodates.

The ```Pipe``` types the PipeFactory is currently aware of are:
```
1. java.lang.String
2. pipefilter.filter.TermFrequency
3. java.lang.Integer _(not used in this application)_
4. java.lang.Double _(not used in this application)_
```

The buffer capacity of pipes is configurable with the global PIPE\_CAPACITY configuration parameter.

#### 3.2 Pump

Pumps are active elements (Runnable) and implement the ```Pump``` interface.
```java
public interface Pump<T, U> extends Runnable {
    void pump();
}
```

The type parameters in the interface represent the input and the output data types of the ```Pump``` implementation.

A ```Pump``` factory dynamically builds pumps by using the Java reflection API. The PumpFactory expects all pumps that implement the ```Pump``` interface to have a single constructor with three arguments:

- 1st argument: Input to the pump
- 2nd argument: Output ```Pipe``` of the pump
- 3rd argument: A countdown latch to signal completion of operations

Implemented pumps: ```TextFilePump```

#### 3.3 Filter

Filters are active elements (Runnable) that implement the ```Filter``` interface.
```java
public interface Filter<T, U> extends Runnable {
    void filter();
}
```
The type parameters in the interface definition represent the input ```Pipe``` type and the output ```Pipe``` type of the ```Filter``` implementation.

A ```Filter``` factory builds filters using the Java reflection API. The FilterFactory expects all filters that implement the ```Filter``` interface to have a single constructor with three arguments:

- 1st argument: Input pipe
- 2nd argument: Output pipe
- 3rd argument: A countdown latch to signal completion of operations

**Implemented Filters** _(class names are descriptive of their functions)_
```
1. NonAlphaNumericWordRemover
2. NumericOnlyWordRemover
3. OpenNLPStemmer (uses the OpenNLP Porter stemmer)
4. PorterStemmer (uses the Porter stemmer version given by the instructor)
5. StopWordRemover
6. TermFrequencyCounter
7. ToLowerCaseTransformer
8. WordBoundaryTokenizer
```

#### 3.4 Sink

Sinks are active elements (Runnable) that implement the ```Sink``` interface.
```java
public interface Sink<T, U> extends Runnable {
    void drain();
}

```

The type parameters in the interface definition represent the input ```Pipe``` type and the output data structure type of the ```Sink``` implementation.

A ```Sink``` factory uses the Java reflection API to build ```Sink``` objects. The SinkFactory expects all sinks that implement the ```Sink``` interface to have a single constructor with three arguments.

- 1st argument: Input pipe
- 2nd argument: Output data structure
- 3rd argument: A countdown latch to signal completion of operations

Implemented sinks:
```
1. FrequencyTermInverter
2. TermFrequencyCounter (_not used in this application_)
```

#### 3.5 Pipeline

A pipeline represents an ordered assembly of a Pump, a series of Filters, and a ```Sink``` chained together. A pipeline implements the Pipeline interface.

```java
public interface Pipeline {
    void run() throws InterruptedException;
}
```

Viewed as a black-box, a pipeline is just some kind of engine that takes an input and produces an output. The _input_ and the _output_ thus characterize a pipeline in addition to an _ordered list of internal components_ and a _pipeline assembly type_.

A pipeline factory takes the _input_, the _output_, the _ordered list of pipeline components_, and the _type of pipeline assembly_ as input parameters and builds a pipeline object.

**Implemented pipelines**

There is currently only one type of pipeline assembly, serial, implemented by the SerialPipeline class, where components are assembled in a single sequential chain.

### 4. The Registry

Each implemented Pump, Filter, or ```Sink``` is registered in a central Registry under a unique identifier.

#### 4.1 Registered Components

| **UNIQUE IDENTIFIER** | **CLASS** | **TYPE** |
| --- | --- | --- |
| ```tokenizer``` | ```WordBoundaryTokenizer``` | ```Filter``` |
| ```non-alphanumeric-word-remover``` | ```NonAlphaNumericWordRemover``` |
| ```numeric-only-word-remover``` | ``NumericOnlyWordRemover`` |
| ```to-lower-case-transformer``` | ```ToLowerCaseTransformer``` |
| ```stop-word-remover``` | ```StopWordRemover``` |
| ```opennlp-porter-stemmer``` | ```OpenNLPStemmer``` |
| ```en-porter-stemmer``` | ```PorterStemmer``` |
| ```term-frequency-counter``` | ```pipefilter.filter.TermFrequencyCounter``` |
| ```text-streamer``` | T```extFilePump``` | ```Pump``` |
| ```frequency-counter``` | ```TermFrequencyCounter``` | ```Sink``` |
| ```frequency-term-inverter``` | ```FrequencyTermInverter``` |


#### 4.2 Importance of the Registry

PumpFactory, FilterFactory, SinkFactory use the Registry to build components dynamically using the Java reflection API. These factories also use the Registry to infer the input and the output types of each registered pump, filter, or sink.

A factory consults the registry and knows the class type. It then accesses the single constructor of that class type and instantiates an object of that class type by reflection.

PiplineFactory uses the Registry to check if a given pipeline assembly is valid. The user supplied ordered list of components is a valid pipeline assembly if and only if the output type of a pipeline component is the same as the input type of the next component in the chain for every pair of adjacent components in the list.

### 5. The Text Processing Pipeline

The pipeline assembly for the text processor that does the functions listed in section 1 above is constructed with the following sequence of components:
```javascript

    {
        "text-streamer",
        "tokenizer",
        "non-alphanumeric-word-remover",
        "numeric-only-word-remover",
        "to-lower-case-transformer",
        "stop-word-remover",
        "en-porter-stemmer",
        "term-frequency-counter",
        "frequency-term-inverter"
    }

```
### 6. Configuration

All configuration parameters are public class variables of the Configuration class.

| **PARAMETER** | **DESCRIPTION** |
| --- | --- |
| ```SENTINEL_VALUE``` | A string that is used to signal the end of the text stream. |
| ```PIPE_CAPACITY``` | The buffer size of the pipes (same for all) |
| ```STOP_WORDS``` | An array of stop words |

### 7. Locations of Classes

| **CLASS** | **IN PACKAGE** |
| --- | --- |
| Registry | ```pipefilter.config``` |
| Configuration | ```pipefilter.config``` |
| All Filters | ```pipefilter.filter``` |
| All Pumps | ```pipefilter.pump``` |
| All Sinks | ```pipefilter.sink``` |
| Pipes | ```pipefilter.pipe``` |
| Pipelines | ```pipefilter.pipeline``` |
| PipeFilterException | ```pipefilter.exception``` |
