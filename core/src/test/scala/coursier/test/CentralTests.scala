package coursier
package test

import utest._
import scala.async.Async.{async, await}

import coursier.test.compatibility._

object CentralTests extends TestSuite {

  val repositories = Seq[Repository](
    repository.mavenCentral
  )

  def repr(dep: Dependency) =
    s"${dep.module.organization}:${dep.module.name}:${dep.`type`}:${Some(dep.classifier).filter(_.nonEmpty).map(_+":").mkString}${dep.version}"


  val tests = TestSuite {
    'logback{
      async {
        val dep = Dependency(Module("ch.qos.logback", "logback-classic"), "1.1.3")
        val res = await(resolve(Set(dep), fetchFrom(repositories)).runF)
          .copy(projectsCache = Map.empty, errors = Map.empty) // No validating these here

        val expected = Resolution(
          rootDependencies = Set(dep.withCompileScope),
          dependencies = Set(
            dep.withCompileScope,
            Dependency(Module("ch.qos.logback", "logback-core"), "1.1.3").withCompileScope,
            Dependency(Module("org.slf4j", "slf4j-api"), "1.7.7").withCompileScope))

        assert(res == expected)
      }
    }
    'asm{
      async {
        val dep = Dependency(Module("org.ow2.asm", "asm-commons"), "5.0.2")
        val res = await(resolve(Set(dep), fetchFrom(repositories)).runF)
          .copy(projectsCache = Map.empty, errors = Map.empty) // No validating these here

        val expected = Resolution(
          rootDependencies = Set(dep.withCompileScope),
          dependencies = Set(
            dep.withCompileScope,
            Dependency(Module("org.ow2.asm", "asm-tree"), "5.0.2").withCompileScope,
            Dependency(Module("org.ow2.asm", "asm"), "5.0.2").withCompileScope))

        assert(res == expected)
      }
    }
    'spark{
      async {
        val expected = await(textResource("resolutions/org.apache.spark:spark-core_2.11:1.3.1")).split('\n').toSeq

        val dep = Dependency(Module("org.apache.spark", "spark-core_2.11"), "1.3.1")
        val res = await(resolve(Set(dep), fetchFrom(repositories)).runF)

        val result = res.dependencies.toVector.map(repr).sorted.distinct

        for (((e, r), idx) <- expected.zip(result).zipWithIndex if e != r)
          println(s"Line $idx:\n  expected: $e\n  got:$r")

        assert(result == expected)
      }
    }
  }

}
