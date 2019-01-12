package org.elasticmq

case class RedrivePolicy(queueName: String, maxReceiveCount: Int)
