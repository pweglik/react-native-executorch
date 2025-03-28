package com.swmansion.rnexecutorch.utils.llms

enum class ChatRole {
  SYSTEM,
  USER,
  ASSISTANT,
}

const val BEGIN_OF_TEXT_TOKEN = "<|begin_of_text|>"
const val END_OF_TEXT_TOKEN = "<|eot_id|>"
const val START_HEADER_ID_TOKEN = "<|start_header_id|>"
const val END_HEADER_ID_TOKEN = "<|end_header_id|>"

class ConversationManager(
  private val numMessagesContextWindow: Int,
  systemPrompt: String,
  messageHistory: Array<Map<String, String>>,
) {
  private val basePrompt: String
  private val messages = ArrayDeque<String>()

  init {
    this.basePrompt =
      BEGIN_OF_TEXT_TOKEN +
      getHeaderTokenFromRole(ChatRole.SYSTEM) +
      systemPrompt +
      END_OF_TEXT_TOKEN +
      getHeaderTokenFromRole(ChatRole.USER)

    messageHistory.forEach { message ->
      when (message["role"]) {
        "user" -> addResponse(message["content"]!!, ChatRole.USER)
        "assistant" -> addResponse(message["content"]!!, ChatRole.ASSISTANT)
      }
    }
  }

  fun addResponse(
    text: String,
    senderRole: ChatRole,
  ) {
    if (this.messages.size >= this.numMessagesContextWindow) {
      this.messages.removeFirst()
    }
    val formattedMessage: String =
      if (senderRole == ChatRole.ASSISTANT) {
        text + getHeaderTokenFromRole(ChatRole.USER)
      } else {
        text + END_OF_TEXT_TOKEN + getHeaderTokenFromRole(ChatRole.ASSISTANT)
      }
    this.messages.add(formattedMessage)
  }

  fun getConversation(): String {
    val prompt = StringBuilder(this.basePrompt)
    for (elem in this.messages) {
      prompt.append(elem)
    }
    return prompt.toString()
  }

  private fun getHeaderTokenFromRole(role: ChatRole): String =
    when (role) {
      ChatRole.SYSTEM -> START_HEADER_ID_TOKEN + "system" + END_HEADER_ID_TOKEN
      ChatRole.USER -> START_HEADER_ID_TOKEN + "user" + END_HEADER_ID_TOKEN
      ChatRole.ASSISTANT -> START_HEADER_ID_TOKEN + "assistant" + END_HEADER_ID_TOKEN
    }
}
