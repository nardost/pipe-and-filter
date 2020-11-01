
# A Text Processing Application
## (Pipe-and-Filter Architectural Pattern)

### 1. What it Does

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

The building-blocks of the application are pumps, filters, pipes, and sinks. The assembly of the building blocks as one unit is represented by a ```Pipeline``` object.

Interfaces: ```Pipe, Pump, Filter, Sink, Pipeline```

#### 3.1 Pipe

Pipes are buffers that use a blocking queue as their underlying data structure. All pipes implement the ```Pipe``` interface.
```java
public interface Pipe<T> {
   T take() throws InterruptedException;
   void put(T t) throws InterruptedException;
}
```

A ```Pipe``` factory uses the Java reflection API to build ```Pipe``` objects dynamically. The ```PipeFactory``` takes the type of the ```Pipe``` as an input parameter to determine which specific type of ```Pipe``` to create. The type parameter in the interface definition represents the data type that the ```Pipe``` accommodates.

The ```Pipe``` types the ```PipeFactory``` is currently aware of are:
```
1. java.lang.String
2. pipefilter.filter.TermFrequency
3. java.lang.Integer (not used in this application)
4. java.lang.Double (not used in this application)
```

The buffer capacity of pipes is configurable with the global ```PIPE_CAPACITY``` configuration parameter.

#### 3.2 Pump

Pumps are active elements ```(Runnable)``` and implement the ```Pump``` interface.
```java
public interface Pump<T, U> extends Runnable {
    void pump();
}
```

The type parameters in the interface represent the input and the output data types of the ```Pump``` implementation.

A ```Pump``` factory dynamically builds pumps by using the Java reflection API. The ```PumpFactory``` expects all pumps that implement the ```Pump``` interface to have a single constructor with three arguments:

- 1st argument: Input to the pump
- 2nd argument: Output ```Pipe``` of the pump
- 3rd argument: A countdown latch to signal completion of operations

Implemented pumps: ```TextFilePump```

#### 3.3 Filter

Filters are active elements ```(Runnable)``` that implement the ```Filter``` interface.
```java
public interface Filter<T, U> extends Runnable {
    void filter();
}
```
The type parameters in the interface definition represent the input ```Pipe``` type and the output ```Pipe``` type of the ```Filter``` implementation.

A ```Filter``` factory builds filters using the Java reflection API. The ```FilterFactory``` expects all filters that implement the ```Filter``` interface to have a single constructor with three arguments:

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

Sinks are active elements ```(Runnable)``` that implement the ```Sink``` interface.
```java
public interface Sink<T, U> extends Runnable {
    void drain();
}

```

The type parameters in the interface definition represent the input ```Pipe``` type and the output data structure type of the ```Sink``` implementation.

A ```Sink``` factory uses the Java reflection API to build ```Sink``` objects. The ```SinkFactory``` expects all sinks that implement the ```Sink``` interface to have a single constructor with three arguments.

- 1st argument: Input pipe
- 2nd argument: Output data structure
- 3rd argument: A countdown latch to signal completion of operations

Implemented sinks: ```FrequencyTermInverter```

#### 3.5 Pipeline

A ```Pipeline``` represents an ordered assembly of a Pump, a series of Filters, and a ```Sink``` chained together. A ```Pipeline``` implements the ```Pipeline``` interface.

```java
public interface Pipeline {
    void run() throws InterruptedException;
}
```

Viewed as a black-box, a ```Pipeline``` is just some kind of engine that takes an input and produces an output. The _input_ and the _output_ thus characterize a ```Pipeline``` in addition to an _ordered list of internal components_ and a _pipeline assembly type_.

A ```Pipeline``` factory takes the _input_, the _output_, the _ordered list of ```Pipeline``` components_, and the _type of ```Pipeline``` assembly_ as input parameters and builds a ```Pipeline``` object.

**Implemented pipelines**

There is currently only one type of ```Pipeline``` assembly, ```serial```, implemented by the ```SerialPipeline``` class, where components are assembled in a single sequential chain.

