package org.elasticmq.actor.queue

import org.elasticmq._
import org.elasticmq.msg._
import org.elasticmq.util.Logging
import org.elasticmq.actor.reply._

trait QueueActorQueueOps extends Logging {
  this: QueueActorStorage =>

  def receiveAndReplyQueueMsg[T](msg: QueueQueueMsg[T]): ReplyAction[T] = msg match {
    case GetQueueData() => queueData
    case UpdateQueueDefaultVisibilityTimeout(newDefaultVisibilityTimeout) =>
      logger.info(s"${queueData.name}: Updating default visibility timeout to $newDefaultVisibilityTimeout")
      queueData = queueData.copy(defaultVisibilityTimeout = newDefaultVisibilityTimeout)
    case UpdateQueueDelay(newDelay) =>
      logger.info(s"${queueData.name}: Updating delay to $newDelay")
      queueData = queueData.copy(delay = newDelay)
    case UpdateQueueReceiveMessageWait(newReceiveMessageWait) =>
      logger.info(s"${queueData.name}: Updating receive message wait to $newReceiveMessageWait")
      queueData = queueData.copy(receiveMessageWait = newReceiveMessageWait)
    case UpdateQueueRedrivePolicy(newPolicy) =>
      val dlqData = DeadLettersQueueData(newPolicy.queueName, newPolicy.maxReceiveCount)
      queueData = queueData.copy(deadLettersQueue = Some(dlqData))
    case ClearQueue() =>
      messageQueue.clear()
    case GetQueueStatistics(deliveryTime) => getQueueStatistics(deliveryTime)
  }

  private def getQueueStatistics(deliveryTime: Long) = {
    var visible = 0
    var invisible = 0
    var delayed = 0

    messageQueue.byId.values.foreach { internalMessage =>
      if (internalMessage.nextDelivery < deliveryTime) {
        visible += 1
      } else if (internalMessage.deliveryReceipts.nonEmpty) {
        invisible += 1
      } else {
        delayed += 1
      }
    }

    QueueStatistics(visible, invisible, delayed)
  }
}
