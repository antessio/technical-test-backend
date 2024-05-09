# Comments and possible improvements

This code is a POC and assumes that it's not possible to take advantage of Stripe webhooks.

There are some challenges in the current implementation:

- it doesn't use any transaction isolation mechanism, the concurrency is handled with a lock. 
The lock implementation uses the database to store the lock with an expiration.   
While this approach guarantees the consistency, it has some drawbacks:
  - for a wallet update it performs 3 updates: lock, update, unlock
  - the synchronous approach implies that a client has to wait for both the database updates and the Stripe HTTP call to finish
- in a distributed ecosystem, it would be beneficial to follow a more asynchronous approach described in [Async Flow](./async_flow.puml)
  - with this approach the client has to wait only for the database insert, then it gets a pending status and will be eventually notified about the result
  - on the other hand, this solution adds complexity and requires a message broker