# message-service
Modeling a basic messaging service with a client-server. My plan was to model a simple protocol-based messaging service (like SMTP) using a similar 
messaging-service architecture (mta's, etc). 

In its current form, the server controls an AccountManager, which administers operations relating to an account- specifically, authenticating account credentials and if those credentials are valid, allowing access to an account's messages, and allowing messages to be sent to other accounts. Each account is associated with a MessageStore, which holds a Collection of messages sent to the account. For now, the Account class, its MessageStore, and the Messages contained in a MessageStore are persisted to a file directory via an implementation of the DAO-interface. That may change at some point.
A Client will issue commands from the Protocol to the Server, and the Server will act on the Accounts via the AccountManager in response to those commands- i.e., responding that a client's Account credentials are valid, receiving messages from the client and forwarding them to the appropriate Account(s) MessageStore(s), or viewing all messages in the authenticated account.
Also, there are obviously security problems with the current setup, but that's beyond the scope of what I was trying to do- for now. I may try to encrypt connections, messages, etc at some point.

