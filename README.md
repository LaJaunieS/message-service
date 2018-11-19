# message-service
Modeling a basic messaging service with a client-server exchange. My plan was to model a simple protocol-based messaging service (like SMTP) using a similar 
messaging-service architecture (mta's, etc). 

In its current form, the server controls an AccountManager, which administers operations relating to an account- specifically, authenticating account credentials and, if those credentials are valid, allowing access to an account's messages. The sever also can receive messages sent by a client and (utilizing the Account Manager) can save those messages to the correct Account). The Server allows a Client to send a new message, or to forward a message.  

Each account is associated with a MessageStore, which holds a Collection of messages sent to the account. For now, the Account class, its MessageStore, and the Messages contained in a MessageStore are persisted to a file directory via an implementation of the DAO interface. That may change at some point, and the DAO interface is basic enough so that additional implementations could be allowed: to JSON,XML etc.

A Client will issue commands from the Protocol to the Server, and the Server will act on the Accounts via the AccountManager in response to those commands- i.e., responding that a client's Account credentials are valid, receiving messages from the client and forwarding them to the appropriate Account(s) MessageStore(s), or viewing all messages in the authenticated account.

There are obviously security problems with the current setup, but that's beyond the scope of what I was trying to do- for now. I may try to encrypt connections, messages, etc at some point. I may also try to implement additional ways to interface with the Server- via a Swing GUI or Servlet specifically. I am also aware of the security vulnerabilities with the Input Stream's readObject() method, and am working on integrating a plug-in like SerialKiller to whitelist the particular class that can be sent.

