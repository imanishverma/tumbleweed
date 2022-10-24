package io.redgreen.tumbleweed

import io.redgreen.tumbleweed.ClassStructure.Companion.logger
import org.slf4j.LoggerFactory

data class ClassStructure(
  val packageName: String,
  val className: String,
  val fields: List<Field>,
  val methods: List<Method>,
  val relationships: List<Relationship>,
) {
  companion object {
    val logger = LoggerFactory.getLogger(ClassStructure::class.java)
  }

  data class Node(
    val type: Relationship.Type? = null,
    val member: Member,
  ) {
    companion object {
      fun root(member: Member): Node {
        return Node(null, member)
      }
    }
  }

  fun normalize(): ClassStructure {
    val lambdas = relationships
      .filter { it.type == Relationship.Type.Calls }
      .map { it.target as Method }
      .filter { it.isLambda }
      .toSet()

    val bridges = relationships
      .flatMap { listOf(it.source, it.target) }
      .filterIsInstance<Method>()
      .filter(Method::isBridge)
      .toSet()

    val nonSyntheticRelationships = skipLambdasInCallChain(relationships)
      .filter { it.source !in bridges }

    val relationshipsInCurrentClass = nonSyntheticRelationships
      .filter { it.target.owner.endsWith(className) }

    return this.copy(
      methods = methods - lambdas - bridges,
      relationships = relationshipsInCurrentClass,
    )
  }

  private fun skipLambdasInCallChain(relationships: List<Relationship>): List<Relationship> {
    val graph = relationships.asGraph()
    val startingPointMethods = graph.keys.filter { !(it as Method).isSynthetic }.map { it as Method }
    val paths = mutableListOf<List<Node>>()

    val queue = ArrayDeque<List<Node>>()
    for (startMethod in startingPointMethods) {
      queue.add(listOf(Node.root(startMethod)))
      while (queue.isNotEmpty()) {
        val currentPath = queue.removeFirst()
        val destinations = graph[currentPath.last().member]
        val pathNotFound = destinations != null && !isRecursiveCall(currentPath, destinations.last(), 2)
        if (pathNotFound) {
          for (destination in destinations!!) {
            val newPath = currentPath + destination
            if (isRecursiveCall(newPath, destination, 3)) {
              break
            }
            queue.add(newPath)
          }
        } else {
          paths.add(currentPath)
          logger.debug("Found path: {}", currentPath.joinToString(" -> ") { it.member.signature.concise })
        }
      }
    }

    return paths
      .map(::removeSyntheticsFromChain)
      .map { path -> path.toRelationships() }
      .flatten()
      .distinct()
  }

  private fun isRecursiveCall(
    path: List<Node>,
    destination: Node,
    nodesToCheck: Int = 3,
  ): Boolean {
    return path
      .takeLast(nodesToCheck)
      .map { node -> node.member.signature }
      .all { signature -> isCyclicCall(signature, destination) } && path.takeLast(nodesToCheck).size == nodesToCheck
  }

  private fun isCyclicCall(
    signature: Signature,
    destination: Node,
  ): Boolean {
    val isRecursiveCall = signature == destination.member.signature
    val isBridgeCall = destination.member.name.startsWith("access\$") &&
      destination.member.name.substring("access\$".length) == signature.name
    return isRecursiveCall || isBridgeCall
  }

  private fun removeSyntheticsFromChain(destinations: List<Node>): List<Node> {
    return destinations.filter { if (it.member is Method) !it.member.isSynthetic else true }
  }

  private fun List<Node>.toRelationships(): List<Relationship> {
    return this
      .zipWithNext()
      .map { (source, target) ->
        Relationship(source.member, target.member, target.type!!) /* FIXME Support 'References' relationship */
      }
  }

  private fun List<Relationship>.asGraph(): MutableMap<Member, List<Node>> {
    val graph = mutableMapOf<Member, List<Node>>()
    for (relationship in this) {
      val source = relationship.source
      val target = relationship.target
      val type = relationship.type

      val destinations = graph[source] ?: emptyList()
      val newDestinations = destinations + Node(type, target)
      graph[source] = newDestinations
    }
    return graph
  }

  internal fun bridgeCallReferences(topLevelClassName: String): List<Relationship> {
    return this.relationships.filter { it.target.owner.endsWith(topLevelClassName) }
  }

  internal fun innerClassConstructorInvocations(): List<Relationship> {
    return relationships
      .filter { it.type == Relationship.Type.Calls }
      .filter { (it.target as Method).name == "<init>" }
      .filter { it.target.owner != className }
  }
}

internal fun List<ClassStructure>.findClassStructureOf(constructor: Member): ClassStructure? {
  logger.debug("Finding class structure of: {}", constructor.owner)
  val classStructure = this.find { it.className == constructor.owner.substringAfterLast('/') }
  if (classStructure == null) {
    logger.warn("Could not find class structure of: {}", constructor.owner)
  }
  return classStructure
}

internal fun List<ClassStructure>.combine(): ClassStructure {
  var classStructure = this.first()
  val topLevelClassName = classStructure.className
  val visitedClassStructures = mutableSetOf<ClassStructure>()

  val innerClassConstructorInvocations = classStructure.innerClassConstructorInvocations()
  for (syntheticInnerClassConstructorInvocation in innerClassConstructorInvocations) {
    val constructorQueue = ArrayDeque(listOf(syntheticInnerClassConstructorInvocation.target))
    val bridgeCallReferencesResult = mutableListOf<Relationship>()

    while (constructorQueue.isNotEmpty()) {
      val constructor = constructorQueue.removeFirst()
      val innerClassStructure = this.findClassStructureOf(constructor)
      if (innerClassStructure == null || innerClassStructure in visitedClassStructures) {
        continue
      }
      visitedClassStructures.add(innerClassStructure)

      val constructorInvocations = innerClassStructure.innerClassConstructorInvocations()
      constructorQueue.addAll(constructorInvocations.map(Relationship::target))

      val bridgeCallReferences = innerClassStructure.bridgeCallReferences(topLevelClassName)
      bridgeCallReferencesResult.addAll(bridgeCallReferences)
    }

    val innerClassInvocationReplacements = bridgeCallReferencesResult.map { relationship ->
      Relationship(
        syntheticInnerClassConstructorInvocation.source,
        relationship.target,
        Relationship.Type.References,
      )
    }

    val expandedRelationships = classStructure.relationships -
      syntheticInnerClassConstructorInvocation + innerClassInvocationReplacements
    classStructure = classStructure.copy(
      relationships = expandedRelationships
    )
  }

  return classStructure
}
