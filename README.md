
📄 Lynq – Code Explanation Document
Overview

This Android app, written in Kotlin using Android Studio, simulates a basic chat interface where:

    You can type messages

    Messages are displayed in a scrollable list

    A fake “bot” replies with an “Echo: …” response

📁 1. Message.kt

data class Message(
    val content: String,
    val isSentByUser: Boolean
)

What it does:

    This is a simple data class used to represent each chat message.

    content: The message text.

    isSentByUser: true if sent by the user, false if it's from the bot.

📁 2. ChatAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

    These are the necessary imports to use RecyclerView, TextView, and inflate layouts.

Adapter Class:

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    ChatAdapter connects the messages data to the UI (RecyclerView).

    It tells how each message should look and behave.

ViewHolder:

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessage: TextView = view.findViewById(R.id.userMessage)
        val botMessage: TextView = view.findViewById(R.id.botMessage)
    }

    Each row in the chat (called an "item") can either be a user message or a bot message.

    The holder holds two TextViews: one for the user, one for the bot.

onCreateViewHolder

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
    return ChatViewHolder(view)
}

    This inflates chat_item.xml — the layout for each individual message bubble.

onBindViewHolder

override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
    val message = messages[position]
    if (message.isSentByUser) {
        holder.userMessage.text = message.content
        holder.userMessage.visibility = View.VISIBLE
        holder.botMessage.visibility = View.GONE
    } else {
        holder.botMessage.text = message.content
        holder.botMessage.visibility = View.VISIBLE
        holder.userMessage.visibility = View.GONE
    }
}

    Shows only the appropriate message bubble for each message.

    If the message is from the user, the bot's bubble is hidden, and vice versa.

getItemCount

override fun getItemCount(): Int = messages.size

    Tells the RecyclerView how many items to show (based on the number of messages).

📁 3. MainActivity.kt

class MainActivity : AppCompatActivity()

    This is the main screen of the app.

Variables

private lateinit var chatRecyclerView: RecyclerView
private lateinit var messageEditText: EditText
private lateinit var sendButton: Button
private val messages = mutableListOf<Message>()
private lateinit var adapter: ChatAdapter

    UI elements: RecyclerView (message list), EditText (typing), Button (send)

    messages: a list storing the chat messages

    adapter: connects data to the RecyclerView

onCreate

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    Sets the screen to use the layout activity_main.xml

    chatRecyclerView = findViewById(R.id.chatRecyclerView)
    messageEditText = findViewById(R.id.messageEditText)
    sendButton = findViewById(R.id.sendButton)

    Links each UI component to your Kotlin code.

    adapter = ChatAdapter(messages)
    chatRecyclerView.layoutManager = LinearLayoutManager(this)
    chatRecyclerView.adapter = adapter

    Prepares the RecyclerView to use the adapter and show messages in a vertical list.

Button Logic

sendButton.setOnClickListener {
    val messageText = messageEditText.text.toString()
    if (messageText.isNotBlank()) {
        messages.add(Message(messageText, true))  // Sent by user
        messages.add(Message("Echo: $messageText", false)) // Fake bot reply
        adapter.notifyItemRangeInserted(messages.size - 2, 2)
        chatRecyclerView.scrollToPosition(messages.size - 1)
        messageEditText.text.clear()
    }
}

    When you press "Send":

        It adds your message and a fake "echo" reply.

        Notifies the adapter to refresh the list.

        Scrolls to the newest message.

        Clears the input field.

📄 4. Layout Files
activity_main.xml

Contains:

    RecyclerView for the chat

    EditText for typing

    Button for sending

Example:

<LinearLayout>
    <RecyclerView android:id="@+id/chatRecyclerView"/>
    <EditText android:id="@+id/messageEditText"/>
    <Button android:id="@+id/sendButton"/>
</LinearLayout>

chat_item.xml

Defines one row in the chat (user or bot message):

<TextView android:id="@+id/userMessage" android:layout_gravity="end"/>
<TextView android:id="@+id/botMessage" android:layout_gravity="start"/>

    Only one message is shown per row — the other is hidden depending on who sent it.

🧪 Testing Tips

    Run on emulator or device

    Type in the EditText and press Send

    You’ll see your message and a bot reply appear

📁 Project Structure Summary

Lynq/
├── app/
│   ├── java/com/example/Lynq/
│   │   ├── MainActivity.kt
│   │   ├── Message.kt
│   │   ├── ChatAdapter.kt
│   ├── res/layout/
│   │   ├── activity_main.xml
│   │   ├── chat_item.xml
│   ├── res/values/
│   │   ├── themes.xml
│   ├── AndroidManifest.xml

