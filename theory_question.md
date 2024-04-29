# Theory Questions

### 1.6.1. Annotation vs. XML Declarations

In the previous tasks you already gained some experiences using annotations and XML. What are the benefits and drawbacks
of each approach? In what situations would you use which one? Hint: Think about maintainability and the different roles
usually involved in software projects.

#### Advantages of Annotations:

- **Simple to Understand**: Annotations are directly written in Java code
- **Compile-Time Safety**: Annotations undergo compile-time checks, minimizing the risk of errors.
- **Everything in one place**: Entity relations are directly defined where the entities and their properties are
  defined.

#### Disadvantages of Annotations:

- **Tight Coupling**: Annotations can tightly couple code and configuration, making changes more challenging.
- **Scalability Issues**: Managing annotations becomes more difficult as projects grow in size.

#### Advantages of XML Configuration:

- **Separation of Concerns**: XML separates configuration from code, simplifying modification.
- **Externalization Capability**: XML configurations can reside outside the codebase, easing management and deployment.

#### Disadvantages of XML Configuration:

- **Harder to Understand**: XML configurations can be harder to read and understand compared to using annotations.
- **Runtime Error Risk**: Errors in XML configurations may surface only during runtime, leading to unexpected failures.

In essence, annotations are good for simple configurations within code, while XML is better suited for more complex
setups and when separating configuration from code is important.

How to choose for software projects?

1. **Project Requirements:** Consider the complexity of the project and the level of flexibility required for
   configurations
2. **Team Preferences and Skills**
3. **Maintainability:** Consider long-term maintainability concerns

### 1.6.2. Entity Manager and Entity Lifecycle

What is the lifecycle of a JPA entity, i.e., what are the different states an entity can be in? What EntityManager
operations change the state of an entity? How and when are changes to entities propagated to the database?

### Entity Lifecycle

![Entity Lifecycle](./theory_ressources/Lifecycle-Model-1024x576.webp)

JPA entities undergo various states as they interact with the persistence context:

- **Transient (new)**: Entities are newly created only exist in memory and haven't been persisted to the database yet.

- **Managed**: Entities become managed when they are associated with the persistence context. This occurs
  after retrieval from the database or persistence through `EntityManager`. In this state, any changes made to the
  entity are automatically tracked by the persistence context and synchronized with the database during commit or flush
  operations.

- **Detached**: Entities that were previously managed but are no longer associated with the persistence context enter
  the detached state. This can happen when the context is closed, or the entity is explicitly detached
  using `EntityManager.detach()`. While detached, changes made to the entity are not automatically synchronized with the
  database.

- **Removed**: Managed entities marked for removal using `EntityManager.remove()` transition to the removed state.
  Although they remain in memory until the next commit or flush operation, they are scheduled for deletion from the
  database.

### EntityManager Operations and State Changes

The state of an entity can be altered through various EntityManager operations:

- **Persist**: Transient -> Managed

- **Merge**: Detached -> Managed

- **Remove**: Managed -> Remove

- **Find/Query**: DB -> Managed

- **Detach**: Managed -> Detached

### Propagation of Changes to the Database

On flush() the required SQL statements are generated and executed.

 - Persist -> SQL INSERT/UPDATE
 - Remove -> SQL DELETE

https://thorben-janssen.com/entity-lifecycle-model/

### 1.6.3. Optimistic vs. Pessimistic Locking

The database systems you have used in this assignment provide different types of concurrency control mechanisms. Redis,
for example, provides the concept of optimistic locks. The JPA EntityManager allows one to set a pessimistic read/write
lock on individual objects. What are the main differences between these locking mechanisms? In what situations or use
cases would you employ them? Think of problems that can arise when using the wrong locking mechanism for these use
cases.

### Locking Mechanisms Overview

#### Optimistic Locking

Assumes conflicts between transactions are rare. It allows multiple transactions to read and modify the same data
concurrently without locking it. In optimistic locking, before committing changes, a transaction checks if the data it
modified has been altered by another transaction since it was last read. If so, the transaction aborts to
prevent conflicting changes.

#### Pessimistic Locking

Assumes conflicts are likely and acquires locks to prevent concurrent access to data. It allows only one transaction to
access and modify the data at a time. Locks are acquired explicitly, preventing other transactions from accessing the
locked data until the lock is released.

