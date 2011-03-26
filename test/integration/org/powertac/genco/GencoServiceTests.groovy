package org.powertac.genco

import grails.test.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant

class GencoServiceTests extends GrailsUnitTestCase 
{
  def simpleGencoService
  def timeService
  
  Instant start
  
  GenCo genco

  protected void setUp() 
  {
    super.setUp()
    genco = new GenCo(name: 'MunicipalPower')
    genco.ensureBroker()
    if (!genco.validate()) {
      genco.errors.allErrors.each { println it.toString() }
    }
    assert genco.save()
    start = new DateTime(2011, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).toInstant()
    timeService.setCurrentTime(start)
  }

  protected void tearDown() 
  {
    super.tearDown()
  }

  void testGenco() 
  {
    assertNotNull("created GenCo", genco)
    assertEquals("correct name", 'MunicipalPower', genco.name)
  }

  // reduce capacity by 1%  
  void testGencoUpdate1 ()
  {
    def values = [0.0, 0.5]
    def mockRandom = [nextDouble: { -> 
      def result = values.first()
      values = values.tail()
      return result }] as Random
    assertEquals("initial capacity", 10.0, genco.currentCapacity, 1e-6)
    assertTrue("in operation", genco.inOperation)
    genco.updateModel(mockRandom, start)
    assertEquals("current capacity", 9.9, genco.currentCapacity, 1e-6)
    assertTrue("still in operation", genco.inOperation)
  }

  // increase capacity by 1%, put offline  
  void testGencoUpdate2 ()
  {
    def values = [1.0, 0.99]
    def mockRandom = [nextDouble: { -> 
      def result = values.first()
      values = values.tail()
      return result }] as Random
    assertTrue("in operation", genco.inOperation)
    genco.updateModel(mockRandom, start)
    assertEquals("current capacity", 10.1, genco.currentCapacity, 1e-6)
    assertFalse("still in operation", genco.inOperation)
  }
}