### 4. The Registry

Each implemented Pump, Filter, or ```Sink``` is registered in a central Registry under a unique identifier.

#### 4.1 Registered Components

| **UNIQUE IDENTIFIER** | **CLASS** | **TYPE** |
| --- | --- | --- |
| ```tokenizer``` | ```WordBoundaryTokenizer``` | ```Filter``` |
| ```non-alphanumeric-word-remover``` | ```NonAlphaNumericWordRemover``` | ```Filter``` |
| ```numeric-only-word-remover``` | ``NumericOnlyWordRemover`` | ```Filter``` |
| ```to-lower-case-transformer``` | ```ToLowerCaseTransformer``` | ```Filter``` |
| ```stop-word-remover``` | ```StopWordRemover``` | ```Filter``` |
| ```opennlp-porter-stemmer``` | ```OpenNLPStemmer``` | ```Filter``` |
| ```en-porter-stemmer``` | ```PorterStemmer``` | ```Filter``` |
| ```term-frequency-counter``` | ```TermFrequencyCounter``` | ```Filter``` |
| ```text-streamer``` | ```TextFilePump``` | ```Pump``` |
| ```frequency-term-inverter``` | ```FrequencyTermInverter``` | ```Sink``` |


#### 4.2 Importance of the Registry

```PumpFactory```, ```FilterFactory```, ```SinkFactory``` use the Registry to build components dynamically using the Java reflection API. These factories also use the ```Registry``` to infer the input and the output types of each registered pump, filter, or sink.

A factory consults the registry and knows the class type. It then accesses the single constructor of that class type and instantiates an object of that class type by reflection.

```PiplineFactory``` uses the ```Registry``` to check if a given ```Pipeline``` assembly is valid. The user supplied ordered list of components is a valid ```Pipeline``` assembly if and only if the output type of a ```Pipeline``` component is the same as the input type of the next component in the chain for every pair of adjacent components in the list.

### 5. The Text Processing Pipeline

The ```Pipeline``` assembly for the text processor that does the functions listed in section 1 above is constructed with the following sequence of components:
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

All configuration parameters are public class variables of the ```Configuration``` class.

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
| Custom exceptions | ```pipefilter.exception``` |

# Part 2

## Extensibility

1. The customer wishes to redesign the system to handle text files written in languages other than English.
2. The design time modification must take less than one day.
3. The ultimate solution must be configurable automatically at runtime.

The only language specific component in the design is the stemmer filter. The customer has, therefore, just a single task to do - implement a stemmer filter for that language in the same fashion as specified for classes that implement the Filter interface, i.e. a single constructor with three arguments.

Once the stemmer for the non-English language has been implemented in the manner required by this design, all the customer has to do is register the new filter (the stemmer) into the Registry (with a unique identifier) and use it.

The extensive use of the _Java reflection API_ and the _Factory Design Pattern_ in the design makes it possible for a user to plug in new components without having to change the design.

**How my Solution Supports the Extensibility Goals**

1. All the customer has to do is implement a new stemmer filter, which will then be registered and plugged into a pipeline. **No system redesign is required**.
2. Since the design does not have to be modified, the _less-than-one-day_ requirement can easily be met.
3. My solution is designed to allow the customer to cherry-pick components for a pipeline at runtime. The customer just constructs an array of component identifiers, which is changeable at runtime.

For example, if the new stemmer is given the unique identifier "stemmer-de" (a stemmer for the German language), the customer can construct the pipeline with the following array of component identifiers:

```javascript
{
  "text-streamer",
  "tokenizer",
  "non-alphanumeric-word-remover",
  "numeric-only-word-remover",
  "to-lower-case-transformer",
  "stop-word-remover",
  "stemmer-de",
  "term-frequency-counter",
  "frequency-term-inverter"
}
```

## Three Small Task Filters Merged into One