### When to use which Locking Mechanism?

- **Optimistic Locking**:
    - Best for situations with **infrequent conflicts** or where system performance is important.
    - Suitable for more reads than writes or when conflicts can be resolved without locking.

- **Pessimistic Locking**:
    - Ideal for scenarios with **frequent conflicts**, where ensuring **data consistency is crucial**.
    - Useful for preventing simultaneous access to important data or ensuring real-time data consistency.

### Potential Issues with Incorrect Usage

In situations where conflicts are frequent, optimistic locking might lead to a higher number of conflicts and subsequent
transaction rollbacks, thus impacting system performance. On the other hand, when conflicts are rare, using pessimistic
locking may introduce unnecessary resource usage and reduce concurrency, potentially affecting system efficiency.
Moreover, failure to adequately address conflicts by choosing an unsuitable locking mechanism or neglecting conflict
resolution strategies can result in data inconsistencies and transaction failures.

### 1.6.4. Database Scalability

How can we address system growth, i.e., increased data volume and query operations, in databases? Hint: vertical vs.
horizontal scaling. What methods in particular do MongoDB and Redis provide to support scalability?

To address the challenges of system growth, particularly concerning increased data volume and query operations in
databases, two primary scaling strategies are commonly employed: vertical scaling and horizontal scaling.

- **Vertical Scaling**: This approach involves enhancing the capacity of a single server by adding more resources like
  CPU, memory, or storage. While vertical scaling can provide immediate improvements, it has limitations as it may
  become costly and eventually encounter hardware limitations.

- **Horizontal Scaling**: In contrast, horizontal scaling involves expanding the database system by adding more servers.
  This method distributes the workload across multiple machines, offering better scalability potential. It enables the
  system to manage larger data volumes and handle increased query operations effectively.

Both MongoDB and Redis offer features to support horizontal scaling:

- **Redis**:
    - **Cluster Mode**: Redis facilitates horizontal scaling through its cluster mode, which distributes data across
      multiple Redis instances or nodes. Each node manages a subset of the data, enabling Redis to handle larger
      datasets and increased query loads effectively.
    - **Replication**: Redis follows a master-replica architecture for replication. The master node is the primary Redis
      instance responsible for handling read and write operations, while replica nodes replicate data from the master.

- **MongoDB**:
    - **Sharding**: MongoDB supports horizontal scaling through sharding, which partitions data across multiple servers
      or shards. This distribution of data allows MongoDB to distribute read and write operations across nodes,
      enhancing performance and scalability.
    - **Replication**: MongoDB also supports replication, where data is duplicated across multiple nodes for redundancy
      and fault tolerance. This ensures data availability even if a node fails.

## Additonal Questions

### 1.1.2 Inheritance Mapping

During the discussion sessions you should be able to explain the different inheritance patterns and discuss your
specific choice.
https://javaee.github.io/tutorial/persistence-intro003.html#BNBQN
((https://www.baeldung.com/hibernate-inheritance))

* Abstract Entities ... can not be instatiated but normally queried (like a own table).
* Mapped Supperclass ... liked mapped entities "inlined" into extending subtype.
* Non-Entity Superclasses ... their members are not persistent.

### 1.2.2 Complex Named Queries

You do not need to find a single query to solve this task (you can use a combination of Java code and named queries),
but you have to keep ORM-performance in mind, i.e., make sure that your solution is also reasonably fast if you have
many entities. During the discussion session you should be able to explain what types of problems can arise with badly
written queries.

### 1.3.3 Datasource

Please familiarize yourself with the concepts of connection creation in jOOQ. You should know which features/options our
implementation activates and what that means for the execution. Specifically, look at the module’s pom.xml (e.g., which
jOOQ arguments are present) and how the DSLContext from getConnection is created.

### 1.3.4 jOOQ DAOs

Make sure that all methods that all write operations are done in a transaction and therefore executed at once. You
should be able to answer during the group interview how you achieved this and also what other guarantees you can make
about the execution (e.g., think about the data source).

### 1.5.1 Data Structures

In the discussion sessions, you should explain the data structures you used, and why you chose them over others that
Redis provides.