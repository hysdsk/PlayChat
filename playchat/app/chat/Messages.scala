package chat

sealed trait Message

case class Join(user: String) extends Message

case class Leave(user: String) extends Message

case class Speech(user: String, text: String) extends Message