Three small task filters were merged into a new filter that does all three tasks as one. This was done with the idea that reducing the number of components that block on input/output pipes whenever possible may improve the response time.


## Task Executor & Thread Pool Instead of Explicit Threads

The active component threads in Part I were explicit threads. In Part II a fixed thread pool is used to execute the active components because the exact number of threads in a pipeline is known in advance.

## Parallel Pipeline

In Part I of this project, there was only one pipeline type – serial. A parallel pipeline was implemented in this part to see how it will improve (or make worse) overall performance.

To achieve parallelism in a seamless manner (i.e. without having to alter my earlier design), I introduced two special filters that serve as adapters – Parallelizer and Serializer.

1. Parallelizer spreads an incoming stream out into several parallel streams.
2. Serializer collects a number of parallel streams into a single stream.
3. Both Parallelizer and Serializer implement the Filter interface, so they are treated just like any other Filter.
4. Parallelizer and Serializer are not registered in the public Registry, so they are not directly available to the user. The user will have to choose the parallel pipeline type to construct a parallel pipeline.
5. The design does not parallelize a Pump or a Sink – only Filters are parallelizable.
6. Parallelizable Filters are registered in the public Registry.


## Known Issues with the  ```ParallelPipeline```  Implementation

1. The ParallelPipeline implementation code looks convoluted, and no unit test is written for it, but it works for demonstration.
2. The program gets stuck when _low pipe capacity_ is combined with _high number of parallel streams_. It could be a resources shortage issue or some bug that I could not figure out. For example, for user inputs type=parallel, capacity=256, streams=2, the program executed with a response time of _4005 ms_, whereas it got stuck for inputs type=parallel, capacity=128, streams=2.
3. It is assumed that all parallelizable filters have java.lang.String inputs and outputs. The dynamic creation of different types of Pipes is not implemented in ParallelPipeline.
4. No visible improvement in performance. In fact, it appears to be slower that the serial pipeline.

## Bottlenecks

It is interesting to note from the various tables printed in this report that the overall response time of the pipeline is almost the same as the individual response times of the components. This is not counter intuitive because the components are connected in series, and the overall progress can only be as fast as the slowest component. Some components are inherently slow or non-parallelizable and may be choke points in the pipeline.

1. text-streamer

The pump reads a text file from the system, and file I/O is inherently slow. That makes the pump, text-streamer, a bottleneck to the pipeline.

2. Low pipe capacity

There seems to be an optimal pipe capacity below which the performance of the pipeline is severely hampered.

3. term-frequency-counter

The term counter (term-frequency-counter) does not seem to have exploitable parallelism. Term counting has to be done at a single point, or there has to be additional modification to synchronize counts by multiple threads. Therefore, the filter term-frequency-counter cannot be parallelized and is a potential bottleneck.

4. frequency-term-inverter

Frequency-term inversion has to be done at a single point, or there needs to be some additional modification to integrate all the _frequency-to-list-of-terms_ maps constructed by several threads. Therefore, the filter term-frequency-counter is not parallelizable and is a potential bottleneck.

## Running the Program

The program is modified to take the pipe type and the number of parallel streams as user inputs. Run the program as (values are for example only):

```$ java -jar executable.jar filename.txttype parallel capacity 256 streams 3```

Note: The first argument is compulsory and must always be the input file path. The rest are optional and could come in any order in the form key1 value1 key2 value2 … (whitespace separated). The program parameters and their possible values are:

| **KEY** | **VALID VALUES** | **DEFAULT VALUE** |
| --- | --- | --- |
| type | { serial, parallel } | serial |
| capacity | Positive integer | 1024 |
| streams | Positive integer | 2 |

## Pipeline Output for kjbible.txt
```
---------------------
FREQUENCY TERMS
---------------------
8006 -> { lord }
4716 -> { god }
4600 -> { thy }
3983 -> { ye }
3843 -> { will }
3827 -> { thee }
3486 -> { son }
2884 -> { king }
2735 -> { man }
2615 -> { dai }
---------------------
```
